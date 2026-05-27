<script setup lang="ts">
import {ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import api from '@/api'


const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || 'null')
if (!user || (user.role !== 'MODERATOR' && user.role !== 'ADMIN')) router.push('/books')

// ── Active tab ────────────────────────────────────────────────────────────────
const activeTab = ref<'books' | 'editBook' | 'authors' | 'series' | 'reports' | 'readers'>('books')

// ── Shared data ───────────────────────────────────────────────────────────────
const authors = ref<any[]>([])
const publishers = ref<any[]>([])
const seriesList = ref<any[]>([])
const genres = ref<any[]>([])

onMounted(async () => {
  document.title = (user?.role === 'ADMIN' ? 'Admin Panel' : 'Moderator Panel') + ' — BookVault'
  const [aRes, pRes, sRes, gRes] = await Promise.all([
    api.get('/authors'), api.get('/publishers'), api.get('/series'), api.get('/genres'),
  ])
  authors.value = aRes.data
  publishers.value = pRes.data
  seriesList.value = sRes.data
  genres.value = gRes.data
  await loadReports()
})

// ── Add Book ──────────────────────────────────────────────────────────────────
const newBook = ref({
  title: '', authorId: null as number | null, publisherId: null as number | null,
  seriesId: null as number | null, publicationYear: null as number | null,
  pageCount: null as number | null, mood: '', genreIds: [] as number[]
})
const bookMsg = ref({text: '', ok: true})

async function addBook() {
  bookMsg.value = {text: '', ok: true}
  if (!newBook.value.title.trim()) {
    bookMsg.value = {text: 'Title is required', ok: false};
    return
  }
  try {
    const p: any = {...newBook.value}
    Object.keys(p).forEach(k => {
      if (p[k] === null || p[k] === '') delete p[k]
    })
    await api.post('/moderator/books', p)
    bookMsg.value = {text: `Book "${newBook.value.title}" added!`, ok: true}
    newBook.value = {
      title: '', authorId: null, publisherId: null, seriesId: null,
      publicationYear: null, pageCount: null, mood: '', genreIds: []
    }
  } catch (e: any) {
    bookMsg.value = {text: e.response?.data ?? 'Failed', ok: false}
  }
}

function toggleGenre(arr: number[], id: number) {
  const i = arr.indexOf(id);
  if (i === -1) arr.push(id); else arr.splice(i, 1)
}

// ── Edit Book ─────────────────────────────────────────────────────────────────
const allBooks = ref<any[]>([])
const selectedBookId = ref<number | null>(null)
const editBook = ref<any>(null)
const editBookMsg = ref({text: '', ok: true})

async function loadBooksForEdit() {
  const res = await api.get('/books')
  allBooks.value = res.data
}

function selectBookToEdit(id: number) {
  const b = allBooks.value.find(x => x.id === id)
  if (!b) return
  editBook.value = {
    title: b.title, mood: b.mood ?? '', publicationYear: b.publicationYear ?? null,
    pageCount: b.pageCount ?? null, authorId: b.author?.id ?? null,
    publisherId: b.publisher?.id ?? null, seriesId: b.series?.id ?? null,
    genreIds: b.genres?.map((g: any) => g.id) ?? []
  }
}

async function saveEditBook() {
  editBookMsg.value = {text: '', ok: true}
  try {
    const p: any = {...editBook.value}
    Object.keys(p).forEach(k => {
      if (p[k] === '') p[k] = null
    })
    await api.put(`/moderator/books/${selectedBookId.value}`, p)
    editBookMsg.value = {text: 'Book updated!', ok: true}
    await loadBooksForEdit()
  } catch (e: any) {
    editBookMsg.value = {text: e.response?.data ?? 'Failed', ok: false}
  }
}

// ── Edit Author ───────────────────────────────────────────────────────────────
const selectedAuthorId = ref<number | null>(null)
const editAuthor = ref<any>(null)
const authorMsg = ref({text: '', ok: true})

function selectAuthorToEdit(id: number) {
  const a = authors.value.find(x => x.id === id)
  if (!a) return
  editAuthor.value = {
    firstName: a.firstName, lastName: a.lastName,
    nationality: a.nationality ?? '', biography: a.biography ?? ''
  }
}

async function saveEditAuthor() {
  authorMsg.value = {text: '', ok: true}
  try {
    await api.put(`/moderator/authors/${selectedAuthorId.value}`, editAuthor.value)
    authorMsg.value = {text: 'Author updated!', ok: true}
    const res = await api.get('/authors')
    authors.value = res.data
  } catch (e: any) {
    authorMsg.value = {text: e.response?.data ?? 'Failed', ok: false}
  }
}

// ── Series ────────────────────────────────────────────────────────────────────
const newSeries = ref({name: '', volumeCount: null as number | null, authorId: null as number | null})
const selectedSeriesId = ref<number | null>(null)
const editSeries = ref<any>(null)
const seriesMsg = ref({text: '', ok: true})

async function addSeries() {
  seriesMsg.value = {text: '', ok: true}
  if (!newSeries.value.name.trim()) {
    seriesMsg.value = {text: 'Name required', ok: false};
    return
  }
  try {
    const p: any = {...newSeries.value}
    Object.keys(p).forEach(k => {
      if (p[k] === null || p[k] === '') delete p[k]
    })
    await api.post('/moderator/series', p)
    seriesMsg.value = {text: `Series "${newSeries.value.name}" added!`, ok: true}
    newSeries.value = {name: '', volumeCount: null, authorId: null}
    const res = await api.get('/series');
    seriesList.value = res.data
  } catch (e: any) {
    seriesMsg.value = {text: e.response?.data ?? 'Failed', ok: false}
  }
}

function selectSeriestoEdit(id: number) {
  const s = seriesList.value.find(x => x.id === id)
  if (!s) return
  editSeries.value = {name: s.name, volumeCount: s.volumeCount ?? null, authorId: s.author?.id ?? null}
}

async function saveEditSeries() {
  seriesMsg.value = {text: '', ok: true}
  try {
    const p: any = {...editSeries.value}
    Object.keys(p).forEach(k => {
      if (p[k] === null || p[k] === '') delete p[k]
    })
    await api.put(`/moderator/series/${selectedSeriesId.value}`, p)
    seriesMsg.value = {text: 'Series updated!', ok: true}
    const res = await api.get('/series');
    seriesList.value = res.data
  } catch (e: any) {
    seriesMsg.value = {text: e.response?.data ?? 'Failed', ok: false}
  }
}

// ── Reports ───────────────────────────────────────────────────────────────────
const reports = ref<any[]>([])
const reportsFilter = ref('pending')

// ── Readers ───────────────────────────────────────────────────────────────────
const readers = ref<any[]>([])
const readerSearch = ref('')
const isAdmin = user?.role === 'ADMIN'

// Ban modal
const banTarget = ref<any>(null)
const banDays = ref(7)
const readerMsg = ref({ text: '', ok: true })

function isBanned(r: any): boolean {
  if (!r.bannedUntil) return false
  return new Date(r.bannedUntil) >= new Date(new Date().toDateString())
}

async function loadReaders() {
  const params: any = {}
  if (readerSearch.value.trim()) params.username = readerSearch.value.trim()
  // Admin gets full list with bannedUntil; moderator uses restricted endpoint
  const endpoint = isAdmin ? '/admin/readers' : '/readers'
  const res = await api.get(endpoint, { params: isAdmin ? {} : params })
  readers.value = isAdmin && readerSearch.value.trim()
    ? res.data.filter((r: any) => r.username?.toLowerCase().includes(readerSearch.value.toLowerCase()))
    : res.data
}

async function setRole(r: any, role: string) {
  try {
    const res = await api.put(`/admin/readers/${r.id}/role`, { role })
    r.role = res.data.role
    showReaderMsg(`${r.username}'s role set to ${role}`, true)
  } catch (e: any) {
    showReaderMsg(e.response?.data ?? 'Failed', false)
  }
}

async function confirmBan() {
  if (!banTarget.value) return
  try {
    const res = await api.put(`/admin/readers/${banTarget.value.id}/ban`, { days: banDays.value })
    banTarget.value.bannedUntil = res.data.bannedUntil || null
    showReaderMsg(res.data.message, true)
  } catch (e: any) {
    showReaderMsg(e.response?.data ?? 'Failed', false)
  } finally {
    banTarget.value = null
  }
}

async function unbanReader(r: any) {
  try {
    const res = await api.put(`/admin/readers/${r.id}/ban`, { days: 0 })
    r.bannedUntil = null
    showReaderMsg(res.data.message, true)
  } catch (e: any) {
    showReaderMsg(e.response?.data ?? 'Failed', false)
  }
}

async function deleteReader(r: any) {
  if (!confirm(`Permanently delete "${r.username}"? This cannot be undone.`)) return
  try {
    await api.delete(`/admin/readers/${r.id}`)
    readers.value = readers.value.filter(x => x.id !== r.id)
    showReaderMsg(`Account "${r.username}" deleted`, true)
  } catch (e: any) {
    showReaderMsg(e.response?.data ?? 'Failed', false)
  }
}

function showReaderMsg(text: string, ok: boolean) {
  readerMsg.value = { text, ok }
  setTimeout(() => { readerMsg.value.text = '' }, 4000)
}

async function loadReports() {
  const res = await api.get(`/moderator/reports?status=${reportsFilter.value}`)
  reports.value = res.data
}

async function resolveReport(id: number) {
  await api.put(`/moderator/reports/${id}/resolve`);
  await loadReports()
}

async function dismissReport(id: number) {
  await api.put(`/moderator/reports/${id}/dismiss`);
  await loadReports()
}

async function deleteReview(reviewId: number) {
  if (!confirm('Delete this review? All its reports will be closed.')) return
  await api.delete(`/moderator/reviews/${reviewId}`);
  await loadReports()
}
</script>

<template>
  <div class="mod-panel">
    <h1 :class="isAdmin ? 'title--admin' : ''">
      <span aria-hidden="true">{{ isAdmin ? '🛡' : '🛡️' }}</span>
      {{ isAdmin ? 'Admin Panel' : 'Moderator Panel' }}
    </h1>

    <!-- Tabs -->
    <div class="tab-bar" role="tablist" aria-label="Moderator sections">
      <button v-for="tab in [
          {key:'books',    label:'Add Book',    emoji:'📚'},
          {key:'editBook', label:'Edit Book',   emoji:'✏️'},
          {key:'authors',  label:'Edit Author', emoji:'✍️'},
          {key:'series',   label:'Series',      emoji:'📖'},
          {key:'reports',  label:'Reports',     emoji:'🚨'},
          {key:'readers',  label:'Readers',     emoji:'👥'},
        ]" :key="tab.key"
              @click="activeTab = tab.key as any; tab.key === 'editBook' && loadBooksForEdit(); tab.key === 'readers' && loadReaders()"
              class="tab-btn"
              :class="{ 'tab-btn--active': activeTab === tab.key }"
              role="tab"
              :aria-selected="activeTab === tab.key"
              :aria-controls="`mod-panel-${tab.key}`"
              :id="`mod-tab-${tab.key}`"
      >
        <span aria-hidden="true">{{ tab.emoji }}</span> {{ tab.label }}
      </button>
    </div>

    <!-- ── Add Book tab ──────────────────────────────────────────────────── -->
    <section v-if="activeTab === 'books'" id="mod-panel-books" role="tabpanel" aria-labelledby="mod-tab-books">
      <h2><span aria-hidden="true">📚</span> Add New Book</h2>
      <div class="form-grid">
        <div>
          <label for="new-book-title">Title *</label>
          <input id="new-book-title" v-model="newBook.title" placeholder="Book title" class="form-input"/>
        </div>
        <div>
          <label for="new-book-mood">Mood</label>
          <input id="new-book-mood" v-model="newBook.mood" placeholder="e.g. dark" class="form-input"/>
        </div>
        <div>
          <label for="new-book-author">Author</label>
          <select id="new-book-author" v-model="newBook.authorId" class="form-select">
            <option :value="null">-- none --</option>
            <option v-for="a in authors" :key="a.id" :value="a.id">{{ a.firstName }} {{ a.lastName }}</option>
          </select>
        </div>
        <div>
          <label for="new-book-publisher">Publisher</label>
          <select id="new-book-publisher" v-model="newBook.publisherId" class="form-select">
            <option :value="null">-- none --</option>
            <option v-for="p in publishers" :key="p.id" :value="p.id">{{ p.name }}</option>
          </select>
        </div>
        <div>
          <label for="new-book-series">Series</label>
          <select id="new-book-series" v-model="newBook.seriesId" class="form-select">
            <option :value="null">-- none --</option>
            <option v-for="s in seriesList" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
        </div>
        <div>
          <label for="new-book-year">Publication Year</label>
          <input id="new-book-year" v-model.number="newBook.publicationYear" type="number" class="form-input"/>
        </div>
        <div>
          <label for="new-book-pages">Page Count</label>
          <input id="new-book-pages" v-model.number="newBook.pageCount" type="number" class="form-input"/>
        </div>
      </div>
      <fieldset class="genres-row">
        <legend>Genres</legend>
        <span v-for="g in genres" :key="g.id" class="genre-check">
          <label>
            <input type="checkbox" :checked="newBook.genreIds.includes(g.id)"
                   @change="toggleGenre(newBook.genreIds, g.id)"
                   :aria-label="g.name"/> {{ g.name }}
          </label>
        </span>
      </fieldset>
      <p
        v-if="bookMsg.text"
        :class="bookMsg.ok ? 'mod-msg--ok' : 'mod-msg--error'"
        role="status"
        aria-live="polite"
      >{{ bookMsg.text }}</p>
      <button @click="addBook" class="btn-submit">Add Book</button>
    </section>

    <!-- ── Edit Book tab ─────────────────────────────────────────────────── -->
    <section v-if="activeTab === 'editBook'" id="mod-panel-editBook" role="tabpanel" aria-labelledby="mod-tab-editBook">
      <h2><span aria-hidden="true">✏️</span> Edit Book</h2>
      <div style="margin-bottom:1rem;">
        <label for="edit-book-select">Select book to edit:</label>
        <select id="edit-book-select" v-model="selectedBookId" @change="selectBookToEdit(selectedBookId!)" class="form-select--full">
          <option :value="null">-- choose a book --</option>
          <option v-for="b in allBooks" :key="b.id" :value="b.id">{{ b.title }}</option>
        </select>
      </div>
      <div v-if="editBook" class="form-grid">
        <div>
          <label for="edit-book-title">Title</label>
          <input id="edit-book-title" v-model="editBook.title" class="form-input"/>
        </div>
        <div>
          <label for="edit-book-mood">Mood</label>
          <input id="edit-book-mood" v-model="editBook.mood" class="form-input"/>
        </div>
        <div>
          <label for="edit-book-author">Author</label>
          <select id="edit-book-author" v-model="editBook.authorId" class="form-select">
            <option :value="null">-- none --</option>
            <option v-for="a in authors" :key="a.id" :value="a.id">{{ a.firstName }} {{ a.lastName }}</option>
          </select>
        </div>
        <div>
          <label for="edit-book-publisher">Publisher</label>
          <select id="edit-book-publisher" v-model="editBook.publisherId" class="form-select">
            <option :value="null">-- none --</option>
            <option v-for="p in publishers" :key="p.id" :value="p.id">{{ p.name }}</option>
          </select>
        </div>
        <div>
          <label for="edit-book-series">Series</label>
          <select id="edit-book-series" v-model="editBook.seriesId" class="form-select">
            <option :value="null">-- none --</option>
            <option v-for="s in seriesList" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
        </div>
        <div>
          <label for="edit-book-year">Publication Year</label>
          <input id="edit-book-year" v-model.number="editBook.publicationYear" type="number" class="form-input"/>
        </div>
        <div>
          <label for="edit-book-pages">Page Count</label>
          <input id="edit-book-pages" v-model.number="editBook.pageCount" type="number" class="form-input"/>
        </div>
      </div>
      <fieldset v-if="editBook" class="genres-row">
        <legend>Genres</legend>
        <span v-for="g in genres" :key="g.id" class="genre-check">
          <label>
            <input type="checkbox" :checked="editBook.genreIds.includes(g.id)"
                   @change="toggleGenre(editBook.genreIds, g.id)"
                   :aria-label="g.name"/> {{ g.name }}
          </label>
        </span>
      </fieldset>
      <p
        v-if="editBookMsg.text"
        :class="editBookMsg.ok ? 'mod-msg--ok' : 'mod-msg--error'"
        role="status"
        aria-live="polite"
      >{{ editBookMsg.text }}</p>
      <button v-if="editBook" @click="saveEditBook" class="btn-submit">Save Changes</button>
    </section>

    <!-- ── Edit Author tab ───────────────────────────────────────────────── -->
    <section v-if="activeTab === 'authors'" id="mod-panel-authors" role="tabpanel" aria-labelledby="mod-tab-authors">
      <h2><span aria-hidden="true">✍️</span> Edit Author</h2>
      <div style="margin-bottom:1rem;">
        <label for="edit-author-select">Select author:</label>
        <select id="edit-author-select" v-model="selectedAuthorId" @change="selectAuthorToEdit(selectedAuthorId!)" class="form-select--full">
          <option :value="null">-- choose an author --</option>
          <option v-for="a in authors" :key="a.id" :value="a.id">{{ a.firstName }} {{ a.lastName }}</option>
        </select>
      </div>
      <div v-if="editAuthor" class="form-grid">
        <div>
          <label for="edit-author-first">First Name</label>
          <input id="edit-author-first" v-model="editAuthor.firstName" class="form-input"/>
        </div>
        <div>
          <label for="edit-author-last">Last Name</label>
          <input id="edit-author-last" v-model="editAuthor.lastName" class="form-input"/>
        </div>
        <div>
          <label for="edit-author-nationality">Nationality</label>
          <input id="edit-author-nationality" v-model="editAuthor.nationality" class="form-input"/>
        </div>
      </div>
      <div v-if="editAuthor" style="margin-top:1rem;">
        <label for="edit-author-bio">Biography</label>
        <textarea id="edit-author-bio" v-model="editAuthor.biography" rows="6" class="bio-textarea"></textarea>
      </div>
      <p
        v-if="authorMsg.text"
        :class="authorMsg.ok ? 'mod-msg--ok' : 'mod-msg--error'"
        role="status"
        aria-live="polite"
      >{{ authorMsg.text }}</p>
      <button v-if="editAuthor" @click="saveEditAuthor" class="btn-submit">Save Author</button>
    </section>

    <!-- ── Series tab ────────────────────────────────────────────────────── -->
    <section v-if="activeTab === 'series'" id="mod-panel-series" role="tabpanel" aria-labelledby="mod-tab-series">
      <h2><span aria-hidden="true">📖</span> Manage Series</h2>

      <h3>Add New Series</h3>
      <div class="form-grid form-grid--narrow">
        <div>
          <label for="new-series-name">Name *</label>
          <input id="new-series-name" v-model="newSeries.name" class="form-input"/>
        </div>
        <div>
          <label for="new-series-volumes">Volume Count</label>
          <input id="new-series-volumes" v-model.number="newSeries.volumeCount" type="number" class="form-input"/>
        </div>
        <div>
          <label for="new-series-author">Author</label>
          <select id="new-series-author" v-model="newSeries.authorId" class="form-select">
            <option :value="null">-- none --</option>
            <option v-for="a in authors" :key="a.id" :value="a.id">{{ a.firstName }} {{ a.lastName }}</option>
          </select>
        </div>
      </div>
      <button @click="addSeries" class="btn-add-series">Add Series</button>

      <h3 style="margin-top:1.5rem;">Edit Existing Series</h3>
      <div style="margin-bottom:1rem;">
        <label for="edit-series-select" class="visually-hidden">Select series to edit</label>
        <select id="edit-series-select" v-model="selectedSeriesId" @change="selectSeriestoEdit(selectedSeriesId!)"
                class="form-select--full" aria-label="Select series to edit">
          <option :value="null">-- choose a series --</option>
          <option v-for="s in seriesList" :key="s.id" :value="s.id">{{ s.name }}</option>
        </select>
      </div>
      <div v-if="editSeries" class="form-grid form-grid--narrow">
        <div>
          <label for="edit-series-name">Name</label>
          <input id="edit-series-name" v-model="editSeries.name" class="form-input"/>
        </div>
        <div>
          <label for="edit-series-volumes">Volume Count</label>
          <input id="edit-series-volumes" v-model.number="editSeries.volumeCount" type="number" class="form-input"/>
        </div>
        <div>
          <label for="edit-series-author">Author</label>
          <select id="edit-series-author" v-model="editSeries.authorId" class="form-select">
            <option :value="null">-- none --</option>
            <option v-for="a in authors" :key="a.id" :value="a.id">{{ a.firstName }} {{ a.lastName }}</option>
          </select>
        </div>
      </div>
      <p
        v-if="seriesMsg.text"
        :class="seriesMsg.ok ? 'mod-msg--ok' : 'mod-msg--error'"
        role="status"
        aria-live="polite"
      >{{ seriesMsg.text }}</p>
      <button v-if="editSeries" @click="saveEditSeries" class="btn-submit">Save Series</button>
    </section>

    <!-- ── Reports tab ───────────────────────────────────────────────────── -->
    <section v-if="activeTab === 'reports'" id="mod-panel-reports" role="tabpanel" aria-labelledby="mod-tab-reports">
      <h2><span aria-hidden="true">🚨</span> Flagged Reviews</h2>
      <div class="filter-row">
        <label for="reports-filter">Filter:</label>
        <select id="reports-filter" v-model="reportsFilter" @change="loadReports" class="filter-select">
          <option value="pending">Pending</option>
          <option value="resolved">Resolved</option>
          <option value="dismissed">Dismissed</option>
          <option value="">All</option>
        </select>
      </div>
      <p v-if="reports.length === 0" class="no-data">No reports found.</p>
      <table v-else class="mod-table">
        <caption class="visually-hidden">Flagged reviews</caption>
        <thead>
        <tr class="table-head-row">
          <th class="table-th" scope="col">ID</th>
          <th class="table-th" scope="col">Review ID</th>
          <th class="table-th" scope="col">Reporter</th>
          <th class="table-th" scope="col">Reason</th>
          <th class="table-th" scope="col">Status</th>
          <th class="table-th" scope="col">Actions</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="r in reports" :key="r.id">
          <td class="table-td">{{ r.id }}</td>
          <td class="table-td">{{ r.review?.id }}</td>
          <td class="table-td">{{ r.reporterType }} #{{ r.reporterId }}</td>
          <td class="table-td">{{ r.reason }}</td>
          <td class="table-td">
            <span :style="{color: r.status==='pending'?'#fabd2f':r.status==='resolved'?'#b8bb26':'#7c6f64'}">{{
                r.status
              }}</span>
          </td>
          <td class="table-td--nowrap">
            <button v-if="r.status==='pending'" @click="resolveReport(r.id)" class="btn-resolve"
                    :aria-label="`Resolve report #${r.id}`">
              <span aria-hidden="true">✅</span> Resolve
            </button>
            <button v-if="r.status==='pending'" @click="dismissReport(r.id)" class="btn-dismiss"
                    :aria-label="`Dismiss report #${r.id}`">
              <span aria-hidden="true">❌</span> Dismiss
            </button>
            <button @click="deleteReview(r.review?.id)" class="btn-delete-review"
                    :aria-label="`Delete review #${r.review?.id}`">
              <span aria-hidden="true">🗑️</span> Delete Review
            </button>
          </td>
        </tr>
        </tbody>
      </table>
    </section>

    <!-- ── Readers tab ────────────────────────────────────────────────────── -->
    <section v-if="activeTab === 'readers'" id="mod-panel-readers" role="tabpanel" aria-labelledby="mod-tab-readers">
      <h2><span aria-hidden="true">👥</span> Registered Readers</h2>

      <p v-if="readerMsg.text" role="alert" :class="['inline-msg', readerMsg.ok ? 'inline-msg--ok' : 'inline-msg--err']">
        {{ readerMsg.text }}
      </p>

      <div class="readers-search" role="search">
        <label for="reader-search" class="visually-hidden">Search readers by username</label>
        <input id="reader-search" v-model="readerSearch" type="search"
               placeholder="Search by username…" class="readers-search-input"
               aria-label="Search readers by username"/>
        <button @click="loadReaders" class="btn-search">Search</button>
        <button @click="readerSearch=''; loadReaders()" class="btn-clear">Clear</button>
      </div>

      <p v-if="readers.length === 0" class="no-data">No readers found.</p>
      <table v-else class="mod-table">
        <caption class="visually-hidden">Registered readers</caption>
        <thead>
        <tr class="table-head-row">
          <th class="table-th" scope="col">ID</th>
          <th class="table-th" scope="col">Username</th>
          <th class="table-th" scope="col">Email</th>
          <th class="table-th" scope="col">Nationality</th>
          <th class="table-th" scope="col">Role</th>
          <th v-if="isAdmin" class="table-th" scope="col">Banned until</th>
          <th v-if="isAdmin" class="table-th" scope="col">Actions</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="r in readers" :key="r.id" :class="{ 'row-banned': isAdmin && isBanned(r) }">
          <td class="table-td">{{ r.id }}</td>
          <td class="table-td">{{ r.username }}</td>
          <td class="table-td">{{ r.email }}</td>
          <td class="table-td">{{ r.nationality }}</td>
          <td class="table-td">
            <!-- Admin: role dropdown; Moderator: read-only badge -->
            <select v-if="isAdmin && r.role !== 'ADMIN'"
                    :value="r.role"
                    @change="setRole(r, ($event.target as HTMLSelectElement).value)"
                    :aria-label="`Change role for ${r.username}`"
                    class="role-select">
              <option value="USER">USER</option>
              <option value="MODERATOR">MODERATOR</option>
            </select>
            <span v-else :class="r.role === 'MODERATOR' ? 'role--mod' : r.role === 'ADMIN' ? 'role--admin' : 'role--reader'">
              {{ r.role }}
            </span>
          </td>
          <td v-if="isAdmin" class="table-td">
            <span v-if="isBanned(r)" class="role--banned">{{ r.bannedUntil }}</span>
            <span v-else class="role--reader">—</span>
          </td>
          <td v-if="isAdmin" class="table-td actions-cell">
            <template v-if="r.role !== 'ADMIN'">
              <button v-if="!isBanned(r)" class="btn-action btn-ban"
                      @click="banTarget = r; banDays = 7"
                      :aria-label="`Ban ${r.username}`">🚫 Ban</button>
              <button v-else class="btn-action btn-unban"
                      @click="unbanReader(r)"
                      :aria-label="`Unban ${r.username}`">✅ Unban</button>
              <button class="btn-action btn-delete"
                      @click="deleteReader(r)"
                      :aria-label="`Delete ${r.username}`">🗑 Delete</button>
            </template>
            <span v-else class="role--reader">—</span>
          </td>
        </tr>
        </tbody>
      </table>

      <!-- Ban duration modal -->
      <div v-if="banTarget" class="modal-overlay" role="dialog" aria-modal="true" :aria-label="`Ban ${banTarget.username}`">
        <div class="modal-box">
          <h3>Ban "{{ banTarget.username }}"</h3>
          <label for="ban-days-input">Number of days:</label>
          <input id="ban-days-input" v-model.number="banDays" type="number" min="1" max="365"
                 aria-required="true" class="ban-days-input"/>
          <div class="modal-actions">
            <button class="btn-action btn-ban" @click="confirmBan">Confirm Ban</button>
            <button class="btn-clear" @click="banTarget = null">Cancel</button>
          </div>
        </div>
      </div>
    </section>

  </div>
</template>

<style scoped>
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

/* ── fieldset used for genre groups ───────────────────────────── */
fieldset.genres-row {
  border: 1px solid #504945;
  border-radius: 6px;
  padding: 0.75rem 1rem;
}

fieldset.genres-row legend {
  color: #a89984;
  font-size: 0.9rem;
  padding: 0 4px;
}

.mod-panel {
  max-width: 1000px;
  margin: 0 auto;
  padding: 1rem;
}

.mod-panel h1 { color: #fe8019; }
.mod-panel h1.title--admin { color: #a855f7; }

.tab-bar {
  display: flex;
  gap: 4px;
  margin-bottom: 1.5rem;
  border-bottom: 2px solid #504945;
  flex-wrap: wrap;
}

.tab-btn {
  padding: 8px 16px;
  border: none;
  cursor: pointer;
  border-radius: 6px 6px 0 0;
  background: #3c3836;
  color: #d5c4a1;
  font-weight: 400;
  transition: background 0.12s;
}
.tab-btn:hover { background: #504945; }

.tab-btn--active {
  background: #458588;
  color: #ebdbb2;
  font-weight: 600;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-grid--narrow { max-width: 640px; }

.form-input,
.form-select {
  width: 100%;
  padding: 6px;
  background: #32302f;
  color: #ebdbb2;
  border: 1px solid #504945;
  border-radius: 4px;
}
.form-input:focus, .form-select:focus { outline: none; border-color: #83a598; }

.form-select--full {
  width: 100%;
  padding: 6px;
  max-width: 400px;
  background: #32302f;
  color: #ebdbb2;
  border: 1px solid #504945;
  border-radius: 4px;
}

.genres-row { margin-top: 1rem; color: #d5c4a1; }

.genre-check { margin-right: 12px; color: #d5c4a1; }

.mod-msg--ok { color: #b8bb26; }
.mod-msg--error { color: #fb4934; }

.btn-submit {
  padding: 8px 20px;
  background: #458588;
  color: #ebdbb2;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: filter 0.12s;
}
.btn-submit:hover { filter: brightness(1.15); }

.btn-add-series {
  margin-top: 0.75rem;
  padding: 8px 20px;
  background: #98971a;
  color: #ebdbb2;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: filter 0.12s;
}
.btn-add-series:hover { filter: brightness(1.15); }

.filter-row { margin-bottom: 1rem; }

.filter-select {
  padding: 4px 8px;
  background: #32302f;
  color: #ebdbb2;
  border: 1px solid #504945;
  border-radius: 4px;
}

.no-data { color: #a89984; }

.mod-table { width: 100%; border-collapse: collapse; }

.table-head-row {
  background: #3c3836;
  text-align: left;
}

.table-th { padding: 8px; border: 1px solid #504945; color: #a89984; }

.table-td { padding: 8px; border: 1px solid #504945; color: #d5c4a1; }

.table-td--nowrap { padding: 8px; border: 1px solid #504945; white-space: nowrap; }

.btn-resolve {
  margin-right: 4px;
  padding: 4px 10px;
  background: #98971a;
  color: #ebdbb2;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-dismiss {
  margin-right: 4px;
  padding: 4px 10px;
  background: #665c54;
  color: #ebdbb2;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-delete-review {
  padding: 4px 10px;
  background: #cc241d;
  color: #ebdbb2;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.readers-search { display: flex; gap: 8px; margin-bottom: 1rem; }

.readers-search-input {
  padding: 6px;
  flex: 1;
  max-width: 300px;
  background: #32302f;
  color: #ebdbb2;
  border: 1px solid #504945;
  border-radius: 4px;
}

.btn-search {
  padding: 6px 14px;
  background: #458588;
  color: #ebdbb2;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.btn-clear {
  padding: 6px 14px;
  background: #665c54;
  color: #ebdbb2;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.role--mod    { color: #fe8019; font-weight: 600; }
.role--reader { color: #d5c4a1; }
.role--admin  { color: #a855f7; font-weight: 700; }
.role--banned { color: #fb4934; font-weight: 600; }

.row-banned { background: rgba(251, 73, 52, 0.07); }

.role-select {
  background: #3c3836;
  color: #ebdbb2;
  border: 1px solid #665c54;
  border-radius: 4px;
  padding: 2px 6px;
  font-size: 0.85rem;
}

.actions-cell { display: flex; gap: 6px; flex-wrap: wrap; align-items: center; }

.btn-action {
  padding: 3px 10px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 0.82rem;
  font-weight: 600;
  white-space: nowrap;
}
.btn-ban    { background: #f97316; color: #fff; }
.btn-unban  { background: #22c55e; color: #1d2021; }
.btn-delete { background: #ef4444; color: #fff; }

.inline-msg {
  padding: .5rem 1rem;
  border-radius: 6px;
  margin-bottom: .75rem;
  font-size: .9rem;
}
.inline-msg--ok  { background: #1d3a26; color: #8ec07c; }
.inline-msg--err { background: #3a1d1d; color: #fb4934; }

.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,.55);
  display: flex; align-items: center; justify-content: center;
  z-index: 200;
}
.modal-box {
  background: #32302f;
  border: 1px solid #504945;
  border-radius: 10px;
  padding: 1.75rem;
  width: 300px;
  display: flex;
  flex-direction: column;
  gap: .75rem;
}
.modal-box h3 { margin: 0; color: #fabd2f; }
.ban-days-input {
  padding: .4rem .6rem;
  border: 1px solid #665c54;
  border-radius: 6px;
  background: #1d2021;
  color: #ebdbb2;
  width: 100%;
}
.modal-actions { display: flex; gap: .75rem; justify-content: flex-end; }

.bio-textarea {
  width: 100%;
  padding: 6px;
  font-family: inherit;
  resize: vertical;
  background: #32302f;
  color: #ebdbb2;
  border: 1px solid #504945;
  border-radius: 4px;
}
</style>

