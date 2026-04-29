<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import api from '@/api'

const allBooks = ref<any[]>([])
const allGenres = ref<any[]>([])
const searchQuery = ref('')
const selectedGenres = ref<string[]>([])

// Load all books and all genres on mount
onMounted(async () => {
  const [booksRes, genresRes] = await Promise.all([
    api.get('/books'),
    api.get('/genres')
  ])
  allBooks.value = booksRes.data
  allGenres.value = genresRes.data
})

// Smart search: title → series → author
function matchesSearch(book: any): boolean {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return true

  const titleMatch = book.title?.toLowerCase().includes(q)
  if (titleMatch) return true

  const seriesMatch = book.series?.name?.toLowerCase().includes(q)
  if (seriesMatch) return true

  const authorMatch =
      book.author?.firstName?.toLowerCase().includes(q) ||
      book.author?.lastName?.toLowerCase().includes(q)
  return authorMatch
}

// Genre filter: book must have ALL selected genres
function matchesGenres(book: any): boolean {
  if (selectedGenres.value.length === 0) return true
  const bookGenreNames = book.genres?.map((g: any) => g.name) ?? []
  return selectedGenres.value.every(g => bookGenreNames.includes(g))
}

const filteredBooks = computed(() =>
    allBooks.value.filter(b => matchesSearch(b) && matchesGenres(b))
)

function toggleGenre(name: string) {
  const idx = selectedGenres.value.indexOf(name)
  if (idx === -1) selectedGenres.value.push(name)
  else selectedGenres.value.splice(idx, 1)
}

function clearFilters() {
  searchQuery.value = ''
  selectedGenres.value = []
}
</script>

<template>
  <div style="display: flex; gap: 2rem;">

    <!-- Sidebar: Genre filter -->
    <aside style="min-width: 180px;">
      <h3>Genres</h3>
      <button @click="clearFilters" style="margin-bottom: 8px;">Clear all</button>
      <ul style="list-style: none; padding: 0;">
        <li v-for="genre in allGenres" :key="genre.id">
          <label>
            <input
                type="checkbox"
                :value="genre.name"
                :checked="selectedGenres.includes(genre.name)"
                @change="toggleGenre(genre.name)"
            />
            {{ genre.name }}
          </label>
        </li>
      </ul>
    </aside>

    <!-- Main: Search + Book list -->
    <main style="flex: 1;">
      <h1>Books</h1>

      <input
          v-model="searchQuery"
          placeholder="Search by title, series or author..."
          style="width: 100%; padding: 8px; margin-bottom: 1rem;"
      />

      <p v-if="selectedGenres.length">
        Filtering by: <strong>{{ selectedGenres.join(', ') }}</strong>
      </p>

      <p>{{ filteredBooks.length }} book(s) found</p>

      <ul>
        <li v-for="book in filteredBooks" :key="book.id" style="margin-bottom: 8px;">
          <RouterLink :to="`/books/${book.id}`"><strong>{{ book.title }}</strong></RouterLink>
          <span v-if="book.author"> — {{ book.author.firstName }} {{ book.author.lastName }}</span>
          <span v-if="book.series"> — 📚 {{ book.series.name }}</span>
          <br/>
          <small>
            {{ book.genres?.map((g: any) => g.name).join(', ') }}
            <span v-if="book.mood"> · 🎭 {{ book.mood }}</span>
          </small>
        </li>
      </ul>

      <p v-if="filteredBooks.length === 0">No books found.</p>
    </main>

  </div>
</template>
