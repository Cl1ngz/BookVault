<script setup lang="ts">
import {ref, computed, onMounted, watch} from 'vue'
import api from '@/api'


const allBooks = ref<any[]>([])
const allGenres = ref<any[]>([])
const filtersOpen = ref(false)
const activeSection = ref<string | null>(null)

function toggleSection(name: string) {
  activeSection.value = activeSection.value === name ? null : name
}

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

const unreadOnly = ref(loadPref('bv_unreadOnly', false))
watch(unreadOnly, v => localStorage.setItem('bv_unreadOnly', JSON.stringify(v)))

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

const pageSize = ref<number>(loadPref('bv_pageSize', 50))
const currentPage = ref(1)

watch(pageSize, v => { localStorage.setItem('bv_pageSize', JSON.stringify(v)); currentPage.value = 1 })
watch(filteredBooks, () => { currentPage.value = 1 })

const totalPages = computed(() =>
  pageSize.value === 0 ? 1 : Math.max(1, Math.ceil(filteredBooks.value.length / pageSize.value))
)

const displayedBooks = computed(() => {
  if (pageSize.value === 0) return filteredBooks.value
  const start = (currentPage.value - 1) * pageSize.value
  return filteredBooks.value.slice(start, start + pageSize.value)
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
  const [booksRes, genresRes] = await Promise.all([api.get('/books'), api.get('/genres')])
  allBooks.value = booksRes.data
  allGenres.value = genresRes.data

  if (isLoggedIn.value) {
    try {
      const shelfRes = await api.get('/reading-log')
      shelfBookIds.value = new Set(shelfRes.data.map((e: any) => e.book?.id))
    } catch {
      // shelf fetch failed (e.g. expired token) — books still show, unread filter skipped
      shelfBookIds.value = new Set()
    }
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
        @click="filtersOpen = !filtersOpen; activeSection = filtersOpen ? (activeSection ?? 'mood') : null"
        class="btn-filters"
        :class="{ 'btn-filters--active': filtersOpen }"
        :aria-expanded="filtersOpen"
        aria-controls="filter-panel"
      >
        <span aria-hidden="true">🔍</span>
        Filters{{ activeFilterCount > (unreadOnly ? 1 : 0) ? ` (${activeFilterCount - (unreadOnly ? 1 : 0)})` : '' }}
      </button>
      <!-- Persistent unread toggle — always visible, saved as preference -->
      <label v-if="isLoggedIn" class="quick-toggle" :class="{ 'quick-toggle--active': unreadOnly }" :title="unreadOnly ? 'Showing only unread books — click to show all' : 'Showing all books — click to hide books already on shelf'">
        <input type="checkbox" v-model="unreadOnly" aria-label="Show only books not on my shelf"/>
        <span aria-hidden="true">📖</span> Unread only
      </label>
      <button
        v-if="activeFilterCount - (unreadOnly ? 1 : 0) > 0"
        @click="clearAll"
        class="btn-clear-all"
        aria-label="Clear all active filters"
      >
        <span aria-hidden="true">✕</span> Clear filters
      </button>
    </div>

    <!-- Filter panel — accordion: one section open at a time -->
    <div
      v-show="filtersOpen"
      id="filter-panel"
      class="filter-panel"
      role="region"
      aria-label="Filter options"
    >
      <!-- Section tab bar -->
      <div class="accordion-tabs" role="tablist">
        <button
          v-for="tab in [
            { key:'mood',   label:'🎭 Mood',    active: selectedMoods.length > 0 },
            { key:'pace',   label:'⚡ Pace / Type', active: selectedPaces.length > 0 || selectedTypes.length > 0 },
            { key:'genres', label:'🏷️ Genres',  active: includeGenres.length > 0 || excludeGenres.length > 0 },
            { key:'other',  label:'📌 Other',   active: !!(yearFrom || yearTo || addedFrom || addedTo || standaloneOnly) },
          ]"
          :key="tab.key"
          @click="toggleSection(tab.key)"
          class="acc-tab"
          :class="{ 'acc-tab--open': activeSection === tab.key, 'acc-tab--dirty': tab.active }"
          :aria-selected="activeSection === tab.key"
          role="tab"
        >
          {{ tab.label }}
          <span v-if="tab.active" class="acc-dot" aria-hidden="true"/>
        </button>
      </div>

      <!-- Mood body -->
      <div v-show="activeSection === 'mood'" class="acc-body" role="tabpanel">
        <div class="filter-header">
          <span class="filter-mode-label" role="group">
            Match:
            <label><input type="radio" v-model="moodMode" value="any" aria-label="Match any mood"/> any</label>
            <label><input type="radio" v-model="moodMode" value="all" aria-label="Match all moods"/> all</label>
          </span>
        </div>
        <div class="chips" role="group" aria-label="Mood filters">
          <button
            v-for="m in MOODS" :key="m"
            @click="toggle(selectedMoods, m)"
            class="chip"
            :class="{ 'chip--active-blue': selectedMoods.includes(m) }"
            :aria-pressed="selectedMoods.includes(m)"
          >{{ m }}</button>
        </div>
      </div>

      <!-- Pace + Type body -->
      <div v-show="activeSection === 'pace'" class="acc-body" role="tabpanel">
        <div class="pace-type-row">
          <div>
            <strong>⚡ Pace</strong>
            <div class="pace-buttons" role="group" aria-label="Pace filters">
              <button
                v-for="p in ['Slow','Medium','Fast']" :key="p"
                @click="toggle(selectedPaces, p)"
                class="chip chip--md"
                :class="{ 'chip--active-purple': selectedPaces.includes(p) }"
                :aria-pressed="selectedPaces.includes(p)"
              >{{ p }}</button>
            </div>
            <p class="pace-hint">Slow ≥500p · Medium 300–499p · Fast &lt;300p</p>
          </div>
          <div>
            <strong>📂 Type</strong>
            <div class="type-buttons" role="group" aria-label="Type filters">
              <button
                v-for="t in ['Fiction','Nonfiction']" :key="t"
                @click="toggle(selectedTypes, t)"
                class="chip chip--md"
                :class="{ 'chip--active-green': selectedTypes.includes(t) }"
                :aria-pressed="selectedTypes.includes(t)"
              >{{ t }}</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Genres body -->
      <div v-show="activeSection === 'genres'" class="acc-body" role="tabpanel">
        <div class="filter-header">
          <span class="filter-mode-label" role="group">
            Include:
            <label><input type="radio" v-model="includeMode" value="any" aria-label="Include any genre"/> any</label>
            <label><input type="radio" v-model="includeMode" value="all" aria-label="Include all genres"/> all</label>
          </span>
        </div>
        <div class="genre-list" role="group" aria-label="Genre filters">
          <div v-for="g in allGenres" :key="g.id" class="genre-row">
            <button @click="toggleInclude(g.name)" class="btn-genre" :class="{ 'btn-genre--include': includeGenres.includes(g.name) }" :aria-pressed="includeGenres.includes(g.name)" :aria-label="`Include ${g.name}`">+</button>
            <button @click="toggleExclude(g.name)" class="btn-genre" :class="{ 'btn-genre--exclude': excludeGenres.includes(g.name) }" :aria-pressed="excludeGenres.includes(g.name)" :aria-label="`Exclude ${g.name}`">−</button>
            <span class="genre-name" :class="{ 'genre-name--include': includeGenres.includes(g.name), 'genre-name--exclude': excludeGenres.includes(g.name) }">{{ g.name }}</span>
          </div>
        </div>
      </div>

      <!-- Other body -->
      <div v-show="activeSection === 'other'" class="acc-body" role="tabpanel">
        <div class="other-grid">
          <div class="subsection">
            <strong>📅 Publication Year</strong>
            <div class="year-range" role="group" aria-label="Year range">
              <input v-model.number="yearFrom" type="number" placeholder="From" min="1000" max="2100" class="year-input" aria-label="From year"/>
              <span class="year-sep" aria-hidden="true">—</span>
              <input v-model.number="yearTo" type="number" placeholder="To" min="1000" max="2100" class="year-input" aria-label="To year"/>
            </div>
          </div>
          <div class="subsection">
            <strong>🗓️ Date Added</strong>
            <div class="date-filter" role="group" aria-label="Date added range">
              <label class="date-label">From <input v-model="addedFrom" type="date" class="date-input"/></label>
              <label class="date-label">To <input v-model="addedTo" type="date" class="date-input"/></label>
              <button @click="addedFrom = new Date().toISOString().slice(0,10); addedTo = ''" class="btn-today">📅 From today</button>
            </div>
          </div>
          <div>
            <strong>📌 Other</strong>
            <div class="other-options">
              <label class="standalone-label">
                <input type="checkbox" v-model="standaloneOnly" aria-label="Not part of a series"/>
                Not part of a series
              </label>
            </div>
          </div>
        </div>
      </div>

    </div>

    <!-- Sort / display bar -->
    <div class="sort-bar" role="group" aria-label="Sort and display options">
      <!-- Left: per-page selector -->
      <label class="bar-label" for="page-size-select">Show</label>
      <select id="page-size-select" v-model.number="pageSize" class="bar-select" aria-label="Books per page">
        <option :value="25">25</option>
        <option :value="50">50</option>
        <option :value="100">100</option>
        <option :value="0">All</option>
      </select>

      <!-- Spacer -->
      <span class="bar-spacer" aria-hidden="true"/>

      <!-- Middle: sort-by dropdown -->
      <label class="bar-label" for="sort-by-select">Sort by</label>
      <select id="sort-by-select" v-model="sortBy" class="bar-select" aria-label="Sort by">
        <option v-for="opt in SORT_OPTIONS" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
      </select>

      <!-- Right: direction toggle -->
      <button
        @click="toggleDir"
        :title="sortDir === 'asc' ? 'Ascending — click for descending' : 'Descending — click for ascending'"
        :aria-label="sortDir === 'asc' ? 'Sort ascending. Click for descending' : 'Sort descending. Click for ascending'"
        class="sort-dir-btn"
      >
        {{ sortDir === 'asc' ? '↑ ASC' : '↓ DESC' }}
      </button>
    </div>

    <!-- Results count -->
    <p class="results-count" role="status" aria-live="polite" aria-atomic="true">
      <strong>{{ filteredBooks.length }}</strong> book{{ filteredBooks.length !== 1 ? 's' : '' }} found
      <span v-if="activeFilterCount"> with {{ activeFilterCount }} active filter{{ activeFilterCount !== 1 ? 's' : '' }}</span>
      <span v-if="pageSize > 0 && totalPages > 1"> · page {{ currentPage }} / {{ totalPages }}</span>
    </p>

    <ul class="book-list" aria-label="Books list">
      <li v-for="book in displayedBooks" :key="book.id" class="book-list-item">
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

    <!-- Pagination -->
    <nav v-if="pageSize > 0 && totalPages > 1" class="pagination" aria-label="Page navigation">
      <button @click="currentPage = 1" :disabled="currentPage === 1" class="pg-btn" aria-label="First page">«</button>
      <button @click="currentPage--" :disabled="currentPage === 1" class="pg-btn" aria-label="Previous page">‹</button>

      <template v-for="p in totalPages" :key="p">
        <button
          v-if="p === 1 || p === totalPages || Math.abs(p - currentPage) <= 2"
          @click="currentPage = p"
          class="pg-btn"
          :class="{ 'pg-btn--active': p === currentPage }"
          :aria-label="`Page ${p}`"
          :aria-current="p === currentPage ? 'page' : undefined"
        >{{ p }}</button>
        <span v-else-if="p === currentPage - 3 || p === currentPage + 3" class="pg-ellipsis" aria-hidden="true">…</span>
      </template>

      <button @click="currentPage++" :disabled="currentPage === totalPages" class="pg-btn" aria-label="Next page">›</button>
      <button @click="currentPage = totalPages" :disabled="currentPage === totalPages" class="pg-btn" aria-label="Last page">»</button>
    </nav>
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
  overflow: hidden;
  margin-bottom: 1.5rem;
  background: #32302f;
}

/* Accordion tab row */
.accordion-tabs {
  display: flex;
  border-bottom: 1px solid #504945;
  overflow-x: auto;
}

.acc-tab {
  flex: 1;
  padding: 9px 14px;
  font-size: 0.85rem;
  cursor: pointer;
  border: none;
  border-right: 1px solid #504945;
  background: #3c3836;
  color: #a89984;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 5px;
  justify-content: center;
  transition: background 0.12s, color 0.12s;
  position: relative;
  white-space: nowrap;
}
.acc-tab:last-child { border-right: none; }
.acc-tab:hover { background: #504945; color: #d5c4a1; }
.acc-tab--open { background: #32302f; color: #ebdbb2; border-bottom: 2px solid #458588; }
.acc-tab--dirty { color: #83a598; }
.acc-tab--dirty.acc-tab--open { color: #8ec07c; }

.acc-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #83a598;
  display: inline-block;
}

/* Accordion body */
.acc-body {
  padding: 1rem 1.25rem;
}

.pace-type-row {
  display: flex;
  gap: 2rem;
  flex-wrap: wrap;
}

.other-grid {
  display: flex;
  gap: 2rem;
  flex-wrap: wrap;
}

/* Persistent unread-only quick toggle in search bar */
.quick-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border: 1px solid #504945;
  border-radius: 8px;
  background: #3c3836;
  color: #a89984;
  font-size: 0.85rem;
  cursor: pointer;
  user-select: none;
  white-space: nowrap;
  transition: background 0.12s, border-color 0.12s, color 0.12s;
}
.quick-toggle:hover { border-color: #665c54; color: #d5c4a1; }
.quick-toggle--active {
  background: rgba(69, 133, 136, 0.2);
  border-color: #458588;
  color: #83a598;
}
.quick-toggle input[type="checkbox"] { display: none; }

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
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 4px 12px;
  max-height: 260px;
  overflow-y: auto;
  padding-right: 4px;
}

.genre-row {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 0.85rem;
  min-width: 0;
}

.genre-name {
  color: #d5c4a1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}
.genre-name--include { color: #83a598; }
.genre-name--exclude { color: #fb4934; }

.btn-genre {
  padding: 1px 8px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #504945;
  font-size: 0.78rem;
  background: #3c3836;
  color: #d5c4a1;
  flex-shrink: 0;
}
.btn-genre--include { background: #458588; color: #ebdbb2; border-color: #458588; }
.btn-genre--exclude { background: #cc241d; color: #ebdbb2; border-color: #cc241d; }


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

.bar-label {
  font-size: 0.85rem;
  color: #a89984;
  white-space: nowrap;
}

.bar-select {
  padding: 4px 8px;
  border: 1px solid #504945;
  border-radius: 6px;
  background: #3c3836;
  color: #d5c4a1;
  font-size: 0.85rem;
  cursor: pointer;
}
.bar-select:focus { outline: none; border-color: #83a598; }

.bar-spacer { flex: 1; }

.sort-dir-btn {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
  border: 1px solid #504945;
  background: #32302f;
  color: #d5c4a1;
  white-space: nowrap;
}
.sort-dir-btn:hover { border-color: #665c54; }

/* Pagination */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 1rem 0 0.5rem;
  flex-wrap: wrap;
}

.pg-btn {
  min-width: 32px;
  height: 32px;
  padding: 0 8px;
  border-radius: 6px;
  border: 1px solid #504945;
  background: #3c3836;
  color: #d5c4a1;
  font-size: 0.85rem;
  cursor: pointer;
  transition: background 0.1s;
}
.pg-btn:hover:not(:disabled) { background: #504945; }
.pg-btn:disabled { opacity: 0.35; cursor: default; }
.pg-btn--active {
  background: #458588;
  border-color: #458588;
  color: #ebdbb2;
  font-weight: 600;
}

.pg-ellipsis {
  color: #7c6f64;
  padding: 0 4px;
  user-select: none;
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

