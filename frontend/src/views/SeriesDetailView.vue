<script setup lang="ts">
import {ref, onMounted} from 'vue'
import {useRoute} from 'vue-router'
import api from '@/api'


const route = useRoute()
const series = ref<any>(null)
const books = ref<any[]>([])
const error = ref('')

onMounted(async () => {
  try {
    const [sRes, bRes] = await Promise.all([
      api.get(`/series/${route.params.id}`),
      api.get(`/series/${route.params.id}/books`)
    ])
    series.value = sRes.data
    books.value = bRes.data
  } catch {
    error.value = 'Series not found.'
  }
})
</script>

<template>
  <div class="series-detail">
    <RouterLink to="/series">← Back to Series</RouterLink>

    <div v-if="error" class="series-error">{{ error }}</div>

    <div v-else-if="series" class="series-content">
      <h1>📚 {{ series.name }}</h1>
      <p><strong>Volumes:</strong> {{ series.volumeCount }}</p>
      <p v-if="series.author">
        <strong>Author:</strong>
        <RouterLink :to="`/authors/${series.author.id}`">
          {{ series.author.firstName }} {{ series.author.lastName }}
        </RouterLink>
      </p>

      <h2 class="series-books-heading">Books in this series</h2>
      <p v-if="books.length === 0" class="no-books">No books found for this series.</p>
      <ul v-else class="series-books-list">
        <li v-for="book in books" :key="book.id" class="series-book-item">
          <RouterLink :to="`/books/${book.id}`"><strong>{{ book.title }}</strong></RouterLink>
          <span v-if="book.publicationYear"> · {{ book.publicationYear }}</span>
          <span v-if="book.pageCount"> · {{ book.pageCount }} pages</span>
          <span v-if="book.mood"> · 🎭 {{ book.mood }}</span>
          <br/>
          <small class="series-book-genres">
            {{ book.genres?.map((g: any) => g.name).join(', ') }}
          </small>
        </li>
      </ul>
    </div>

    <p v-else>Loading...</p>
  </div>
</template>

<style scoped>
.series-detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 1rem;
}

.series-content {
  margin-top: 1rem;
}

.series-error {
  color: red;
  margin-top: 1rem;
}

.series-books-heading {
  margin-top: 1.5rem;
}

.no-books {
  color: gray;
}

.series-books-list {
  list-style: none;
  padding: 0;
}

.series-book-item {
  padding: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  margin-bottom: 8px;
}

.series-book-genres {
  color: #6b7280;
}
</style>

