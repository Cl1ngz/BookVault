<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import api from '@/api'

const allBooks  = ref<any[]>([])
const allGenres = ref<any[]>([])
const filtersOpen = ref(true)

onMounted(async () => {
  const [booksRes, genresRes] = await Promise.all([api.get('/books'), api.get('/genres')])
  allBooks.value  = booksRes.data
  allGenres.value = genresRes.data
})

// ── Search ────────────────────────────────────────────────────────────────────
const searchQuery = ref('')

// ── Mood ──────────────────────────────────────────────────────────────────────
const MOODS = ['adventurous','challenging','dark','emotional','funny','hopeful',
               'informative','inspiring','lighthearted','mysterious','reflective',
               'relaxing','sad','tense']
const selectedMoods = ref<string[]>([])
const moodMode      = ref<'any'|'all'>('any')

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
const FICTION_GENRES = new Set(['Fantasy','Science Fiction','Romance','Horror','Thriller',
  'Mystery','Crime','Historical Fiction','Adventure','Literary Fiction','Contemporary Fiction',
  'Magical Realism','Dystopian','Speculative Fiction','Paranormal','Urban Fantasy','Epic Fantasy',
  'Dark Fantasy','Space Opera','Cyberpunk','Steampunk','Alternate History','Satire','Humor',
  'Drama','Coming of Age',"Women's Fiction",'Chick Lit','Fairy Tale','Mythology',
  'Short Stories','Anthology','Young Adult','Middle Grade',"Children's",'Picture Book',
  'Graphic Novel','Manga','Poetry','Play / Drama'])
const selectedTypes = ref<string[]>([])
function getType(book: any): 'Fiction'|'Nonfiction'|null {
  const names: string[] = book.genres?.map((g:any) => g.name) ?? []
  if (!names.length) return null
  return names.filter(n => FICTION_GENRES.has(n)).length > names.length / 2 ? 'Fiction' : 'Nonfiction'
}

// ── Genres ────────────────────────────────────────────────────────────────────
const includeGenres = ref<string[]>([])
const excludeGenres = ref<string[]>([])
const includeMode   = ref<'any'|'all'>('any')

function toggleInclude(name: string) {
  const ei = excludeGenres.value.indexOf(name); if (ei !== -1) excludeGenres.value.splice(ei, 1)
  const ii = includeGenres.value.indexOf(name)
  if (ii === -1) includeGenres.value.push(name); else includeGenres.value.splice(ii, 1)
}
function toggleExclude(name: string) {
  const ii = includeGenres.value.indexOf(name); if (ii !== -1) includeGenres.value.splice(ii, 1)
  const ei = excludeGenres.value.indexOf(name)
  if (ei === -1) excludeGenres.value.push(name); else excludeGenres.value.splice(ei, 1)
}

// ── Pages ─────────────────────────────────────────────────────────────────────
const selectedPageBuckets = ref<string[]>([])

// ── Year ──────────────────────────────────────────────────────────────────────
const yearFrom = ref<number|null>(null)
const yearTo   = ref<number|null>(null)

// ── Series ────────────────────────────────────────────────────────────────────
const standaloneOnly = ref(false)

// ── Active filter count ───────────────────────────────────────────────────────
const activeFilterCount = computed(() =>
  [selectedMoods.value.length > 0, selectedPaces.value.length > 0,
   selectedTypes.value.length > 0, includeGenres.value.length > 0,
   excludeGenres.value.length > 0, selectedPageBuckets.value.length > 0,
   !!(yearFrom.value || yearTo.value), standaloneOnly.value
  ].filter(Boolean).length
)

// ── Filter logic ──────────────────────────────────────────────────────────────
const filteredBooks = computed(() => allBooks.value.filter(book => {
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
    const bg: string[] = book.genres?.map((g:any) => g.name) ?? []
    if (includeMode.value === 'any' ? !includeGenres.value.some(g => bg.includes(g))
                                    : !includeGenres.value.every(g => bg.includes(g))) return false
  }
  if (excludeGenres.value.length) {
    const bg: string[] = book.genres?.map((g:any) => g.name) ?? []
    if (excludeGenres.value.some(g => bg.includes(g))) return false
  }
  if (selectedPageBuckets.value.length) {
    const p = book.pageCount ?? 0
    if (!selectedPageBuckets.value.some(b =>
      b === '<300' ? p < 300 : b === '300-499' ? p >= 300 && p < 500 : p >= 500)) return false
  }
  if (yearFrom.value && (book.publicationYear ?? 0) < yearFrom.value) return false
  if (yearTo.value   && (book.publicationYear ?? 9999) > yearTo.value) return false
  if (standaloneOnly.value && book.series) return false
  return true
}))

function clearAll() {
  searchQuery.value = ''; selectedMoods.value = []; moodMode.value = 'any'
  selectedPaces.value = []; selectedTypes.value = []
  includeGenres.value = []; excludeGenres.value = []; includeMode.value = 'any'
  selectedPageBuckets.value = []; yearFrom.value = null; yearTo.value = null
  standaloneOnly.value = false
}

function toggle(arr: string[], val: string) {
  const i = arr.indexOf(val); if (i === -1) arr.push(val); else arr.splice(i, 1)
}
</script>

<template>
  <div style="max-width:1100px; margin:0 auto; padding:1rem;">

    <!-- Search bar -->
    <div style="display:flex; gap:8px; margin-bottom:1rem;">
      <input v-model="searchQuery" placeholder="Search by title, author or series…"
             style="flex:1; padding:10px 14px; font-size:1rem; border:1px solid #d1d5db; border-radius:8px;"/>
      <button @click="filtersOpen = !filtersOpen"
              :style="{padding:'10px 16px', border:'1px solid #d1d5db', borderRadius:'8px',
                       background: filtersOpen ? '#2563eb' : '#fff',
                       color: filtersOpen ? 'white' : '#374151', cursor:'pointer', fontWeight:'500'}">
        🔍 Filters{{ activeFilterCount ? ` (${activeFilterCount})` : '' }}
      </button>
      <button v-if="activeFilterCount" @click="clearAll"
              style="padding:10px 14px; border:1px solid #fca5a5; border-radius:8px; background:#fef2f2; color:#dc2626; cursor:pointer;">
        ✕ Clear all
      </button>
    </div>

    <!-- Filter panel -->
    <div v-show="filtersOpen"
         style="border:1px solid #e5e7eb; border-radius:10px; padding:1.25rem; margin-bottom:1.5rem; background:#fafafa; display:grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap:1.25rem;">

      <!-- Mood -->
      <div>
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
          <strong>🎭 Mood</strong>
          <span style="font-size:0.8rem; color:#6b7280;">
            <label><input type="radio" v-model="moodMode" value="any"/> any</label>
            <label style="margin-left:8px;"><input type="radio" v-model="moodMode" value="all"/> all</label>
          </span>
        </div>
        <div style="display:flex; flex-wrap:wrap; gap:5px;">
          <button v-for="m in MOODS" :key="m" @click="toggle(selectedMoods, m)"
                  :style="{padding:'3px 10px', borderRadius:'999px', fontSize:'0.82rem', cursor:'pointer', border:'1px solid',
                           background: selectedMoods.includes(m) ? '#2563eb' : '#fff',
                           color: selectedMoods.includes(m) ? 'white' : '#374151',
                           borderColor: selectedMoods.includes(m) ? '#2563eb' : '#d1d5db'}">
            {{ m }}
          </button>
        </div>
      </div>

      <!-- Pace + Type -->
      <div>
        <div style="margin-bottom:1rem;">
          <strong>⚡ Pace</strong>
          <div style="display:flex; gap:6px; margin-top:6px;">
            <button v-for="p in ['Slow','Medium','Fast']" :key="p" @click="toggle(selectedPaces, p)"
                    :style="{padding:'4px 14px', borderRadius:'6px', cursor:'pointer', border:'1px solid', fontSize:'0.9rem',
                             background: selectedPaces.includes(p) ? '#7c3aed' : '#fff',
                             color: selectedPaces.includes(p) ? 'white' : '#374151',
                             borderColor: selectedPaces.includes(p) ? '#7c3aed' : '#d1d5db'}">
              {{ p }}
            </button>
          </div>
          <p style="font-size:0.75rem; color:#9ca3af; margin:4px 0 0;">Slow ≥500p · Medium 300–499p · Fast &lt;300p</p>
        </div>
        <div>
          <strong>📂 Type</strong>
          <div style="display:flex; gap:6px; margin-top:6px;">
            <button v-for="t in ['Fiction','Nonfiction']" :key="t" @click="toggle(selectedTypes, t)"
                    :style="{padding:'4px 14px', borderRadius:'6px', cursor:'pointer', border:'1px solid', fontSize:'0.9rem',
                             background: selectedTypes.includes(t) ? '#059669' : '#fff',
                             color: selectedTypes.includes(t) ? 'white' : '#374151',
                             borderColor: selectedTypes.includes(t) ? '#059669' : '#d1d5db'}">
              {{ t }}
            </button>
          </div>
        </div>
      </div>

      <!-- Genres -->
      <div>
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px;">
          <strong>🏷️ Genres</strong>
          <span style="font-size:0.8rem; color:#6b7280;">
            Include:
            <label><input type="radio" v-model="includeMode" value="any"/> any</label>
            <label style="margin-left:6px;"><input type="radio" v-model="includeMode" value="all"/> all</label>
          </span>
        </div>
        <div style="max-height:180px; overflow-y:auto; display:flex; flex-direction:column; gap:2px;">
          <div v-for="g in allGenres" :key="g.id" style="display:flex; align-items:center; gap:6px; font-size:0.85rem;">
            <button @click="toggleInclude(g.name)"
                    :style="{padding:'1px 8px', borderRadius:'4px', cursor:'pointer', border:'1px solid', fontSize:'0.78rem',
                             background: includeGenres.includes(g.name) ? '#2563eb' : '#fff',
                             color: includeGenres.includes(g.name) ? 'white' : '#374151',
                             borderColor: includeGenres.includes(g.name) ? '#2563eb' : '#d1d5db'}">+</button>
            <button @click="toggleExclude(g.name)"
                    :style="{padding:'1px 8px', borderRadius:'4px', cursor:'pointer', border:'1px solid', fontSize:'0.78rem',
                             background: excludeGenres.includes(g.name) ? '#dc2626' : '#fff',
                             color: excludeGenres.includes(g.name) ? 'white' : '#374151',
                             borderColor: excludeGenres.includes(g.name) ? '#dc2626' : '#d1d5db'}">−</button>
            <span :style="{color: includeGenres.includes(g.name) ? '#2563eb' : excludeGenres.includes(g.name) ? '#dc2626' : '#374151'}">
              {{ g.name }}
            </span>
          </div>
        </div>
      </div>

      <!-- Pages + Year + Standalone -->
      <div>
        <div style="margin-bottom:1rem;">
          <strong>📄 Pages</strong>
          <div style="display:flex; gap:6px; margin-top:6px;">
            <button v-for="[key, label] in ([['<300','< 300'],['300-499','300–499'],['500+','500+']] as [string,string][])" :key="key"
                    @click="toggle(selectedPageBuckets, key)"
                    :style="{padding:'4px 12px', borderRadius:'6px', cursor:'pointer', border:'1px solid', fontSize:'0.85rem',
                             background: selectedPageBuckets.includes(key) ? '#d97706' : '#fff',
                             color: selectedPageBuckets.includes(key) ? 'white' : '#374151',
                             borderColor: selectedPageBuckets.includes(key) ? '#d97706' : '#d1d5db'}">
              {{ label }}
            </button>
          </div>
        </div>

        <div style="margin-bottom:1rem;">
          <strong>📅 Publication Year</strong>
          <div style="display:flex; gap:8px; margin-top:6px; align-items:center;">
            <input v-model.number="yearFrom" type="number" placeholder="From" min="1000" max="2100"
                   style="width:80px; padding:4px 6px; border:1px solid #d1d5db; border-radius:6px; font-size:0.9rem;"/>
            <span style="color:#9ca3af;">—</span>
            <input v-model.number="yearTo" type="number" placeholder="To" min="1000" max="2100"
                   style="width:80px; padding:4px 6px; border:1px solid #d1d5db; border-radius:6px; font-size:0.9rem;"/>
          </div>
        </div>

        <div>
          <strong>📌 Other</strong>
          <div style="margin-top:6px;">
            <label style="display:flex; align-items:center; gap:6px; cursor:pointer; font-size:0.9rem;">
              <input type="checkbox" v-model="standaloneOnly"/>
              Not part of a series
            </label>
          </div>
        </div>
      </div>

    </div>

    <!-- Results -->
    <p style="color:#6b7280; margin-bottom:0.75rem;">
      <strong style="color:#111;">{{ filteredBooks.length }}</strong> book{{ filteredBooks.length !== 1 ? 's' : '' }} found
      <span v-if="activeFilterCount"> with {{ activeFilterCount }} active filter{{ activeFilterCount !== 1 ? 's' : '' }}</span>
    </p>

    <ul style="list-style:none; padding:0; margin:0;">
      <li v-for="book in filteredBooks" :key="book.id"
          style="padding:10px 12px; border:1px solid #e5e7eb; border-radius:8px; margin-bottom:8px; display:flex; gap:12px; align-items:flex-start;">
        <div style="flex:1;">
          <RouterLink :to="`/books/${book.id}`" style="font-size:1.05rem;"><strong>{{ book.title }}</strong></RouterLink>
          <span v-if="book.author" style="color:#6b7280;">
            — <RouterLink :to="`/authors/${book.author.id}`">{{ book.author.firstName }} {{ book.author.lastName }}</RouterLink>
          </span>
          <span v-if="book.series" style="color:#6b7280;">
            · 📚 <RouterLink :to="`/series/${book.series.id}`">{{ book.series.name }}</RouterLink>
          </span>
          <br/>
          <small style="color:#9ca3af;">
            <span v-if="book.genres?.length">{{ book.genres.map((g:any) => g.name).join(', ') }}</span>
            <span v-if="book.mood"> · 🎭 {{ book.mood }}</span>
            <span v-if="book.pageCount"> · {{ book.pageCount }}p</span>
            <span v-if="book.publicationYear"> · {{ book.publicationYear }}</span>
          </small>
        </div>
      </li>
    </ul>

    <p v-if="filteredBooks.length === 0 && allBooks.length > 0" style="color:#6b7280; text-align:center; padding:2rem;">
      No books match your filters. <button @click="clearAll" style="color:#2563eb; background:none; border:none; cursor:pointer; text-decoration:underline;">Clear all filters</button>
    </p>
  </div>
</template>
