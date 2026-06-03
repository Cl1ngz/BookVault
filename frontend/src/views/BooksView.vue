<script setup lang="ts">
import {ref, computed, onMounted, watch} from 'vue'
import api from '@/api'


const allBooks = ref<any[]>([])
const allGenres = ref<any[]>([])
const filtersOpen = ref(true)

const user = computed(() => {
  try { return JSON.parse(localStorage.getItem('user') || 'null') } catch { return null }
})
const isLoggedIn = computed(() => !!user.value?.token)
const shelfBookIds = ref<Set<number>>(new Set())

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

const searchQuery = ref('')

const MOODS = ['adventurous', 'challenging', 'dark', 'emotional', 'funny', 'hopeful',
  'informative', 'inspiring', 'lighthearted', 'mysterious', 'reflective',
  'relaxing', 'sad', 'tense']
const selectedMoods = ref<string[]>([])
const moodMode = ref<'any' | 'all'>('any')

const selectedPaces = ref<string[]>([])

function getPace(book: any) {
  const p = book.pageCount
  if (!p) return null
  if (p < 300) return 'Fast'
  if (p < 500) return 'Medium'
  return 'Slow'
}

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


const yearFrom = ref<number | null>(null)
const yearTo = ref<number | null>(null)

const addedFrom = ref<string>('')   // ISO date string  e.g. "2025-01-01"
const addedTo = ref<string>('')   // ISO date string

const standaloneOnly = ref(false)

const unreadOnly = ref(true)

const activeFilterCount = computed(() =>
    [selectedMoods.value.length > 0, selectedPaces.value.length > 0,
      selectedTypes.value.length > 0, includeGenres.value.length > 0,
      excludeGenres.value.length > 0,
      !!(yearFrom.value || yearTo.value), standaloneOnly.value,
      !!addedFrom.value, !!addedTo.value, unreadOnly.value
    ].filter(Boolean).length
)

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
    // unread only
    if (unreadOnly.value && shelfBookIds.value.has(book.id)) return false
    // date-added range
    if (addedFrom.value && book.createdAt) {
      if (new Date(book.createdAt) < new Date(addedFrom.value)) return false
    }
    if (addedTo.value && book.createdAt) {
      if (new Date(book.createdAt) > new Date(addedTo.value + 'T23:59:59')) return false
    }
    return true
  })

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
  unreadOnly.value = false
  addedFrom.value = '';
  addedTo.value = ''
}

function toggle(arr: string[], val: string) {
  const i = arr.indexOf(val);
  if (i === -1) arr.push(val); else arr.splice(i, 1)
}

onMounted(async () => {
  document.title = 'Books — BookVault'
  const requests: Promise<any>[] = [api.get('/books'), api.get('/genres')]
  if (isLoggedIn.value) requests.push(api.get('/reading-log'))
  const [booksRes, genresRes, shelfRes] = await Promise.all(requests)
  allBooks.value = booksRes.data
  allGenres.value = genresRes.data
  if (shelfRes) {
    shelfBookIds.value = new Set(shelfRes.data.map((e: any) => e.book?.id))
  }
})
</script>

<template>
  <div class="books-view">

    <!-- Search bar -->
    <div class="search-bar" role="search">
      <label for="book-search" class="visually-hidden">Search books</label>
      <input
        id="book-search"
        v-model="searchQuery"
        type="search"
        placeholder="Search by title, author or series…"
        class="search-input"
        aria-label="Search by title, author or series"
      />
      <button
        @click="filtersOpen = !filtersOpen"
        class="btn-filters"
        :class="{ 'btn-filters--active': filtersOpen }"
        :aria-expanded="filtersOpen"
        aria-controls="filter-panel"
      >
        <span aria-hidden="true">🔍</span>
        Filters{{ activeFilterCount ? ` (${activeFilterCount})` : '' }}
      </button>
      <button
        v-if="activeFilterCount"
        @click="clearAll"
        class="btn-clear-all"
        aria-label="Clear all active filters"
      >
        <span aria-hidden="true">✕</span> Clear all
      </button>
    </div>

    <!-- Filter panel -->
    <div
      v-show="filtersOpen"
      id="filter-panel"
      class="filter-panel"
      role="region"
      aria-label="Filter options"
    >

      <!-- Mood — full width -->
      <div class="filter-section filter-section--full">
        <div class="filter-header">
          <strong id="mood-label"><span aria-hidden="true">🎭</span> Mood</strong>
          <span class="filter-mode-label" role="group" aria-labelledby="mood-mode-label">
            <span id="mood-mode-label" class="visually-hidden">Mood match mode</span>
            <label><input type="radio" v-model="moodMode" value="any" aria-label="Match any mood"/> any</label>
            <label><input type="radio" v-model="moodMode" value="all" aria-label="Match all moods"/> all</label>
          </span>
        </div>
        <div class="chips" role="group" aria-labelledby="mood-label">
          <button
            v-for="m in MOODS"
            :key="m"
            @click="toggle(selectedMoods, m)"
            class="chip"
            :class="{ 'chip--active-blue': selectedMoods.includes(m) }"
            :aria-pressed="selectedMoods.includes(m)"
            :aria-label="`Filter by mood: ${m}`"
          >
            {{ m }}
          </button>
        </div>
      </div>

      <!-- Pace + Type -->
      <div class="filter-section">
        <div class="pace-section">
          <strong id="pace-label"><span aria-hidden="true">⚡</span> Pace</strong>
          <div class="pace-buttons" role="group" aria-labelledby="pace-label">
            <button
              v-for="p in ['Slow','Medium','Fast']"
              :key="p"
              @click="toggle(selectedPaces, p)"
              class="chip chip--md"
              :class="{ 'chip--active-purple': selectedPaces.includes(p) }"
              :aria-pressed="selectedPaces.includes(p)"
              :aria-label="`Filter by pace: ${p}`"
            >
              {{ p }}
            </button>
          </div>
          <p class="pace-hint" aria-label="Pace categories: Slow 500 or more pages, Medium 300 to 499 pages, Fast under 300 pages">
            Slow ≥500p · Medium 300–499p · Fast &lt;300p
          </p>
        </div>
        <div>
          <strong id="type-label"><span aria-hidden="true">📂</span> Type</strong>
          <div class="type-buttons" role="group" aria-labelledby="type-label">
            <button
              v-for="t in ['Fiction','Nonfiction']"
              :key="t"
              @click="toggle(selectedTypes, t)"
              class="chip chip--md"
              :class="{ 'chip--active-green': selectedTypes.includes(t) }"
              :aria-pressed="selectedTypes.includes(t)"
              :aria-label="`Filter by type: ${t}`"
            >
              {{ t }}
            </button>
          </div>
        </div>
      </div>

      <!-- Genres -->
      <div class="filter-section">
        <div class="filter-header">
          <strong id="genres-label"><span aria-hidden="true">🏷️</span> Genres</strong>
          <span class="filter-mode-label" role="group" aria-labelledby="include-mode-label">
            <span id="include-mode-label">Include:</span>
            <label><input type="radio" v-model="includeMode" value="any" aria-label="Include any of the selected genres"/> any</label>
            <label><input type="radio" v-model="includeMode" value="all" aria-label="Include all of the selected genres"/> all</label>
          </span>
        </div>
        <div class="genre-list" role="group" aria-labelledby="genres-label">
          <div v-for="g in allGenres" :key="g.id" class="genre-row">
            <button
              @click="toggleInclude(g.name)"
              class="btn-genre"
              :class="{ 'btn-genre--include': includeGenres.includes(g.name) }"
              :aria-pressed="includeGenres.includes(g.name)"
              :aria-label="`Include genre: ${g.name}`"
            >+</button>
            <button
              @click="toggleExclude(g.name)"
              class="btn-genre"
              :class="{ 'btn-genre--exclude': excludeGenres.includes(g.name) }"
              :aria-pressed="excludeGenres.includes(g.name)"
              :aria-label="`Exclude genre: ${g.name}`"
            >−</button>
            <span
              class="genre-name"
              :class="{
                'genre-name--include': includeGenres.includes(g.name),
                'genre-name--exclude': excludeGenres.includes(g.name)
              }"
              aria-hidden="true"
            >
              {{ g.name }}
            </span>
          </div>
        </div>
      </div>

      <!-- Year + Date + Standalone -->
      <div class="filter-section">
        <div class="subsection">
          <strong id="year-label"><span aria-hidden="true">📅</span> Publication Year</strong>
          <div class="year-range" role="group" aria-labelledby="year-label">
            <label for="year-from" class="visually-hidden">From year</label>
            <input
              id="year-from"
              v-model.number="yearFrom"
              type="number"
              placeholder="From"
              min="1000"
              max="2100"
              class="year-input"
              aria-label="Publication year from"
            />
            <span class="year-sep" aria-hidden="true">—</span>
            <label for="year-to" class="visually-hidden">To year</label>
            <input
              id="year-to"
              v-model.number="yearTo"
              type="number"
              placeholder="To"
              min="1000"
              max="2100"
              class="year-input"
              aria-label="Publication year to"
            />
          </div>
        </div>

        <div class="subsection">
          <strong id="date-added-label"><span aria-hidden="true">🗓️</span> Date Added</strong>
          <div class="date-filter" role="group" aria-labelledby="date-added-label">
            <label class="date-label" for="added-from">From
              <input id="added-from" v-model="addedFrom" type="date" class="date-input"/>
            </label>
            <label class="date-label" for="added-to">To
              <input id="added-to" v-model="addedTo" type="date" class="date-input"/>
            </label>
            <button
              @click="addedFrom = new Date().toISOString().slice(0,10); addedTo = ''"
              class="btn-today"
              aria-label="Set date added filter from today onwards"
            >
              <span aria-hidden="true">📅</span> From today onwards
            </button>
          </div>
        </div>

        <div>
          <strong id="other-label"><span aria-hidden="true">📌</span> Other</strong>
          <div class="other-options" role="group" aria-labelledby="other-label">
            <label class="standalone-label">
              <input type="checkbox" v-model="standaloneOnly" aria-label="Show only books not part of a series"/>
              Not part of a series
            </label>
            <label v-if="isLoggedIn" class="standalone-label">
              <input type="checkbox" v-model="unreadOnly" aria-label="Show only books not on my shelf"/>
              <span aria-hidden="true">📖</span> Not on my shelf (unread)
            </label>
          </div>
        </div>
      </div>

    </div>

    <!-- Sort bar -->
    <div class="sort-bar" role="group" aria-label="Sort books">
      <span class="sort-label" id="sort-label">Sort by:</span>
      <button
        v-for="opt in SORT_OPTIONS"
        :key="opt.value"
        @click="sortBy = opt.value"
        class="sort-btn"
        :class="{ 'sort-btn--active': sortBy === opt.value }"
        :aria-pressed="sortBy === opt.value"
        :aria-label="`Sort by ${opt.label}`"
      >
        {{ opt.label }}
      </button>
      <button
        @click="toggleDir"
        :title="sortDir === 'asc' ? 'Currently ascending — click for descending' : 'Currently descending — click for ascending'"
        :aria-label="sortDir === 'asc' ? 'Sort direction: ascending. Click for descending' : 'Sort direction: descending. Click for ascending'"
        class="sort-dir-btn"
      >
        {{ sortDir === 'asc' ? '↑ ASC' : '↓ DESC' }}
      </button>
    </div>

    <!-- Results -->
    <p class="results-count" role="status" aria-live="polite" aria-atomic="true">
      <strong>{{ filteredBooks.length }}</strong> book{{ filteredBooks.length !== 1 ? 's' : '' }} found
      <span v-if="activeFilterCount"> with {{ activeFilterCount }} active filter{{
          activeFilterCount !== 1 ? 's' : ''
        }}</span>
    </p>

    <ul class="book-list" aria-label="Books list">
      <li v-for="book in filteredBooks" :key="book.id" class="book-list-item">
        <div class="book-info">
          <RouterLink :to="`/books/${book.id}`" class="book-title-link"><strong>{{ book.title }}</strong></RouterLink>
          <span v-if="book.author" class="book-meta">
            — <RouterLink :to="`/authors/${book.author.id}`" :aria-label="`Author: ${book.author.firstName} ${book.author.lastName}`">{{ book.author.firstName }} {{
              book.author.lastName
            }}</RouterLink>
          </span>
          <span v-if="book.series" class="book-meta">
            · <span aria-hidden="true">📚</span>
            <RouterLink :to="`/series/${book.series.id}`" :aria-label="`Series: ${book.series.name}`">{{ book.series.name }}</RouterLink>
          </span>
          <br/>
          <small class="book-small">
            <span v-if="book.genres?.length">{{ book.genres.map((g: any) => g.name).join(', ') }}</span>
            <span v-if="book.mood"> · <span aria-hidden="true">🎭</span> {{ book.mood }}</span>
            <span v-if="book.pageCount"> · {{ book.pageCount }}p</span>
            <span v-if="book.publicationYear"> · {{ book.publicationYear }}</span>
          </small>
        </div>
      </li>
    </ul>

    <p v-if="filteredBooks.length === 0 && allBooks.length > 0" class="no-results" role="status">
      No books match your filters.
      <button @click="clearAll" class="btn-clear-inline">Clear all filters</button>
    </p>
  </div>
</template>

<style scoped>
/* Visually hidden utility (WCAG) */
.visually-hidden {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

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
  border: 1px solid #504945;
  border-radius: 8px;
  background: #32302f;
  color: #ebdbb2;
}
.search-input::placeholder { color: #7c6f64; }
.search-input:focus { outline: none; border-color: #83a598; }

.btn-filters {
  padding: 10px 16px;
  border: 1px solid #504945;
  border-radius: 8px;
  background: #3c3836;
  color: #d5c4a1;
  cursor: pointer;
  font-weight: 500;
  transition: background 0.12s;
}
.btn-filters:hover { background: #504945; }

.btn-filters--active {
  background: #458588;
  color: #ebdbb2;
  border-color: #458588;
}

.btn-clear-all {
  padding: 10px 14px;
  border: 1px solid #fb4934;
  border-radius: 8px;
  background: rgba(204, 36, 29, 0.15);
  color: #fb4934;
  cursor: pointer;
}

.filter-panel {
  border: 1px solid #504945;
  border-radius: 10px;
  padding: 1.25rem;
  margin-bottom: 1.5rem;
  background: #32302f;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: auto auto;
  gap: 1rem 1.5rem;
}

.filter-section--full {
  grid-column: 1 / -1;
  padding-bottom: 1rem;
  border-bottom: 1px solid #504945;
}

.filter-section {
  padding-left: 1rem;
  border-left: 1px solid #504945;
}

.filter-section:first-of-type {
  padding-left: 0;
  border-left: none;
}

@media (max-width: 700px) {
  .filter-panel { grid-template-columns: 1fr; }
  .filter-section { padding-left: 0; border-left: none; border-top: 1px solid #504945; padding-top: 1rem; }
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.filter-mode-label {
  font-size: 0.8rem;
  color: #a89984;
}
.filter-mode-label label + label { margin-left: 8px; }

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
  border: 1px solid #504945;
  background: #3c3836;
  color: #d5c4a1;
  transition: background 0.1s, color 0.1s;
}
.chip:hover { border-color: #83a598; }

.chip--active-blue {
  background: #458588;
  color: #ebdbb2;
  border-color: #458588;
}

.chip--active-purple {
  background: #b16286;
  color: #ebdbb2;
  border-color: #b16286;
}

.chip--active-green {
  background: #98971a;
  color: #ebdbb2;
  border-color: #98971a;
}

.chip--md { padding: 4px 14px; border-radius: 6px; font-size: 0.9rem; }

.pace-section { margin-bottom: 1rem; }
.pace-buttons { display: flex; gap: 6px; margin-top: 6px; }
.pace-hint { font-size: 0.75rem; color: #7c6f64; margin: 4px 0 0; }
.type-buttons { display: flex; gap: 6px; margin-top: 6px; }

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
  border: 1px solid #504945;
  font-size: 0.78rem;
  background: #3c3836;
  color: #d5c4a1;
}

.btn-genre--include { background: #458588; color: #ebdbb2; border-color: #458588; }
.btn-genre--exclude { background: #cc241d; color: #ebdbb2; border-color: #cc241d; }

.genre-name { color: #d5c4a1; }
.genre-name--include { color: #83a598; }
.genre-name--exclude { color: #fb4934; }

.year-range { display: flex; gap: 8px; margin-top: 6px; align-items: center; }

.year-input {
  width: 80px;
  padding: 4px 6px;
  border: 1px solid #504945;
  border-radius: 6px;
  font-size: 0.9rem;
  background: #3c3836;
  color: #ebdbb2;
  -moz-appearance: textfield;
}
.year-input:focus { outline: none; border-color: #83a598; }
.year-input::-webkit-outer-spin-button,
.year-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.year-sep { color: #7c6f64; }

.date-filter { display: flex; flex-direction: column; gap: 6px; margin-top: 6px; }
.date-label { font-size: 0.82rem; color: #a89984; }

.date-input {
  margin-left: 6px;
  padding: 3px 6px;
  border: 1px solid #504945;
  border-radius: 6px;
  font-size: 0.85rem;
  background: #3c3836;
  color: #ebdbb2;
}

.btn-today {
  align-self: flex-start;
  font-size: 0.78rem;
  padding: 2px 8px;
  border: 1px solid #504945;
  border-radius: 5px;
  background: #3c3836;
  color: #d5c4a1;
  cursor: pointer;
}

.subsection { margin-bottom: 1rem; }

.standalone-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  color: #d5c4a1;
  user-select: none;
}

.standalone-label input[type="checkbox"] {
  appearance: none;
  -webkit-appearance: none;
  width: 16px;
  height: 16px;
  min-width: 16px;
  border: 2px solid #665c54;
  border-radius: 4px;
  background: #32302f;
  cursor: pointer;
  position: relative;
  transition: border-color 0.15s, background 0.15s;
}

.standalone-label input[type="checkbox"]:hover {
  border-color: #83a598;
}

.standalone-label input[type="checkbox"]:checked {
  background: #458588;
  border-color: #458588;
}

.standalone-label input[type="checkbox"]:checked::after {
  content: '';
  position: absolute;
  left: 3px;
  top: 0px;
  width: 5px;
  height: 9px;
  border: 2px solid #1d2021;
  border-top: none;
  border-left: none;
  transform: rotate(45deg);
}

/* radio buttons in filter mode labels */
.filter-mode-label input[type="radio"] {
  appearance: none;
  -webkit-appearance: none;
  width: 14px;
  height: 14px;
  border: 2px solid #665c54;
  border-radius: 50%;
  background: #32302f;
  cursor: pointer;
  position: relative;
  vertical-align: middle;
  transition: border-color 0.15s;
}

.filter-mode-label input[type="radio"]:hover {
  border-color: #83a598;
}

.filter-mode-label input[type="radio"]:checked {
  border-color: #458588;
  background: #458588;
}

.filter-mode-label input[type="radio"]:checked::after {
  content: '';
  position: absolute;
  top: 2px;
  left: 2px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #1d2021;
}

.other-options { margin-top: 6px; display: flex; flex-direction: column; gap: 6px; }

.sort-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 0.75rem;
  flex-wrap: wrap;
}

.sort-label { font-size: 0.85rem; color: #a89984; font-weight: 500; }

.sort-btn {
  padding: 4px 14px;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
  border: 1px solid #504945;
  background: #3c3836;
  color: #d5c4a1;
  font-weight: 400;
  transition: background 0.1s;
}
.sort-btn:hover { background: #504945; }

.sort-btn--active {
  background: #458588;
  color: #ebdbb2;
  border-color: #458588;
  font-weight: 600;
}

.sort-dir-btn {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
  border: 1px solid #504945;
  background: #32302f;
  color: #d5c4a1;
  display: flex;
  align-items: center;
  gap: 4px;
}

.results-count { color: #a89984; margin-bottom: 0.75rem; }
.results-count strong { color: #ebdbb2; }

.book-list { list-style: none; padding: 0; margin: 0; }

.book-list-item {
  padding: 10px 12px;
  border: 1px solid #504945;
  border-radius: 8px;
  margin-bottom: 8px;
  display: flex;
  gap: 12px;
  align-items: flex-start;
  background: #32302f;
  transition: border-color 0.12s;
}
.book-list-item:hover { border-color: #665c54; }

.book-info { flex: 1; }

.book-title-link { font-size: 1.05rem; color: #d5c4a1; }
.book-title-link:hover { color: #fabd2f; }

.book-meta { color: #a89984; }
.book-small { color: #7c6f64; }

.no-results { color: #a89984; text-align: center; padding: 2rem; }

.btn-clear-inline {
  color: #83a598;
  background: none;
  border: none;
  cursor: pointer;
  text-decoration: underline;
}
</style>

