<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
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
  <div style="max-width:800px; margin:0 auto; padding:1rem;">
    <RouterLink to="/series">← Back to Series</RouterLink>

    <div v-if="error" style="color:red; margin-top:1rem;">{{ error }}</div>

    <div v-else-if="series" style="margin-top:1rem;">
      <h1>📚 {{ series.name }}</h1>
      <p>
        <strong>Volumes:</strong> {{ series.volumeCount }}
      </p>
      <p v-if="series.author">
        <strong>Author:</strong>
        <RouterLink :to="`/authors/${series.author.id}`">
          {{ series.author.firstName }} {{ series.author.lastName }}
        </RouterLink>
      </p>

      <h2 style="margin-top:1.5rem;">Books in this series</h2>
      <p v-if="books.length === 0" style="color:gray;">No books found for this series.</p>
      <ul v-else style="list-style:none; padding:0;">
        <li v-for="book in books" :key="book.id"
            style="padding:10px; border:1px solid #e5e7eb; border-radius:6px; margin-bottom:8px;">
          <RouterLink :to="`/books/${book.id}`"><strong>{{ book.title }}</strong></RouterLink>
          <span v-if="book.publicationYear"> · {{ book.publicationYear }}</span>
          <span v-if="book.pageCount"> · {{ book.pageCount }} pages</span>
          <span v-if="book.mood"> · 🎭 {{ book.mood }}</span>
          <br/>
          <small style="color:#6b7280;">
            {{ book.genres?.map((g: any) => g.name).join(', ') }}
          </small>
        </li>
      </ul>
    </div>

    <p v-else>Loading...</p>
  </div>
</template>

