<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api'

const route = useRoute()
const publisher = ref<any>(null)
const books = ref<any[]>([])
const error = ref('')

onMounted(async () => {
  try {
    const [pRes, bRes] = await Promise.all([
      api.get(`/publishers/${route.params.id}`),
      api.get(`/publishers/${route.params.id}/books`)
    ])
    publisher.value = pRes.data
    books.value = bRes.data
    document.title = `${pRes.data.name} — BookVault`
  } catch {
    error.value = 'Publisher not found.'
  }
})
</script>

<template>
  <div class="publisher-detail">
    <RouterLink to="/books">← Back to Books</RouterLink>

    <div v-if="error" class="publisher-error" role="alert">{{ error }}</div>

    <div v-else-if="publisher" class="publisher-content">
      <h1><span aria-hidden="true">🏢</span> {{ publisher.name }}</h1>
      <p v-if="publisher.foundationYear"><strong>Founded:</strong> {{ publisher.foundationYear }}</p>
      <p v-if="publisher.owner"><strong>Owner:</strong> {{ publisher.owner }}</p>

      <h2 class="publisher-books-heading">Books by this Publisher</h2>
      <p v-if="books.length === 0" class="no-books">No books found for this publisher.</p>
      <ul v-else class="publisher-books-list" aria-label="Books by this publisher">
        <li v-for="book in books" :key="book.id" class="publisher-book-item">
          <RouterLink :to="`/books/${book.id}`"><strong>{{ book.title }}</strong></RouterLink>
          <span v-if="book.publicationYear">  {{ book.publicationYear }}</span>
          <span v-if="book.pageCount">  {{ book.pageCount }} pages</span>
          <span v-if="book.mood">  <span aria-hidden="true">🎭</span> {{ book.mood }}</span>
          <br/>
          <small v-if="book.author" class="publisher-book-author">
            {{ book.author.firstName }} {{ book.author.lastName }}
          </small>
          <small class="publisher-book-genres">
            {{ book.genres?.map((g: any) => g.name).join(', ') }}
          </small>
        </li>
      </ul>
    </div>

    <p v-else>Loading...</p>
  </div>
</template>

<style scoped>
.publisher-detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 1rem;
}

.publisher-content { margin-top: 1rem; }
.publisher-error { color: #fb4934; margin-top: 1rem; }
.publisher-books-heading { margin-top: 1.5rem; color: #fabd2f; }
.no-books { color: #a89984; }

.publisher-books-list { list-style: none; padding: 0; }

.publisher-book-item {
  padding: 10px;
  border: 1px solid #504945;
  border-radius: 6px;
  margin-bottom: 8px;
  background: #32302f;
}

.publisher-book-author { color: #83a598; margin-right: 8px; }
.publisher-book-genres { color: #a89984; }
</style>

