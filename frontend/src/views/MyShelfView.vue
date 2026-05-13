<script setup lang="ts">
import {ref, onMounted, computed} from 'vue'
import {useRouter} from 'vue-router'
import api from '@/api'


const router = useRouter()
const user = computed(() => JSON.parse(localStorage.getItem('user') || 'null'))

const shelf = ref<any[]>([])
const loading = ref(true)
const activeTab = ref<'TO_READ' | 'READING' | 'FINISHED' | 'DNF'>('READING')

const tabs = [
  {key: 'READING', label: '📖 Currently Reading'},
  {key: 'TO_READ', label: '🔖 Want to Read'},
  {key: 'FINISHED', label: '✅ Finished'},
  {key: 'DNF', label: '❌ Did Not Finish'},
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
    const res = await api.put(`/reading-log/${entry.id}`, {status: newStatus})
    Object.assign(entry, res.data)
  } catch (e: any) {
    alert(e.response?.data ?? 'Failed to update status')
  }
}

async function updatePages(entry: any) {
  try {
    const res = await api.put(`/reading-log/${entry.id}`, {pagesRead: entry.pagesRead})
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
  if (!user.value) {
    router.push('/login');
    return
  }
  loadShelf()
})
</script>

<template>
  <div class="my-shelf">
    <h1>📚 My Shelf</h1>

    <!-- Tabs -->
    <div class="shelf-tabs">
      <button
          v-for="tab in tabs" :key="tab.key"
          @click="activeTab = tab.key as any"
          class="tab-btn"
          :class="{ 'tab-btn--active': activeTab === tab.key }">
        {{ tab.label }}
        <span class="tab-count">({{ shelf.filter(e => e.status === tab.key).length }})</span>
      </button>
    </div>

    <div v-if="loading" class="shelf-loading">Loading your shelf…</div>

    <div v-else-if="filtered.length === 0" class="shelf-empty">
      No books here yet.
      <RouterLink to="/books"> Browse books →</RouterLink>
    </div>

    <div v-else class="shelf-list">
      <div v-for="entry in filtered" :key="entry.id" class="shelf-item">
        <div class="shelf-item-body">
          <div class="shelf-item-info">
            <RouterLink :to="`/books/${entry.book?.id}`" class="book-title-link">
              {{ entry.book?.title }}
            </RouterLink>
            <div class="book-meta">
              <span v-if="entry.book?.author">
                {{ entry.book.author.firstName }} {{ entry.book.author.lastName }}
              </span>
              <span v-if="entry.book?.pageCount" class="book-meta-pages">· {{ entry.book.pageCount }} pages</span>
            </div>

            <!-- Progress bar (reading) -->
            <div v-if="entry.status === 'READING'" class="progress-section">
              <div class="progress-row">
                <input
                    type="number" v-model.number="entry.pagesRead"
                    :max="entry.book?.pageCount ?? 9999" min="0"
                    @change="updatePages(entry)"
                    class="progress-input"/>
                <span class="progress-text">/ {{ entry.book?.pageCount ?? '?' }} pages</span>
                <span class="progress-pct">{{ progressPercent(entry) }}%</span>
              </div>
              <div class="progress-bar-bg">
                <div class="progress-bar-fill" :style="{ width: progressPercent(entry) + '%' }"></div>
              </div>
            </div>

            <div v-if="entry.startedAt" class="shelf-dates">
              Started: {{ entry.startedAt }}
              <span v-if="entry.finishedAt"> · Finished: {{ entry.finishedAt }}</span>
            </div>
          </div>

          <!-- Actions -->
          <div class="shelf-actions">
            <select
                :value="entry.status"
                @change="updateStatus(entry, ($event.target as HTMLSelectElement).value)"
                class="status-select">
              <option value="TO_READ">🔖 Want to Read</option>
              <option value="READING">📖 Reading</option>
              <option value="FINISHED">✅ Finished</option>
              <option value="DNF">❌ Did Not Finish</option>
            </select>

            <RouterLink :to="`/journal?readingLogId=${entry.id}`" class="activity-link">
              📖 Activity
            </RouterLink>

            <button @click="removeEntry(entry)" class="btn-remove">🗑️ Remove</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.my-shelf {
  max-width: 900px;
  margin: 0 auto;
  padding: 1.5rem;
}

.my-shelf h1 { margin-bottom: 1rem; color: #fabd2f; }

.shelf-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.tab-btn {
  padding: 8px 16px;
  border: 1px solid #504945;
  border-radius: 6px;
  cursor: pointer;
  font-weight: normal;
  background: #3c3836;
  color: #d5c4a1;
  transition: background 0.12s;
}
.tab-btn:hover { background: #504945; }

.tab-btn--active {
  background: #458588;
  color: #ebdbb2;
  border-color: #458588;
  font-weight: bold;
}

.tab-count { margin-left: 4px; opacity: 0.8; }

.shelf-loading { color: #a89984; }
.shelf-empty { color: #a89984; font-style: italic; }
.shelf-empty a { color: #83a598; }

.shelf-list { display: flex; flex-direction: column; gap: 1rem; }

.shelf-item {
  border: 1px solid #504945;
  border-radius: 10px;
  padding: 1rem;
  background: #3c3836;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.25);
}

.shelf-item-body {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
}

.shelf-item-info { flex: 1; }

.book-title-link {
  font-size: 1.1rem;
  font-weight: 600;
  color: #d5c4a1;
  text-decoration: none;
}
.book-title-link:hover { color: #fabd2f; }

.book-meta { color: #a89984; font-size: 0.9rem; margin-top: 2px; }
.book-meta-pages { margin-left: 8px; }

.progress-section { margin-top: 10px; }
.progress-row { display: flex; align-items: center; gap: 8px; }

.progress-input {
  width: 70px;
  padding: 4px 8px;
  border: 1px solid #504945;
  border-radius: 6px;
  font-size: 0.9rem;
  background: #32302f;
  color: #ebdbb2;
}

.progress-text { font-size: 0.85rem; color: #a89984; }

.progress-pct {
  font-size: 0.85rem;
  font-weight: 600;
  color: #83a598;
}

.progress-bar-bg {
  margin-top: 6px;
  height: 6px;
  background: #504945;
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  background: #458588;
  transition: width 0.3s;
}

.shelf-dates { font-size: 0.8rem; color: #7c6f64; margin-top: 6px; }

.shelf-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 140px;
}

.status-select {
  padding: 6px 10px;
  border: 1px solid #504945;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
  background: #32302f;
  color: #ebdbb2;
}

.activity-link {
  padding: 6px 10px;
  background: rgba(104, 157, 106, 0.15);
  color: #8ec07c;
  border: 1px solid #689d6a;
  border-radius: 6px;
  text-decoration: none;
  font-size: 0.85rem;
  text-align: center;
}

.btn-remove {
  padding: 6px 10px;
  background: rgba(204, 36, 29, 0.15);
  color: #fb4934;
  border: 1px solid #cc241d;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
}
</style>

