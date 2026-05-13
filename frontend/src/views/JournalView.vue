<script setup lang="ts">
import {ref, onMounted, computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import api from '@/api'


const route = useRoute()
const router = useRouter()
const user = computed(() => JSON.parse(localStorage.getItem('user') || 'null'))

const readingLogId = computed(() => route.query.readingLogId ? Number(route.query.readingLogId) : null)
const entries = ref<any[]>([])
const loading = ref(true)
const error = ref('')
const editingId = ref<number | null>(null)
const editDate = ref('')
const editPages = ref<number | null>(null)

// --- Session-pages computation ---
// For each entry, session pages = cumulativePages - previous entry's cumulativePages (per reading log, chronological)
const sessionPagesMap = computed(() => {
  const map = new Map<number, number>()
  // Group entries per readingLog
  const groups = new Map<number, any[]>()
  for (const e of entries.value) {
    const rid = e.readingLog?.id
    if (rid == null) continue
    if (!groups.has(rid)) groups.set(rid, [])
    groups.get(rid)!.push(e)
  }
  for (const group of groups.values()) {
    // Sort ASC by date then id for chronological ordering
    const asc = [...group].sort((a, b) => {
      const d = new Date(a.entryDate).getTime() - new Date(b.entryDate).getTime()
      return d !== 0 ? d : a.id - b.id
    })
    let prevPages = 0
    for (const e of asc) {
      if (e.cumulativePages != null) {
        map.set(e.id, e.cumulativePages - prevPages)
        prevPages = e.cumulativePages
      }
    }
  }
  return map
})

function getPercent(entry: any) {
  const total = entry.readingLog?.book?.pageCount
  if (!total || entry.cumulativePages == null) return null
  return Math.round((entry.cumulativePages / total) * 100)
}

function getSessionPages(entry: any) {
  return sessionPagesMap.value.get(entry.id) ?? entry.cumulativePages ?? 0
}

async function loadEntries() {
  loading.value = true
  error.value = ''
  try {
    const params: any = {}
    if (readingLogId.value) params.readingLogId = readingLogId.value
    const res = await api.get('/journal', {params})
    entries.value = res.data
  } catch (e: any) {
    error.value = e.response?.data ?? 'Failed to load activity'
  } finally {
    loading.value = false
  }
}

function startEdit(entry: any) {
  editingId.value = entry.id
  editDate.value = entry.entryDate
  editPages.value = entry.cumulativePages ?? null
}

async function saveEdit(entry: any) {
  const body: any = {entryDate: editDate.value}
  if (entry.entryType === 'PROGRESS_UPDATE' || entry.status === 'FINISHED' || entry.status === 'DNF') {
    if (editPages.value != null) body.cumulativePages = editPages.value
  }
  try {
    const res = await api.put(`/journal/${entry.id}`, body)
    Object.assign(entry, res.data)
    editingId.value = null
  } catch (e: any) {
    alert(e.response?.data ?? 'Failed to save')
  }
}

async function deleteEntry(entry: any) {
  if (!confirm('Delete this activity entry?')) return
  await api.delete(`/journal/${entry.id}`)
  entries.value = entries.value.filter(e => e.id !== entry.id)
}

onMounted(() => {
  if (!user.value) {
    router.push('/login');
    return
  }
  loadEntries()
})
</script>

<template>
  <div class="journal-view">
    <div class="journal-header">
      <RouterLink to="/my-shelf">← My Shelf</RouterLink>
      <h1>📖 Reading Activity</h1>
    </div>

    <div v-if="loading" class="journal-loading">Loading activity…</div>
    <div v-else-if="error" class="journal-error">{{ error }}</div>
    <div v-else-if="!entries.length" class="journal-empty">
      No reading activity yet. Start reading a book to see your progress here!
    </div>

    <div v-else>
      <div v-for="entry in entries" :key="entry.id" class="timeline-entry">

        <!-- Timeline line -->
        <div class="timeline-dot-col">
          <div class="timeline-dot"></div>
          <div class="timeline-line"></div>
        </div>

        <!-- Entry card -->
        <div class="entry-card">

          <!-- Book title + author -->
          <RouterLink :to="`/books/${entry.readingLog?.book?.id}`" class="entry-book-link">
            <span v-if="entry.readingLog?.book?.author">
              {{ entry.readingLog.book.author.firstName }} {{ entry.readingLog.book.author.lastName }} —
            </span>
            <span class="entry-book-title">{{ entry.readingLog?.book?.title }}</span>
          </RouterLink>

          <!-- VIEW mode -->
          <div v-if="editingId !== entry.id">
            <div class="entry-top">
              <div>
                <div class="entry-date">{{ entry.entryDate }}</div>

                <!-- STATUS_CHANGE -->
                <template v-if="entry.entryType === 'STATUS_CHANGE'">
                  <div v-if="entry.status === 'READING'" class="status-started">Started reading</div>
                  <div v-else-if="entry.status === 'FINISHED'">
                    <div class="status-finished">Finished</div>
                    <div v-if="entry.cumulativePages != null" class="entry-pages-note">
                      {{ getSessionPages(entry) }} pages read
                      ({{ entry.cumulativePages }} pages out of {{ entry.readingLog?.book?.pageCount ?? '?' }})
                    </div>
                  </div>
                  <div v-else-if="entry.status === 'DNF'">
                    <div class="status-dnf">Did not finish</div>
                    <div v-if="entry.cumulativePages != null" class="entry-pages-note">
                      {{ entry.cumulativePages }} pages read out of {{ entry.readingLog?.book?.pageCount ?? '?' }}
                    </div>
                  </div>
                  <div v-else class="status-want-read">Added to want-to-read</div>
                </template>

                <!-- PROGRESS_UPDATE -->
                <template v-else>
                  <div class="progress-pct">
                    {{ getPercent(entry) != null ? getPercent(entry) + '%' : '—' }}
                  </div>
                  <div class="progress-pages">
                    {{ getSessionPages(entry) }} pages read
                    ({{ entry.cumulativePages }} pages out of {{ entry.readingLog?.book?.pageCount ?? '?' }})
                  </div>
                  <div v-if="getPercent(entry) != null" class="mini-progress-bg">
                    <div class="mini-progress-fill" :style="{ width: getPercent(entry) + '%' }"></div>
                  </div>
                </template>
              </div>

              <!-- Actions -->
              <div class="entry-actions">
                <button @click="startEdit(entry)" class="btn-edit">Edit</button>
                <button @click="deleteEntry(entry)" class="btn-delete">Delete</button>
              </div>
            </div>
          </div>

          <!-- EDIT mode -->
          <div v-else>
            <div class="edit-row">
              <div>
                <label class="edit-label">Date</label>
                <input type="date" v-model="editDate" class="edit-input"/>
              </div>
              <div
                  v-if="entry.entryType === 'PROGRESS_UPDATE' || entry.status === 'FINISHED' || entry.status === 'DNF'">
                <label class="edit-label">Pages read (total)</label>
                <input type="number" v-model.number="editPages" min="0"
                       :max="entry.readingLog?.book?.pageCount ?? 9999"
                       class="edit-input edit-input--pages"/>
              </div>
              <button @click="saveEdit(entry)" class="btn-save">Save</button>
              <button @click="editingId = null" class="btn-cancel">Cancel</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.journal-view {
  max-width: 760px;
  margin: 0 auto;
  padding: 1.5rem;
}

.journal-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.journal-header a {
  color: #2563eb;
  text-decoration: none;
  font-size: 0.9rem;
}

.journal-header h1 {
  margin: 0;
  font-size: 1.5rem;
}

.journal-loading {
  color: gray;
}

.journal-error {
  color: red;
}

.journal-empty {
  color: gray;
  font-style: italic;
}

.timeline-entry {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.timeline-dot-col {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.timeline-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #2563eb;
  margin-top: 4px;
  flex-shrink: 0;
}

.timeline-line {
  width: 2px;
  flex: 1;
  background: #e5e7eb;
  margin-top: 4px;
}

.entry-card {
  flex: 1;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 1rem;
  background: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.entry-book-link {
  display: block;
  font-size: 0.8rem;
  color: #6b7280;
  text-decoration: none;
  margin-bottom: 4px;
}

.entry-book-title {
  font-weight: 600;
  color: #1e3a5f;
}

.entry-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.entry-date {
  font-size: 0.85rem;
  color: #9ca3af;
  margin-bottom: 6px;
}

.status-started {
  font-size: 1rem;
  font-weight: 600;
  color: #2563eb;
}

.status-finished {
  font-size: 1rem;
  font-weight: 600;
  color: #16a34a;
}

.status-dnf {
  font-size: 1rem;
  font-weight: 600;
  color: #dc2626;
}

.status-want-read {
  font-size: 1rem;
  font-weight: 600;
  color: #6b7280;
}

.entry-pages-note {
  font-size: 0.9rem;
  color: #6b7280;
  margin-top: 4px;
}

.progress-pct {
  font-size: 1.4rem;
  font-weight: 700;
  color: #1e3a5f;
}

.progress-pages {
  font-size: 0.9rem;
  color: #6b7280;
  margin-top: 2px;
}

.mini-progress-bg {
  margin-top: 8px;
  height: 5px;
  background: #e5e7eb;
  border-radius: 3px;
  overflow: hidden;
  width: 200px;
  max-width: 100%;
}

.mini-progress-fill {
  height: 100%;
  background: #2563eb;
}

.entry-actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
  margin-left: 1rem;
}

.btn-edit {
  padding: 4px 10px;
  font-size: 0.8rem;
  background: #f3f4f6;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  cursor: pointer;
}

.btn-delete {
  padding: 4px 10px;
  font-size: 0.8rem;
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
  border-radius: 4px;
  cursor: pointer;
}

.edit-row {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  align-items: flex-end;
}

.edit-label {
  font-size: 0.8rem;
  color: #6b7280;
  display: block;
  margin-bottom: 3px;
}

.edit-input {
  padding: 5px 8px;
  border: 1px solid #2563eb;
  border-radius: 5px;
}

.edit-input--pages {
  width: 90px;
}

.btn-save {
  padding: 6px 16px;
  background: #16a34a;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.btn-cancel {
  padding: 6px 16px;
  background: #e5e7eb;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}
</style>

