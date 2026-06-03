<script setup lang="ts">
import {ref, onMounted, computed} from 'vue'
import {useRouter} from 'vue-router'
import api from '@/api'
import ConfirmModal from '@/components/ConfirmModal.vue'


const router = useRouter()
const user = computed(() => JSON.parse(localStorage.getItem('user') || 'null'))

const shelf = ref<any[]>([])
const loading = ref(true)
const activeTab = ref<'TO_READ' | 'READING' | 'FINISHED' | 'DNF'>('READING')

const shareUrl = ref<string | null>(null)
const shareMsg = ref('')

async function generateShareLink() {
  try {
    const res = await api.post('/reading-log/share')
    shareUrl.value = window.location.origin + res.data.url
    shareMsg.value = ''
  } catch (e: any) {
    shareMsg.value = e.response?.data ?? 'Failed to generate link'
  }
}

async function revokeShareLink() {
  try {
    await api.delete('/reading-log/share')
    shareUrl.value = null
    shareMsg.value = 'Share link revoked.'
  } catch (e: any) {
    shareMsg.value = e.response?.data ?? 'Failed to revoke link'
  }
}

function copyShareLink() {
  if (!shareUrl.value) return
  navigator.clipboard.writeText(shareUrl.value)
  shareMsg.value = 'Link copied to clipboard!'
  setTimeout(() => { shareMsg.value = '' }, 3000)
}

const tabs = [
  {key: 'READING', label: '📖 Currently Reading'},
  {key: 'TO_READ', label: '🔖 Want to Read'},
  {key: 'FINISHED', label: '✅ Finished'},
  {key: 'DNF', label: '❌ Did Not Finish'},
]

const filtered = computed(() => shelf.value.filter(e => e.status === activeTab.value))

// Finish-book confirmation modal
const finishTarget = ref<any>(null)
function onFinishConfirm() { if (finishTarget.value) updateStatus(finishTarget.value, 'FINISHED'); finishTarget.value = null }
function onFinishCancel() { finishTarget.value = null }

// Draft pages map — keyed by entry.id, only saved on button click
const draftPages = ref<Record<number, number>>({})

function initDraft(entry: any) {
  if (!(entry.id in draftPages.value)) {
    draftPages.value[entry.id] = entry.pagesRead ?? 0
  }
}

async function loadShelf() {
  loading.value = true
  try {
    const res = await api.get('/reading-log')
    shelf.value = res.data
    // initialise drafts for all entries
    res.data.forEach((e: any) => { draftPages.value[e.id] = e.pagesRead ?? 0 })
  } finally {
    loading.value = false
  }
}

async function updateStatus(entry: any, newStatus: string) {
  try {
    const res = await api.put(`/reading-log/${entry.id}`, {status: newStatus})
    Object.assign(entry, res.data)
    draftPages.value[entry.id] = res.data.pagesRead ?? 0
  } catch (e: any) {
    alert(e.response?.data ?? 'Failed to update status')
  }
}

async function savePages(entry: any) {
  // clamp: no negatives, no more than total pages
  const max = entry.book?.pageCount ?? 999999
  const clamped = Math.min(Math.max(0, draftPages.value[entry.id] ?? 0), max)
  draftPages.value[entry.id] = clamped
  try {
    const res = await api.put(`/reading-log/${entry.id}`, {pagesRead: clamped})
    Object.assign(entry, res.data)
    draftPages.value[entry.id] = res.data.pagesRead ?? clamped

    // Offer to mark as finished when max pages reached
    if (entry.book?.pageCount && clamped >= entry.book.pageCount) {
      finishTarget.value = entry
    }
  } catch (e: any) {
    alert(e.response?.data ?? 'Failed to update progress')
  }
}

async function removeEntry(entry: any) {
  if (!confirm(`Remove "${entry.book?.title}" from your shelf?`)) return
  await api.delete(`/reading-log/${entry.id}`)
  shelf.value = shelf.value.filter(e => e.id !== entry.id)
  delete draftPages.value[entry.id]
}

function progressPercent(entry: any) {
  const total = entry.book?.pageCount
  const pages = draftPages.value[entry.id] ?? entry.pagesRead ?? 0
  if (!total || !pages) return 0
  return Math.min(100, Math.round((pages / total) * 100))
}

onMounted(() => {
  document.title = 'My Shelf — BookVault'
  if (!user.value) {
    router.push('/login');
    return
  }
  loadShelf()
})
</script>

<template>
  <div class="my-shelf">
    <h1><span aria-hidden="true">📚</span> My Shelf</h1>

    <!-- Share shelf -->
    <div class="share-section" aria-label="Share your shelf">
      <div v-if="!shareUrl" class="share-idle">
        <button class="btn-share" @click="generateShareLink" aria-label="Generate a public share link for your shelf">
          <span aria-hidden="true">🔗</span> Share my shelf
        </button>
        <span v-if="shareMsg" class="share-msg" role="alert">{{ shareMsg }}</span>
      </div>
      <div v-else class="share-active">
        <span class="share-label"><span aria-hidden="true">🔗</span> Public link:</span>
        <input class="share-input" :value="shareUrl" readonly aria-label="Your public shelf link" />
        <button class="btn-copy" @click="copyShareLink" aria-label="Copy share link">📋 Copy</button>
        <button class="btn-revoke" @click="revokeShareLink" aria-label="Revoke share link">✕ Revoke</button>
        <span v-if="shareMsg" class="share-msg" role="alert">{{ shareMsg }}</span>
      </div>
    </div>

    <!-- Tabs -->
    <div class="shelf-tabs" role="tablist" aria-label="Shelf categories">
      <button
          v-for="tab in tabs" :key="tab.key"
          @click="activeTab = tab.key as any"
          class="tab-btn"
          :class="{ 'tab-btn--active': activeTab === tab.key }"
          role="tab"
          :aria-selected="activeTab === tab.key"
          :aria-controls="`shelf-panel-${tab.key}`"
          :id="`shelf-tab-${tab.key}`"
      >
        {{ tab.label }}
        <span class="tab-count" aria-label="count">({{ shelf.filter(e => e.status === tab.key).length }})</span>
      </button>
    </div>

    <div
      :id="`shelf-panel-${activeTab}`"
      role="tabpanel"
      :aria-labelledby="`shelf-tab-${activeTab}`"
    >
      <div v-if="loading" class="shelf-loading" aria-live="polite">Loading your shelf…</div>

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
                  <label :for="`pages-${entry.id}`" class="visually-hidden">
                    Pages read (out of {{ entry.book?.pageCount ?? '?' }})
                  </label>
                  <input
                      :id="`pages-${entry.id}`"
                      type="number"
                      v-model.number="draftPages[entry.id]"
                      :max="entry.book?.pageCount ?? 9999" min="0"
                      :aria-label="`Pages read for ${entry.book?.title}`"
                      @keydown="(e) => !/^\d$/.test(e.key) && !['Backspace','Delete','Tab','ArrowLeft','ArrowRight','ArrowUp','ArrowDown','Home','End'].includes(e.key) && e.preventDefault()"
                      @input="draftPages[entry.id] = draftPages[entry.id] < 0 ? 0 : draftPages[entry.id] > (entry.book?.pageCount ?? 9999) ? (entry.book?.pageCount ?? 9999) : draftPages[entry.id]"
                      @vue:mounted="initDraft(entry)"
                      class="progress-input"/>
                  <span class="progress-text">/ {{ entry.book?.pageCount ?? '?' }} pages</span>
                  <span class="progress-pct" aria-live="polite">{{ progressPercent(entry) }}%</span>
                  <button
                    @click="savePages(entry)"
                    class="btn-save-pages"
                    :aria-label="`Save reading progress for ${entry.book?.title}`"
                  >Save</button>
                </div>
                <div
                  class="progress-bar-bg"
                  role="progressbar"
                  :aria-valuenow="progressPercent(entry)"
                  aria-valuemin="0"
                  aria-valuemax="100"
                  :aria-label="`Reading progress for ${entry.book?.title}: ${progressPercent(entry)}%`"
                >
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
              <label :for="`status-${entry.id}`" class="visually-hidden">
                Reading status for {{ entry.book?.title }}
              </label>
              <select
                  :id="`status-${entry.id}`"
                  :value="entry.status"
                  @change="updateStatus(entry, ($event.target as HTMLSelectElement).value)"
                  class="status-select"
                  :aria-label="`Reading status for ${entry.book?.title}`"
              >
                <option value="TO_READ">🔖 Want to Read</option>
                <option value="READING">📖 Reading</option>
                <option value="FINISHED">✅ Finished</option>
                <option value="DNF">❌ Did Not Finish</option>
              </select>

              <RouterLink :to="`/journal?readingLogId=${entry.id}`" class="activity-link">
                <span aria-hidden="true">📖</span> Activity
              </RouterLink>

              <button
                @click="removeEntry(entry)"
                class="btn-remove"
                :aria-label="`Remove ${entry.book?.title} from shelf`"
              >
                <span aria-hidden="true">🗑️</span> Remove
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <ConfirmModal
    v-if="finishTarget"
    title="Last page reached!"
    :message="`You've read all ${finishTarget.book?.pageCount} pages of &quot;${finishTarget.book?.title}&quot;. Mark it as Finished?`"
    confirm-label="✅ Yes, mark as Finished"
    cancel-label="📖 Keep Reading"
    @confirm="onFinishConfirm"
    @cancel="onFinishCancel"
  />
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

.my-shelf {
  max-width: 900px;
  margin: 0 auto;
  padding: 1.5rem;
}

.my-shelf h1 { margin-bottom: 1rem; color: #fabd2f; }

.share-section {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1.25rem;
  flex-wrap: wrap;
  background: #3c3836;
  border: 1px solid #504945;
  border-radius: 8px;
  padding: 0.65rem 1rem;
}
.share-idle, .share-active { display: flex; align-items: center; gap: 0.6rem; flex-wrap: wrap; width: 100%; }
.share-label { color: #a89984; font-size: 0.88rem; white-space: nowrap; }
.share-input {
  flex: 1; min-width: 220px;
  padding: 4px 8px;
  border: 1px solid #504945;
  border-radius: 6px;
  background: #32302f;
  color: #ebdbb2;
  font-size: 0.85rem;
}
.btn-share {
  padding: 5px 14px;
  background: #458588;
  color: #ebdbb2;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.88rem;
}
.btn-share:hover { filter: brightness(1.15); }
.btn-copy {
  padding: 4px 10px;
  background: #504945;
  color: #ebdbb2;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.82rem;
}
.btn-copy:hover { background: #665c54; }
.btn-revoke {
  padding: 4px 10px;
  background: rgba(204, 36, 29, 0.15);
  color: #fb4934;
  border: 1px solid #cc241d;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.82rem;
}
.share-msg { font-size: 0.82rem; color: #8ec07c; }

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
  -moz-appearance: textfield;
}
.progress-input:focus-visible { outline: 3px solid #83a598; outline-offset: 1px; }
.progress-input::-webkit-outer-spin-button,
.progress-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.progress-text { font-size: 0.85rem; color: #a89984; }

.progress-pct {
  font-size: 0.85rem;
  font-weight: 600;
  color: #83a598;
}

.btn-save-pages {
  padding: 4px 10px;
  background: #458588;
  color: #ebdbb2;
  border: none;
  border-radius: 6px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: filter 0.12s;
}
.btn-save-pages:hover { filter: brightness(1.15); }

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

