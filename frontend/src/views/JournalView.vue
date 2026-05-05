<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
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
    const res = await api.get('/journal', { params })
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
  const body: any = { entryDate: editDate.value }
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
  if (!user.value) { router.push('/login'); return }
  loadEntries()
})
</script>

<template>
  <div style="max-width:760px; margin:0 auto; padding:1.5rem;">
    <div style="display:flex; align-items:center; gap:1rem; margin-bottom:1.5rem;">
      <RouterLink to="/my-shelf" style="color:#2563eb; text-decoration:none; font-size:0.9rem;">← My Shelf</RouterLink>
      <h1 style="margin:0; font-size:1.5rem;">📖 Reading Activity</h1>
    </div>

    <div v-if="loading" style="color:gray;">Loading activity…</div>
    <div v-else-if="error" style="color:red;">{{ error }}</div>
    <div v-else-if="!entries.length" style="color:gray; font-style:italic;">
      No reading activity yet. Start reading a book to see your progress here!
    </div>

    <div v-else>
      <!-- Timeline entries -->
      <div v-for="entry in entries" :key="entry.id" style="display:flex; gap:1rem; margin-bottom:1.5rem;">

        <!-- Timeline line -->
        <div style="display:flex; flex-direction:column; align-items:center; flex-shrink:0;">
          <div style="width:10px; height:10px; border-radius:50%; background:#2563eb; margin-top:4px; flex-shrink:0;"></div>
          <div style="width:2px; flex:1; background:#e5e7eb; margin-top:4px;"></div>
        </div>

        <!-- Entry card -->
        <div style="flex:1; border:1px solid #e5e7eb; border-radius:10px; padding:1rem; background:white; box-shadow:0 1px 3px rgba(0,0,0,0.04);">

          <!-- Book title + author -->
          <RouterLink :to="`/books/${entry.readingLog?.book?.id}`"
            style="display:block; font-size:0.8rem; color:#6b7280; text-decoration:none; margin-bottom:4px;">
            <span v-if="entry.readingLog?.book?.author">
              {{ entry.readingLog.book.author.firstName }} {{ entry.readingLog.book.author.lastName }} —
            </span>
            <span style="font-weight:600; color:#1e3a5f;">{{ entry.readingLog?.book?.title }}</span>
          </RouterLink>

          <!-- VIEW mode -->
          <div v-if="editingId !== entry.id">
            <div style="display:flex; justify-content:space-between; align-items:flex-start;">
              <div>
                <!-- Date -->
                <div style="font-size:0.85rem; color:#9ca3af; margin-bottom:6px;">{{ entry.entryDate }}</div>

                <!-- STATUS_CHANGE -->
                <template v-if="entry.entryType === 'STATUS_CHANGE'">
                  <div v-if="entry.status === 'READING'" style="font-size:1rem; font-weight:600; color:#2563eb;">
                    Started reading
                  </div>
                  <div v-else-if="entry.status === 'FINISHED'">
                    <div style="font-size:1rem; font-weight:600; color:#16a34a;">Finished</div>
                    <div v-if="entry.cumulativePages != null" style="font-size:0.9rem; color:#6b7280; margin-top:4px;">
                      {{ getSessionPages(entry) }} pages read
                      ({{ entry.cumulativePages }} pages out of {{ entry.readingLog?.book?.pageCount ?? '?' }})
                    </div>
                  </div>
                  <div v-else-if="entry.status === 'DNF'">
                    <div style="font-size:1rem; font-weight:600; color:#dc2626;">Did not finish</div>
                    <div v-if="entry.cumulativePages != null" style="font-size:0.9rem; color:#6b7280; margin-top:4px;">
                      {{ entry.cumulativePages }} pages read out of {{ entry.readingLog?.book?.pageCount ?? '?' }}
                    </div>
                  </div>
                  <div v-else style="font-size:1rem; font-weight:600; color:#6b7280;">
                    Added to want-to-read
                  </div>
                </template>

                <!-- PROGRESS_UPDATE -->
                <template v-else>
                  <div style="font-size:1.4rem; font-weight:700; color:#1e3a5f;">
                    {{ getPercent(entry) != null ? getPercent(entry) + '%' : '—' }}
                  </div>
                  <div style="font-size:0.9rem; color:#6b7280; margin-top:2px;">
                    {{ getSessionPages(entry) }} pages read
                    ({{ entry.cumulativePages }} pages out of {{ entry.readingLog?.book?.pageCount ?? '?' }})
                  </div>
                  <!-- Mini progress bar -->
                  <div v-if="getPercent(entry) != null"
                    style="margin-top:8px; height:5px; background:#e5e7eb; border-radius:3px; overflow:hidden; width:200px; max-width:100%;">
                    <div :style="{ width: getPercent(entry) + '%', height:'100%', background:'#2563eb' }"></div>
                  </div>
                </template>
              </div>

              <!-- Actions -->
              <div style="display:flex; gap:6px; flex-shrink:0; margin-left:1rem;">
                <button @click="startEdit(entry)"
                  style="padding:4px 10px; font-size:0.8rem; background:#f3f4f6; border:1px solid #d1d5db; border-radius:4px; cursor:pointer;">
                  Edit
                </button>
                <button @click="deleteEntry(entry)"
                  style="padding:4px 10px; font-size:0.8rem; background:#fef2f2; color:#dc2626; border:1px solid #fecaca; border-radius:4px; cursor:pointer;">
                  Delete
                </button>
              </div>
            </div>
          </div>

          <!-- EDIT mode -->
          <div v-else>
            <div style="display:flex; gap:1rem; flex-wrap:wrap; align-items:flex-end;">
              <div>
                <label style="font-size:0.8rem; color:#6b7280; display:block; margin-bottom:3px;">Date</label>
                <input type="date" v-model="editDate"
                  style="padding:5px 8px; border:1px solid #2563eb; border-radius:5px;" />
              </div>
              <div v-if="entry.entryType === 'PROGRESS_UPDATE' || entry.status === 'FINISHED' || entry.status === 'DNF'">
                <label style="font-size:0.8rem; color:#6b7280; display:block; margin-bottom:3px;">
                  Pages read (total)
                </label>
                <input type="number" v-model.number="editPages" min="0"
                  :max="entry.readingLog?.book?.pageCount ?? 9999"
                  style="width:90px; padding:5px 8px; border:1px solid #2563eb; border-radius:5px;" />
              </div>
              <button @click="saveEdit(entry)"
                style="padding:6px 16px; background:#16a34a; color:white; border:none; border-radius:5px; cursor:pointer;">
                Save
              </button>
              <button @click="editingId = null"
                style="padding:6px 16px; background:#e5e7eb; border:none; border-radius:5px; cursor:pointer;">
                Cancel
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

