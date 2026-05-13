<script setup lang="ts">
import {ref, computed, onMounted, watch} from 'vue'
import api from '@/api'


const allBooks = ref<any[]>([])
const allGenres = ref<any[]>([])
const filtersOpen = ref(true)

// ── Sorting ───────────────────────────────────────────────────────────────────
const SORT_OPTIONS = [
  {value: 'title', label: 'Title'},
  {value: 'publicationYear', label: 'Publication Year'},
  {value: 'pageCount', label: 'Pages'},
]

function loadPref<T>(key: string, fallback: T): T {
  try {
    const v = localStorage.getItem(key);
    return v !== null ? JSON.parse(v) : fallback
  } catch {
    return fallback
  }
}

const sortBy = ref<string>(loadPref('bv_sortBy', 'title'))
const sortDir = ref<'asc' | 'desc'>(loadPref('bv_sortDir', 'asc'))

watch(sortBy, v => localStorage.setItem('bv_sortBy', JSON.stringify(v)))
watch(sortDir, v => localStorage.setItem('bv_sortDir', JSON.stringify(v)))

function toggleDir() {
  sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc'
}

onMounted(async () => {
  const [booksRes, genresRes] = await Promise.all([api.get('/books'), api.get('/genres')])
  allBooks.value = booksRes.data
  allGenres.value = genresRes.data
})

// ── Search ────────────────────────────────────────────────────────────────────
const searchQuery = ref('')

// ── Mood ──────────────────────────────────────────────────────────────────────
const MOODS = ['adventurous', 'challenging', 'dark', 'emotional', 'funny', 'hopeful',
  'informative', 'inspiring', 'lighthearted', 'mysterious', 'reflective',
  'relaxing', 'sad', 'tense']
const selectedMoods = ref<string[]>([])
const moodMode = ref<'any' | 'all'>('any')

// ── Pace (derived from pageCount) ─────────────────────────────────────────────
const selectedPaces = ref<string[]>([])

function getPace(book: any) {
  const p = book.pageCount
  if (!p) return null
  if (p < 300) return 'Fast'
  if (p < 500) return 'Medium'
  return 'Slow'
}

// ── Type (derived from genres) ────────────────────────────────────────────────
const FICTION_GENRES = new Set(['Fantasy', 'Science Fiction', 'Romance', 'Horror', 'Thriller',
  'Mystery', 'Crime', 'Historical Fiction', 'Adventure', 'Literary Fiction', 'Contemporary Fiction',
  'Magical Realism', 'Dystopian', 'Speculative Fiction', 'Paranormal', 'Urban Fantasy', 'Epic Fantasy',
  'Dark Fantasy', 'Space Opera', 'Cyberpunk', 'Steampunk', 'Alternate History', 'Satire', 'Humor',
  'Drama', 'Coming of Age', "Women's Fiction", 'Chick Lit', 'Fairy Tale', 'Mythology',
  'Short Stories', 'Anthology', 'Young Adult', 'Middle Grade', "Children's", 'Picture Book',
  'Graphic Novel', 'Manga', 'Poetry', 'Play / Drama'])
const selectedTypes = ref<string[]>([])

function getType(book: any): 'Fiction' | 'Nonfiction' | null {
  const names: string[] = book.genres?.map((g: any) => g.name) ?? []
  if (!names.length) return null
  return names.filter(n => FICTION_GENRES.has(n)).length > names.length / 2 ? 'Fiction' : 'Nonfiction'
}

// ── Genres ────────────────────────────────────────────────────────────────────
const includeGenres = ref<string[]>([])
const excludeGenres = ref<string[]>([])
const includeMode = ref<'any' | 'all'>('any')

function toggleInclude(name: string) {
  const ei = excludeGenres.value.indexOf(name);
  if (ei !== -1) excludeGenres.value.splice(ei, 1)
  const ii = includeGenres.value.indexOf(name)
  if (ii === -1) includeGenres.value.push(name); else includeGenres.value.splice(ii, 1)
}

function toggleExclude(name: string) {
  const ii = includeGenres.value.indexOf(name);
  if (ii !== -1) includeGenres.value.splice(ii, 1)
  const ei = excludeGenres.value.indexOf(name)
  if (ei === -1) excludeGenres.value.push(name); else excludeGenres.value.splice(ei, 1)
}


// ── Year ──────────────────────────────────────────────────────────────────────
const yearFrom = ref<number | null>(null)
const yearTo = ref<number | null>(null)

// ── Added date (createdAt) ────────────────────────────────────────────────────
const addedFrom = ref<string>('')   // ISO date string  e.g. "2025-01-01"
const addedTo = ref<string>('')   // ISO date string

// ── Series ────────────────────────────────────────────────────────────────────
const standaloneOnly = ref(false)

// ── Active filter count ───────────────────────────────────────────────────────
const activeFilterCount = computed(() =>
    [selectedMoods.value.length > 0, selectedPaces.value.length > 0,
      selectedTypes.value.length > 0, includeGenres.value.length > 0,
      excludeGenres.value.length > 0,
      !!(yearFrom.value || yearTo.value), standaloneOnly.value,
      !!addedFrom.value, !!addedTo.value
    ].filter(Boolean).length
)

// ── Filter logic ──────────────────────────────────────────────────────────────
const filteredBooks = computed(() => {
  const filtered = allBooks.value.filter(book => {
    if (searchQuery.value.trim()) {
      const q = searchQuery.value.trim().toLowerCase()
      if (!book.title?.toLowerCase().includes(q) &&
          !book.author?.firstName?.toLowerCase().includes(q) &&
          !book.author?.lastName?.toLowerCase().includes(q) &&
          !book.series?.name?.toLowerCase().includes(q)) return false
    }
    if (selectedMoods.value.length) {
      const m = book.mood?.toLowerCase() ?? ''
      const check = selectedMoods.value.map(s => m.includes(s))
      if (moodMode.value === 'any' ? !check.some(Boolean) : !check.every(Boolean)) return false
    }
    if (selectedPaces.value.length && !selectedPaces.value.includes(getPace(book) ?? '')) return false
    if (selectedTypes.value.length) {
      const t = getType(book)
      if (!t || !selectedTypes.value.includes(t)) return false
    }
    if (includeGenres.value.length) {
      const bg: string[] = book.genres?.map((g: any) => g.name) ?? []
      if (includeMode.value === 'any' ? !includeGenres.value.some(g => bg.includes(g))
          : !includeGenres.value.every(g => bg.includes(g))) return false
    }
    if (excludeGenres.value.length) {
      const bg: string[] = book.genres?.map((g: any) => g.name) ?? []
      if (excludeGenres.value.some(g => bg.includes(g))) return false
    }
    if (yearFrom.value && (book.publicationYear ?? 0) < yearFrom.value) return false
    if (yearTo.value && (book.publicationYear ?? 9999) > yearTo.value) return false
    if (standaloneOnly.value && book.series) return false
    // date-added range
    if (addedFrom.value && book.createdAt) {
      if (new Date(book.createdAt) < new Date(addedFrom.value)) return false
    }
    if (addedTo.value && book.createdAt) {
      if (new Date(book.createdAt) > new Date(addedTo.value + 'T23:59:59')) return false
    }
    return true
  })

  // ── Sort ──────────────────────────────────────────────────────────────────
  return [...filtered].sort((a, b) => {
    let va: any, vb: any
    if (sortBy.value === 'title') {
      va = (a.title ?? '').toLowerCase();
      vb = (b.title ?? '').toLowerCase()
    } else if (sortBy.value === 'publicationYear') {
      va = a.publicationYear ?? 0;
      vb = b.publicationYear ?? 0
    } else {
      va = a.pageCount ?? 0;
      vb = b.pageCount ?? 0
    }
    if (va < vb) return sortDir.value === 'asc' ? -1 : 1
    if (va > vb) return sortDir.value === 'asc' ? 1 : -1
    return 0
  })
})

function clearAll() {
  searchQuery.value = '';
  selectedMoods.value = [];
  moodMode.value = 'any'
  selectedPaces.value = [];
  selectedTypes.value = []
  includeGenres.value = [];
  excludeGenres.value = [];
  includeGenres.value = []; excludeGenres.value = []; includeMode.value = 'any'
  yearFrom.value = null; yearTo.value = null
  standaloneOnly.value = false;
  addedFrom.value = '';
  addedTo.value = ''
}

function toggle(arr: string[], val: string) {
  const i = arr.indexOf(val);
  if (i === -1) arr.push(val); else arr.splice(i, 1)
}
</script>

<template>
  <div class="books-view">

    <!-- Search bar -->
    <div class="search-bar">
      <input v-model="searchQuery" placeholder="Search by title, author or series…" class="search-input"/>
      <button @click="filtersOpen = !filtersOpen"
              class="btn-filters" :class="{ 'btn-filters--active': filtersOpen }">
        🔍 Filters{{ activeFilterCount ? ` (${activeFilterCount})` : '' }}
      </button>
      <button v-if="activeFilterCount" @click="clearAll" class="btn-clear-all">✕ Clear all</button>
    </div>

    <!-- Filter panel -->
    <div v-show="filtersOpen" class="filter-panel">

      <!-- Mood — full width -->
      <div class="filter-section filter-section--full">
        <div class="filter-header">
          <strong>🎭 Mood</strong>
          <span class="filter-mode-label">
            <label><input type="radio" v-model="moodMode" value="any"/> any</label>
            <label><input type="radio" v-model="moodMode" value="all"/> all</label>
          </span>
        </div>
        <div class="chips">
          <button v-for="m in MOODS" :key="m" @click="toggle(selectedMoods, m)"
                  class="chip" :class="{ 'chip--active-blue': selectedMoods.includes(m) }">
            {{ m }}
          </button>
        </div>
      </div>

      <!-- Pace + Type -->
      <div class="filter-section">
        <div class="pace-section">
          <strong>⚡ Pace</strong>
          <div class="pace-buttons">
            <button v-for="p in ['Slow','Medium','Fast']" :key="p" @click="toggle(selectedPaces, p)"
                    class="chip chip--md" :class="{ 'chip--active-purple': selectedPaces.includes(p) }">
              {{ p }}
            </button>
          </div>
          <p class="pace-hint">Slow ≥500p · Medium 300–499p · Fast &lt;300p</p>
        </div>
        <div>
          <strong>📂 Type</strong>
          <div class="type-buttons">
            <button v-for="t in ['Fiction','Nonfiction']" :key="t" @click="toggle(selectedTypes, t)"
                    class="chip chip--md" :class="{ 'chip--active-green': selectedTypes.includes(t) }">
              {{ t }}
            </button>
          </div>
        </div>
      </div>

      <!-- Genres -->
      <div class="filter-section">
        <div class="filter-header">
          <strong>🏷️ Genres</strong>
          <span class="filter-mode-label">
            Include:
            <label><input type="radio" v-model="includeMode" value="any"/> any</label>
            <label><input type="radio" v-model="includeMode" value="all"/> all</label>
          </span>
        </div>
        <div class="genre-list">
          <div v-for="g in allGenres" :key="g.id" class="genre-row">
            <button @click="toggleInclude(g.name)"
                    class="btn-genre" :class="{ 'btn-genre--include': includeGenres.includes(g.name) }">+</button>
            <button @click="toggleExclude(g.name)"
                    class="btn-genre" :class="{ 'btn-genre--exclude': excludeGenres.includes(g.name) }">−</button>
            <span class="genre-name"
                  :class="{
                    'genre-name--include': includeGenres.includes(g.name),
                    'genre-name--exclude': excludeGenres.includes(g.name)
                  }">
              {{ g.name }}
            </span>
          </div>
        </div>
      </div>

      <!-- Year + Date + Standalone -->
      <div class="filter-section">
        <div class="subsection">
          <strong>📅 Publication Year</strong>
          <div class="year-range">
            <input v-model.number="yearFrom" type="number" placeholder="From" min="1000" max="2100" class="year-input"/>
            <span class="year-sep">—</span>
            <input v-model.number="yearTo" type="number" placeholder="To" min="1000" max="2100" class="year-input"/>
          </div>
        </div>

        <div class="subsection">
          <strong>🗓️ Date Added</strong>
          <div class="date-filter">
            <label class="date-label">From
              <input v-model="addedFrom" type="date" class="date-input"/>
            </label>
            <label class="date-label">To
              <input v-model="addedTo" type="date" class="date-input"/>
            </label>
            <button @click="addedFrom = new Date().toISOString().slice(0,10); addedTo = ''" class="btn-today">
              📅 From today onwards
            </button>
          </div>
        </div>

        <div>
          <strong>📌 Other</strong>
          <div class="other-options">
            <label class="standalone-label">
              <input type="checkbox" v-model="standaloneOnly"/>
              Not part of a series
            </label>
          </div>
        </div>
      </div>

    </div>

    <!-- Sort bar -->
    <div class="sort-bar">
      <span class="sort-label">Sort by:</span>
      <button v-for="opt in SORT_OPTIONS" :key="opt.value"
              @click="sortBy = opt.value"
              class="sort-btn" :class="{ 'sort-btn--active': sortBy === opt.value }">
        {{ opt.label }}
      </button>
      <button @click="toggleDir"
              :title="sortDir === 'asc' ? 'Currently ascending — click for descending' : 'Currently descending — click for ascending'"
              class="sort-dir-btn">
        {{ sortDir === 'asc' ? '↑ ASC' : '↓ DESC' }}
      </button>
    </div>

    <!-- Results -->
    <p class="results-count">
      <strong>{{ filteredBooks.length }}</strong> book{{ filteredBooks.length !== 1 ? 's' : '' }} found
      <span v-if="activeFilterCount"> with {{ activeFilterCount }} active filter{{
          activeFilterCount !== 1 ? 's' : ''
        }}</span>
    </p>

    <ul class="book-list">
      <li v-for="book in filteredBooks" :key="book.id" class="book-list-item">
        <div class="book-info">
          <RouterLink :to="`/books/${book.id}`" class="book-title-link"><strong>{{ book.title }}</strong></RouterLink>
          <span v-if="book.author" class="book-meta">
            — <RouterLink :to="`/authors/${book.author.id}`">{{ book.author.firstName }} {{
              book.author.lastName
            }}</RouterLink>
          </span>
          <span v-if="book.series" class="book-meta">
            · 📚 <RouterLink :to="`/series/${book.series.id}`">{{ book.series.name }}</RouterLink>
          </span>
          <br/>
          <small class="book-small">
            <span v-if="book.genres?.length">{{ book.genres.map((g: any) => g.name).join(', ') }}</span>
            <span v-if="book.mood"> · 🎭 {{ book.mood }}</span>
            <span v-if="book.pageCount"> · {{ book.pageCount }}p</span>
            <span v-if="book.publicationYear"> · {{ book.publicationYear }}</span>
          </small>
        </div>
      </li>
    </ul>

    <p v-if="filteredBooks.length === 0 && allBooks.length > 0" class="no-results">
      No books match your filters.
      <button @click="clearAll" class="btn-clear-inline">Clear all filters</button>
    </p>
  </div>
</template>

<style scoped>
.books-view {
  max-width: 1100px;
  margin: 0 auto;
  padding: 1rem;
}

.search-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 1rem;
}

.search-input {
  flex: 1;
  padding: 10px 14px;
  font-size: 1rem;
  border: 1px solid #d1d5db;
  border-radius: 8px;
}

.btn-filters {
  padding: 10px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  background: #fff;
  color: #374151;
  cursor: pointer;
  font-weight: 500;
}

.btn-filters--active {
  background: #2563eb;
  color: white;
}

.btn-clear-all {
  padding: 10px 14px;
  border: 1px solid #fca5a5;
  border-radius: 8px;
  background: #fef2f2;
  color: #dc2626;
  cursor: pointer;
}

.filter-panel {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 1.25rem;
  margin-bottom: 1.5rem;
  background: #fafafa;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: auto auto;
  gap: 1rem 1.5rem;
}

/* Mood spans all 3 columns */
.filter-section--full {
  grid-column: 1 / -1;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e5e7eb;
}

/* The three column sections get a left border divider (except first) */
.filter-section {
  padding-left: 1rem;
  border-left: 1px solid #e5e7eb;
}

.filter-section:first-of-type {
  padding-left: 0;
  border-left: none;
}

/* Responsive: collapse to 1 column on small screens */
@media (max-width: 700px) {
  .filter-panel {
    grid-template-columns: 1fr;
  }
  .filter-section {
    padding-left: 0;
    border-left: none;
    border-top: 1px solid #e5e7eb;
    padding-top: 1rem;
  }
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.filter-mode-label {
  font-size: 0.8rem;
  color: #6b7280;
}

.filter-mode-label label + label {
  margin-left: 8px;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.chip {
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 0.82rem;
  cursor: pointer;
  border: 1px solid #d1d5db;
  background: #fff;
  color: #374151;
}

.chip--active-blue {
  background: #2563eb;
  color: white;
  border-color: #2563eb;
}

.chip--active-purple {
  background: #7c3aed;
  color: white;
  border-color: #7c3aed;
}

.chip--active-green {
  background: #059669;
  color: white;
  border-color: #059669;
}

.chip--md   { padding: 4px 14px; border-radius: 6px; font-size: 0.9rem; }

.pace-section {
  margin-bottom: 1rem;
}

.pace-buttons {
  display: flex;
  gap: 6px;
  margin-top: 6px;
}

.pace-hint {
  font-size: 0.75rem;
  color: #9ca3af;
  margin: 4px 0 0;
}

.type-buttons {
  display: flex;
  gap: 6px;
  margin-top: 6px;
}

.genre-list {
  max-height: 180px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.genre-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85rem;
}

.btn-genre {
  padding: 1px 8px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #d1d5db;
  font-size: 0.78rem;
  background: #fff;
  color: #374151;
}

.btn-genre--include {
  background: #2563eb;
  color: white;
  border-color: #2563eb;
}

.btn-genre--exclude {
  background: #dc2626;
  color: white;
  border-color: #dc2626;
}

.genre-name {
  color: #374151;
}

.genre-name--include {
  color: #2563eb;
}

.genre-name--exclude {
  color: #dc2626;
}


.year-range {
  display: flex;
  gap: 8px;
  margin-top: 6px;
  align-items: center;
}

.year-input {
  width: 80px;
  padding: 4px 6px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
}

.year-sep {
  color: #9ca3af;
}

.date-filter {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 6px;
}

.date-label {
  font-size: 0.82rem;
  color: #6b7280;
}

.date-input {
  margin-left: 6px;
  padding: 3px 6px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.85rem;
}

.btn-today {
  align-self: flex-start;
  font-size: 0.78rem;
  padding: 2px 8px;
  border: 1px solid #d1d5db;
  border-radius: 5px;
  background: #f9fafb;
  cursor: pointer;
}

.subsection {
  margin-bottom: 1rem;
}

.standalone-label {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 0.9rem;
}

.other-options {
  margin-top: 6px;
}

.sort-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 0.75rem;
  flex-wrap: wrap;
}

.sort-label {
  font-size: 0.85rem;
  color: #6b7280;
  font-weight: 500;
}

.sort-btn {
  padding: 4px 14px;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
  border: 1px solid #d1d5db;
  background: #fff;
  color: #374151;
  font-weight: 400;
}

.sort-btn--active {
  background: #1e3a5f;
  color: white;
  border-color: #1e3a5f;
  font-weight: 600;
}

.sort-dir-btn {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
  border: 1px solid #d1d5db;
  background: #f9fafb;
  color: #374151;
  display: flex;
  align-items: center;
  gap: 4px;
}

.results-count {
  color: #6b7280;
  margin-bottom: 0.75rem;
}

.results-count strong {
  color: #111;
}

.book-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.book-list-item {
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  margin-bottom: 8px;
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.book-info {
  flex: 1;
}

.book-title-link {
  font-size: 1.05rem;
}

.book-meta {
  color: #6b7280;
}

.book-small {
  color: #9ca3af;
}

.no-results {
  color: #6b7280;
  text-align: center;
  padding: 2rem;
}

.btn-clear-inline {
  color: #2563eb;
  background: none;
  border: none;
  cursor: pointer;
  text-decoration: underline;
}
</style>

