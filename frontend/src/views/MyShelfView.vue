<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api'

const router = useRouter()
const user = computed(() => JSON.parse(localStorage.getItem('user') || 'null'))

const shelf = ref<any[]>([])
const loading = ref(true)
const activeTab = ref<'TO_READ' | 'READING' | 'FINISHED' | 'DNF'>('READING')

const tabs = [
  { key: 'READING', label: '📖 Currently Reading' },
  { key: 'TO_READ', label: '🔖 Want to Read' },
  { key: 'FINISHED', label: '✅ Finished' },
  { key: 'DNF', label: '❌ Did Not Finish' },
]

const filtered = computed(() => shelf.value.filter(e => e.status === activeTab.value))

async function loadShelf() {
  loading.value = true
  try {
    const res = await api.get('/reading-log')
    shelf.value = res.data
  } finally {
    loading.value = false
  }
}

async function updateStatus(entry: any, newStatus: string) {
  try {
    const res = await api.put(`/reading-log/${entry.id}`, { status: newStatus })
    Object.assign(entry, res.data)
  } catch (e: any) {
    alert(e.response?.data ?? 'Failed to update status')
  }
}

async function updatePages(entry: any) {
  try {
    const res = await api.put(`/reading-log/${entry.id}`, { pagesRead: entry.pagesRead })
    Object.assign(entry, res.data)
  } catch (e: any) {
    alert(e.response?.data ?? 'Failed to update progress')
  }
}

async function removeEntry(entry: any) {
  if (!confirm(`Remove "${entry.book?.title}" from your shelf?`)) return
  await api.delete(`/reading-log/${entry.id}`)
  shelf.value = shelf.value.filter(e => e.id !== entry.id)
}

function progressPercent(entry: any) {
  const total = entry.book?.pageCount
  if (!total || !entry.pagesRead) return 0
  return Math.min(100, Math.round((entry.pagesRead / total) * 100))
}

onMounted(() => {
  if (!user.value) { router.push('/login'); return }
  loadShelf()
})
</script>

<template>
  <div style="max-width:900px; margin:0 auto; padding:1.5rem;">
    <h1 style="margin-bottom:1rem;">📚 My Shelf</h1>

    <!-- Tabs -->
    <div style="display:flex; gap:0.5rem; margin-bottom:1.5rem; flex-wrap:wrap;">
      <button
        v-for="tab in tabs" :key="tab.key"
        @click="activeTab = tab.key as any"
        :style="{
          padding: '8px 16px',
          border: 'none',
          borderRadius: '6px',
          cursor: 'pointer',
          fontWeight: activeTab === tab.key ? 'bold' : 'normal',
          background: activeTab === tab.key ? '#2563eb' : '#e5e7eb',
          color: activeTab === tab.key ? 'white' : '#374151',
        }">
        {{ tab.label }}
        <span style="margin-left:4px; opacity:0.8;">({{ shelf.filter(e => e.status === tab.key).length }})</span>
      </button>
    </div>

    <div v-if="loading" style="color:gray;">Loading your shelf…</div>

    <div v-else-if="filtered.length === 0" style="color:gray; font-style:italic;">
      No books here yet.
      <RouterLink to="/books" style="color:#2563eb;"> Browse books →</RouterLink>
    </div>

    <div v-else style="display:flex; flex-direction:column; gap:1rem;">
      <div
        v-for="entry in filtered" :key="entry.id"
        style="border:1px solid #e5e7eb; border-radius:10px; padding:1rem; background:white; box-shadow:0 1px 3px rgba(0,0,0,0.05);">

        <div style="display:flex; align-items:flex-start; justify-content:space-between; gap:1rem;">
          <div style="flex:1;">
            <RouterLink :to="`/books/${entry.book?.id}`" style="font-size:1.1rem; font-weight:600; color:#1e3a5f; text-decoration:none;">
              {{ entry.book?.title }}
            </RouterLink>
            <div style="color:#6b7280; font-size:0.9rem; margin-top:2px;">
              <span v-if="entry.book?.author">
                {{ entry.book.author.firstName }} {{ entry.book.author.lastName }}
              </span>
              <span v-if="entry.book?.pageCount" style="margin-left:8px;">· {{ entry.book.pageCount }} pages</span>
            </div>

            <!-- Progress bar (reading) -->
            <div v-if="entry.status === 'READING'" style="margin-top:10px;">
              <div style="display:flex; align-items:center; gap:8px;">
                <input
                  type="number" v-model.number="entry.pagesRead"
                  :max="entry.book?.pageCount ?? 9999" min="0"
                  @change="updatePages(entry)"
                  style="width:70px; padding:4px 8px; border:1px solid #d1d5db; border-radius:6px; font-size:0.9rem;" />
                <span style="font-size:0.85rem; color:#6b7280;">/ {{ entry.book?.pageCount ?? '?' }} pages</span>
                <span style="font-size:0.85rem; font-weight:600; color:#2563eb;">{{ progressPercent(entry) }}%</span>
              </div>
              <div style="margin-top:6px; height:6px; background:#e5e7eb; border-radius:3px; overflow:hidden;">
                <div :style="{ width: progressPercent(entry) + '%', height:'100%', background:'#2563eb', transition:'width 0.3s' }"></div>
              </div>
            </div>

            <div v-if="entry.startedAt" style="font-size:0.8rem; color:#9ca3af; margin-top:6px;">
              Started: {{ entry.startedAt }}
              <span v-if="entry.finishedAt"> · Finished: {{ entry.finishedAt }}</span>
            </div>
          </div>

          <!-- Actions -->
          <div style="display:flex; flex-direction:column; gap:6px; min-width:140px;">
            <select
              :value="entry.status"
              @change="updateStatus(entry, ($event.target as HTMLSelectElement).value)"
              style="padding:6px 10px; border:1px solid #d1d5db; border-radius:6px; font-size:0.85rem; cursor:pointer;">
              <option value="TO_READ">🔖 Want to Read</option>
              <option value="READING">📖 Reading</option>
              <option value="FINISHED">✅ Finished</option>
              <option value="DNF">❌ Did Not Finish</option>
            </select>

            <RouterLink :to="`/journal?readingLogId=${entry.id}`"
              style="padding:6px 10px; background:#f0fdf4; color:#16a34a; border:1px solid #bbf7d0; border-radius:6px; text-decoration:none; font-size:0.85rem; text-align:center;">
              📖 Activity
            </RouterLink>

            <button @click="removeEntry(entry)"
              style="padding:6px 10px; background:#fef2f2; color:#dc2626; border:1px solid #fecaca; border-radius:6px; font-size:0.85rem; cursor:pointer;">
              🗑️ Remove
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

