<script setup lang="ts">
import {ref, onMounted, computed} from 'vue'
import {useRoute} from 'vue-router'
import api from '@/api'


const route = useRoute()
const author = ref<any>(null)
const books = ref<any[]>([])
const error = ref('')

onMounted(async () => {
  try {
    const [aRes, bRes] = await Promise.all([
      api.get(`/authors/${route.params.id}`),
      api.get(`/authors/${route.params.id}/books`)
    ])
    author.value = aRes.data
    books.value = bRes.data
  } catch {
    error.value = 'Author not found.'
  }
})

// Group books by series (books without a series go under null)
const booksBySeries = computed(() => {
  const map = new Map<string, any[]>()
  for (const book of books.value) {
    const key = book.series ? `${book.series.id}::${book.series.name}` : 'standalone'
    if (!map.has(key)) map.set(key, [])
    map.get(key)!.push(book)
  }
  return map
})
</script>

<template>
  <div class="author-detail">
    <RouterLink to="/authors">← Back to Authors</RouterLink>

    <div v-if="error" class="author-error">{{ error }}</div>

    <div v-else-if="author" class="author-content">
      <h1>✍️ {{ author.firstName }} {{ author.lastName }}</h1>
      <p v-if="author.nationality"><strong>Nationality:</strong> {{ author.nationality }}</p>
      <p v-if="author.birthDate"><strong>Born:</strong> {{ author.birthDate }}</p>
      <div v-if="author.biography" class="author-biography">
        <strong>Biography</strong>
        <p>{{ author.biography }}</p>
      </div>

      <h2 class="books-heading">Books ({{ books.length }})</h2>
      <p v-if="books.length === 0" class="no-books">No books found for this author.</p>

      <div v-else>
        <template v-for="[key, seriesBooks] in booksBySeries" :key="key">
          <div class="series-group">
            <h3 v-if="key !== 'standalone'" class="series-title">
              📚
              <RouterLink :to="`/series/${seriesBooks[0].series.id}`">
                {{ seriesBooks[0].series.name }}
              </RouterLink>
            </h3>
            <h3 v-else class="standalone-title">📖 Standalone books</h3>

            <ul class="books-list">
              <li v-for="book in seriesBooks" :key="book.id" class="book-item">
                <RouterLink :to="`/books/${book.id}`"><strong>{{ book.title }}</strong></RouterLink>
                <span v-if="book.publicationYear"> · {{ book.publicationYear }}</span>
                <span v-if="book.pageCount"> · {{ book.pageCount }} pages</span>
                <span v-if="book.mood"> · 🎭 {{ book.mood }}</span>
                <br/>
                <small class="book-genres">
                  {{ book.genres?.map((g: any) => g.name).join(', ') }}
                </small>
              </li>
            </ul>
          </div>
        </template>
      </div>
    </div>

    <p v-else>Loading...</p>
  </div>
</template>

<style scoped>
.author-detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 1rem;
}

.author-content {
  margin-top: 1rem;
}

.author-error {
  color: red;
  margin-top: 1rem;
}

.author-biography {
  background: #f9fafb;
  border-left: 4px solid #2563eb;
  padding: 12px 16px;
  border-radius: 4px;
  margin: 1rem 0;
}

.author-biography p {
  margin: 8px 0 0;
  color: #374151;
}

.books-heading {
  margin-top: 1.5rem;
}

.no-books {
  color: gray;
}

.series-group {
  margin-bottom: 1.5rem;
}

.series-title {
  color: #1e3a5f;
}

.standalone-title {
  color: #6b7280;
}

.books-list {
  list-style: none;
  padding: 0;
}

.book-item {
  padding: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  margin-bottom: 6px;
}

.book-genres {
  color: #6b7280;
}
</style>

