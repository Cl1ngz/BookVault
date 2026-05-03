<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
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
  <div style="max-width:800px; margin:0 auto; padding:1rem;">
    <RouterLink to="/authors">← Back to Authors</RouterLink>

    <div v-if="error" style="color:red; margin-top:1rem;">{{ error }}</div>

    <div v-else-if="author" style="margin-top:1rem;">
      <!-- Author profile -->
      <h1>✍️ {{ author.firstName }} {{ author.lastName }}</h1>
      <p v-if="author.nationality"><strong>Nationality:</strong> {{ author.nationality }}</p>
      <p v-if="author.birthDate"><strong>Born:</strong> {{ author.birthDate }}</p>
      <div v-if="author.biography"
           style="background:#f9fafb; border-left:4px solid #2563eb; padding:12px 16px; border-radius:4px; margin:1rem 0;">
        <strong>Biography</strong>
        <p style="margin:8px 0 0; color:#374151;">{{ author.biography }}</p>
      </div>

      <!-- Books -->
      <h2 style="margin-top:1.5rem;">Books ({{ books.length }})</h2>
      <p v-if="books.length === 0" style="color:gray;">No books found for this author.</p>

      <div v-else>
        <!-- Books belonging to a series -->
        <template v-for="[key, seriesBooks] in booksBySeries" :key="key">
          <div style="margin-bottom:1.5rem;">
            <h3 v-if="key !== 'standalone'" style="color:#1e3a5f;">
              📚
              <RouterLink :to="`/series/${seriesBooks[0].series.id}`">
                {{ seriesBooks[0].series.name }}
              </RouterLink>
            </h3>
            <h3 v-else style="color:#6b7280;">📖 Standalone books</h3>

            <ul style="list-style:none; padding:0;">
              <li v-for="book in seriesBooks" :key="book.id"
                  style="padding:10px; border:1px solid #e5e7eb; border-radius:6px; margin-bottom:6px;">
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
        </template>
      </div>
    </div>

    <p v-else>Loading...</p>
  </div>
</template>

