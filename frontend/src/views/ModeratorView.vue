<script setup lang="ts">
import {ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import api from '@/api'


const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || 'null')
if (!user || user.role !== 'MODERATOR') router.push('/books')

// ── Active tab ────────────────────────────────────────────────────────────────
const activeTab = ref<'books' | 'editBook' | 'authors' | 'series' | 'reports' | 'readers'>('books')

// ── Shared data ───────────────────────────────────────────────────────────────
const authors = ref<any[]>([])
const publishers = ref<any[]>([])
const seriesList = ref<any[]>([])
const genres = ref<any[]>([])

onMounted(async () => {
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

async function loadReaders() {
  const params: any = {}
  if (readerSearch.value.trim()) params.username = readerSearch.value.trim()
  const res = await api.get('/readers', {params})
  readers.value = res.data
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
    <h1>🛡️ Moderator Panel</h1>

    <!-- Tabs -->
    <div class="tab-bar">
      <button v-for="tab in [
          {key:'books',    label:'📚 Add Book'},
          {key:'editBook', label:'✏️ Edit Book'},
          {key:'authors',  label:'✍️ Edit Author'},
          {key:'series',   label:'📖 Series'},
          {key:'reports',  label:'🚨 Reports'},
          {key:'readers',  label:'👥 Readers'},
        ]" :key="tab.key"
              @click="activeTab = tab.key as any; tab.key === 'editBook' && loadBooksForEdit(); tab.key === 'readers' && loadReaders()"
              class="tab-btn"
              :class="{ 'tab-btn--active': activeTab === tab.key }">
        {{ tab.label }}
      </button>
    </div>

    <!-- ── Add Book tab ──────────────────────────────────────────────────── -->
    <section v-if="activeTab === 'books'">
      <h2>📚 Add New Book</h2>
      <div class="form-grid">
        <div><label>Title *</label><br/><input v-model="newBook.title" placeholder="Book title" class="form-input"/>
        </div>
        <div><label>Mood</label><br/><input v-model="newBook.mood" placeholder="e.g. dark" class="form-input"/></div>
        <div><label>Author</label><br/>
          <select v-model="newBook.authorId" class="form-select">
            <option :value="null">-- none --</option>
            <option v-for="a in authors" :key="a.id" :value="a.id">{{ a.firstName }} {{ a.lastName }}</option>
          </select>
        </div>
        <div><label>Publisher</label><br/>
          <select v-model="newBook.publisherId" class="form-select">
            <option :value="null">-- none --</option>
            <option v-for="p in publishers" :key="p.id" :value="p.id">{{ p.name }}</option>
          </select>
        </div>
        <div><label>Series</label><br/>
          <select v-model="newBook.seriesId" class="form-select">
            <option :value="null">-- none --</option>
            <option v-for="s in seriesList" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
        </div>
        <div><label>Publication Year</label><br/><input v-model.number="newBook.publicationYear" type="number"
                                                        class="form-input"/></div>
        <div><label>Page Count</label><br/><input v-model.number="newBook.pageCount" type="number" class="form-input"/>
        </div>
      </div>
      <div class="genres-row"><label>Genres</label><br/>
        <span v-for="g in genres" :key="g.id" class="genre-check">
          <label><input type="checkbox" :checked="newBook.genreIds.includes(g.id)"
                        @change="toggleGenre(newBook.genreIds, g.id)"/> {{ g.name }}</label>
        </span>
      </div>
      <p :class="bookMsg.ok ? 'mod-msg--ok' : 'mod-msg--error'">{{ bookMsg.text }}</p>
      <button @click="addBook" class="btn-submit">Add Book</button>
    </section>

    <!-- ── Edit Book tab ─────────────────────────────────────────────────── -->
    <section v-if="activeTab === 'editBook'">
      <h2>✏️ Edit Book</h2>
      <div style="margin-bottom:1rem;">
        <label>Select book to edit:</label><br/>
        <select v-model="selectedBookId" @change="selectBookToEdit(selectedBookId!)" class="form-select--full">
          <option :value="null">-- choose a book --</option>
          <option v-for="b in allBooks" :key="b.id" :value="b.id">{{ b.title }}</option>
        </select>
      </div>
      <div v-if="editBook" class="form-grid">
        <div><label>Title</label><br/><input v-model="editBook.title" class="form-input"/></div>
        <div><label>Mood</label><br/><input v-model="editBook.mood" class="form-input"/></div>
        <div><label>Author</label><br/>
          <select v-model="editBook.authorId" class="form-select">
            <option :value="null">-- none --</option>
            <option v-for="a in authors" :key="a.id" :value="a.id">{{ a.firstName }} {{ a.lastName }}</option>
          </select>
        </div>
        <div><label>Publisher</label><br/>
          <select v-model="editBook.publisherId" class="form-select">
            <option :value="null">-- none --</option>
            <option v-for="p in publishers" :key="p.id" :value="p.id">{{ p.name }}</option>
          </select>
        </div>
        <div><label>Series</label><br/>
          <select v-model="editBook.seriesId" class="form-select">
            <option :value="null">-- none --</option>
            <option v-for="s in seriesList" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
        </div>
        <div><label>Publication Year</label><br/><input v-model.number="editBook.publicationYear" type="number"
                                                        class="form-input"/></div>
        <div><label>Page Count</label><br/><input v-model.number="editBook.pageCount" type="number" class="form-input"/>
        </div>
      </div>
      <div v-if="editBook" class="genres-row"><label>Genres</label><br/>
        <span v-for="g in genres" :key="g.id" class="genre-check">
          <label><input type="checkbox" :checked="editBook.genreIds.includes(g.id)"
                        @change="toggleGenre(editBook.genreIds, g.id)"/> {{ g.name }}</label>
        </span>
      </div>
      <p :class="editBookMsg.ok ? 'mod-msg--ok' : 'mod-msg--error'">{{ editBookMsg.text }}</p>
      <button v-if="editBook" @click="saveEditBook" class="btn-submit">Save Changes</button>
    </section>

    <!-- ── Edit Author tab ───────────────────────────────────────────────── -->
    <section v-if="activeTab === 'authors'">
      <h2>✍️ Edit Author</h2>
      <div style="margin-bottom:1rem;">
        <label>Select author:</label><br/>
        <select v-model="selectedAuthorId" @change="selectAuthorToEdit(selectedAuthorId!)" class="form-select--full">
          <option :value="null">-- choose an author --</option>
          <option v-for="a in authors" :key="a.id" :value="a.id">{{ a.firstName }} {{ a.lastName }}</option>
        </select>
      </div>
      <div v-if="editAuthor" class="form-grid">
        <div><label>First Name</label><br/><input v-model="editAuthor.firstName" class="form-input"/></div>
        <div><label>Last Name</label><br/><input v-model="editAuthor.lastName" class="form-input"/></div>
        <div><label>Nationality</label><br/><input v-model="editAuthor.nationality" class="form-input"/></div>
      </div>
      <div v-if="editAuthor" style="margin-top:1rem;">
        <label>Biography</label><br/>
        <textarea v-model="editAuthor.biography" rows="6" class="bio-textarea"></textarea>
      </div>
      <p :class="authorMsg.ok ? 'mod-msg--ok' : 'mod-msg--error'">{{ authorMsg.text }}</p>
      <button v-if="editAuthor" @click="saveEditAuthor" class="btn-submit">Save Author</button>
    </section>

    <!-- ── Series tab ────────────────────────────────────────────────────── -->
    <section v-if="activeTab === 'series'">
      <h2>📖 Manage Series</h2>

      <h3>Add New Series</h3>
      <div class="form-grid form-grid--narrow">
        <div><label>Name *</label><br/><input v-model="newSeries.name" class="form-input"/></div>
        <div><label>Volume Count</label><br/><input v-model.number="newSeries.volumeCount" type="number"
                                                    class="form-input"/></div>
        <div><label>Author</label><br/>
          <select v-model="newSeries.authorId" class="form-select">
            <option :value="null">-- none --</option>
            <option v-for="a in authors" :key="a.id" :value="a.id">{{ a.firstName }} {{ a.lastName }}</option>
          </select>
        </div>
      </div>
      <button @click="addSeries" class="btn-add-series">Add Series</button>

      <h3 style="margin-top:1.5rem;">Edit Existing Series</h3>
      <div style="margin-bottom:1rem;">
        <select v-model="selectedSeriesId" @change="selectSeriestoEdit(selectedSeriesId!)" class="form-select--full">
          <option :value="null">-- choose a series --</option>
          <option v-for="s in seriesList" :key="s.id" :value="s.id">{{ s.name }}</option>
        </select>
      </div>
      <div v-if="editSeries" class="form-grid form-grid--narrow">
        <div><label>Name</label><br/><input v-model="editSeries.name" class="form-input"/></div>
        <div><label>Volume Count</label><br/><input v-model.number="editSeries.volumeCount" type="number"
                                                    class="form-input"/></div>
        <div><label>Author</label><br/>
          <select v-model="editSeries.authorId" class="form-select">
            <option :value="null">-- none --</option>
            <option v-for="a in authors" :key="a.id" :value="a.id">{{ a.firstName }} {{ a.lastName }}</option>
          </select>
        </div>
      </div>
      <p :class="seriesMsg.ok ? 'mod-msg--ok' : 'mod-msg--error'">{{ seriesMsg.text }}</p>
      <button v-if="editSeries" @click="saveEditSeries" class="btn-submit">Save Series</button>
    </section>

    <!-- ── Reports tab ───────────────────────────────────────────────────── -->
    <section v-if="activeTab === 'reports'">
      <h2>🚨 Flagged Reviews</h2>
      <div class="filter-row">
        <label>Filter: </label>
        <select v-model="reportsFilter" @change="loadReports" class="filter-select">
          <option value="pending">Pending</option>
          <option value="resolved">Resolved</option>
          <option value="dismissed">Dismissed</option>
          <option value="">All</option>
        </select>
      </div>
      <p v-if="reports.length === 0" class="no-data">No reports found.</p>
      <table v-else class="mod-table">
        <thead>
        <tr class="table-head-row">
          <th class="table-th">ID</th>
          <th class="table-th">Review ID</th>
          <th class="table-th">Reporter</th>
          <th class="table-th">Reason</th>
          <th class="table-th">Status</th>
          <th class="table-th">Actions</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="r in reports" :key="r.id">
          <td class="table-td">{{ r.id }}</td>
          <td class="table-td">{{ r.review?.id }}</td>
          <td class="table-td">{{ r.reporterType }} #{{ r.reporterId }}</td>
          <td class="table-td">{{ r.reason }}</td>
          <td class="table-td">
            <span :style="{color: r.status==='pending'?'orange':r.status==='resolved'?'green':'gray'}">{{
                r.status
              }}</span>
          </td>
          <td class="table-td--nowrap">
            <button v-if="r.status==='pending'" @click="resolveReport(r.id)" class="btn-resolve">✅ Resolve</button>
            <button v-if="r.status==='pending'" @click="dismissReport(r.id)" class="btn-dismiss">❌ Dismiss</button>
            <button @click="deleteReview(r.review?.id)" class="btn-delete-review">🗑️ Delete Review</button>
          </td>
        </tr>
        </tbody>
      </table>
    </section>

    <!-- ── Readers tab ────────────────────────────────────────────────────── -->
    <section v-if="activeTab === 'readers'">
      <h2>👥 Registered Readers</h2>
      <div class="readers-search">
        <input v-model="readerSearch" placeholder="Search by username…" class="readers-search-input"/>
        <button @click="loadReaders" class="btn-search">Search</button>
        <button @click="readerSearch=''; loadReaders()" class="btn-clear">Clear</button>
      </div>
      <p v-if="readers.length === 0" class="no-data">No readers found.</p>
      <table v-else class="mod-table">
        <thead>
        <tr class="table-head-row">
          <th class="table-th">ID</th>
          <th class="table-th">Username</th>
          <th class="table-th">Email</th>
          <th class="table-th">Nationality</th>
          <th class="table-th">Role</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="r in readers" :key="r.id">
          <td class="table-td">{{ r.id }}</td>
          <td class="table-td">{{ r.username }}</td>
          <td class="table-td">{{ r.email }}</td>
          <td class="table-td">{{ r.nationality }}</td>
          <td class="table-td">
            <span :class="r.role === 'MODERATOR' ? 'role--mod' : 'role--reader'">{{ r.role }}</span>
          </td>
        </tr>
        </tbody>
      </table>
    </section>

  </div>
</template>

<style scoped>
.mod-panel {
  max-width: 1000px;
  margin: 0 auto;
  padding: 1rem;
}

.tab-bar {
  display: flex;
  gap: 4px;
  margin-bottom: 1.5rem;
  border-bottom: 2px solid #e5e7eb;
}

.tab-btn {
  padding: 8px 16px;
  border: none;
  cursor: pointer;
  border-radius: 6px 6px 0 0;
  background: #f3f4f6;
  color: #374151;
  font-weight: 400;
}

.tab-btn--active {
  background: #2563eb;
  color: white;
  font-weight: 600;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-grid--narrow {
  max-width: 640px;
}

.form-input,
.form-select {
  width: 100%;
  padding: 6px;
}

.form-select--full {
  width: 100%;
  padding: 6px;
  max-width: 400px;
}

.genres-row {
  margin-top: 1rem;
}

.genre-check {
  margin-right: 12px;
}

.mod-msg--ok {
  color: green;
}

.mod-msg--error {
  color: red;
}

.btn-submit {
  padding: 8px 20px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.btn-add-series {
  margin-top: 0.75rem;
  padding: 8px 20px;
  background: #16a34a;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.filter-row {
  margin-bottom: 1rem;
}

.filter-select {
  padding: 4px 8px;
}

.no-data {
  color: gray;
}

.mod-table {
  width: 100%;
  border-collapse: collapse;
}

.table-head-row {
  background: #f3f4f6;
  text-align: left;
}

.table-th {
  padding: 8px;
  border: 1px solid #ddd;
}

.table-td {
  padding: 8px;
  border: 1px solid #ddd;
}

.table-td--nowrap {
  padding: 8px;
  border: 1px solid #ddd;
  white-space: nowrap;
}

.btn-resolve {
  margin-right: 4px;
  padding: 4px 10px;
  background: #16a34a;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-dismiss {
  margin-right: 4px;
  padding: 4px 10px;
  background: #6b7280;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-delete-review {
  padding: 4px 10px;
  background: #dc2626;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.readers-search {
  display: flex;
  gap: 8px;
  margin-bottom: 1rem;
}

.readers-search-input {
  padding: 6px;
  flex: 1;
  max-width: 300px;
}

.btn-search {
  padding: 6px 14px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.btn-clear {
  padding: 6px 14px;
  background: #6b7280;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.role--mod {
  color: #2563eb;
}

.role--reader {
  color: #374151;
}

.bio-textarea {
  width: 100%;
  padding: 6px;
  font-family: inherit;
  resize: vertical;
}
</style>

