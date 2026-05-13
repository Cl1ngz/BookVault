<script setup lang="ts">
import {ref, onMounted, computed, watch} from 'vue'
import {useRoute} from 'vue-router'
import api from '@/api'
import ConfirmModal from '@/components/ConfirmModal.vue'


const route = useRoute()
const book = ref<any>(null)
const reviews = ref<any[]>([])
const error = ref('')

const user = computed(() => JSON.parse(localStorage.getItem('user') || 'null'))

// Reading status tracking
const shelfEntry = ref<any>(null)
const shelfLoading = ref(false)
const shelfMsg = ref({text: '', ok: true})

// Local draft for pages input — only saved when user clicks Save
const draftPages = ref(0)
watch(() => shelfEntry.value?.pagesRead, (v) => { draftPages.value = v ?? 0 }, { immediate: true })

// Finish-book confirmation modal
const showFinishModal = ref(false)
function onFinishConfirm() { showFinishModal.value = false; updateShelfStatus('FINISHED') }
function onFinishCancel() { showFinishModal.value = false }

async function loadShelfEntry() {
  if (!user.value) return
  try {
    const res = await api.get('/reading-log', {params: {bookId: route.params.id}})
    shelfEntry.value = res.data
  } catch {
    shelfEntry.value = null
  }
}

async function addToShelf(status: string) {
  shelfLoading.value = true
  shelfMsg.value = {text: '', ok: true}
  try {
    const res = await api.post('/reading-log', {bookId: Number(route.params.id), status})
    shelfEntry.value = res.data
    shelfMsg.value = {text: 'Added to shelf!', ok: true}
  } catch (e: any) {
    shelfMsg.value = {text: e.response?.data ?? 'Failed', ok: false}
  } finally {
    shelfLoading.value = false
  }
}

async function updateShelfStatus(newStatus: string) {
  if (!shelfEntry.value) return
  shelfLoading.value = true
  try {
    const res = await api.put(`/reading-log/${shelfEntry.value.id}`, {status: newStatus})
    shelfEntry.value = res.data
    shelfMsg.value = {text: 'Status updated!', ok: true}
  } catch (e: any) {
    shelfMsg.value = {text: e.response?.data ?? 'Failed', ok: false}
  } finally {
    shelfLoading.value = false
  }
}

async function updatePages() {
  if (!shelfEntry.value) return
  // clamp to valid range
  const clamped = Math.min(Math.max(0, draftPages.value ?? 0), book.value?.pageCount ?? 999999)
  draftPages.value = clamped
  shelfEntry.value.pagesRead = clamped
  try {
    const res = await api.put(`/reading-log/${shelfEntry.value.id}`, {pagesRead: clamped})
    shelfEntry.value = res.data
    draftPages.value = res.data.pagesRead ?? clamped
    shelfMsg.value = {text: 'Progress saved!', ok: true}

    // Offer to mark as finished when max pages reached
    if (book.value?.pageCount && clamped >= book.value.pageCount) {
      showFinishModal.value = true
    }
  } catch {
    shelfMsg.value = {text: 'Failed to save progress.', ok: false}
  }
}

function progressPercent() {
  const total = book.value?.pageCount
  if (!total || !draftPages.value) return 0
  return Math.min(100, Math.round((draftPages.value / total) * 100))
}

// Star rating options: 0.25, 0.50, … 5.00
const ratingOptions = Array.from({length: 20}, (_, i) => +((i + 1) * 0.25).toFixed(2))

// Review form state
const newRating = ref<number>(3)
const newContent = ref('')
const reviewMsg = ref({text: '', ok: true})

async function loadReviews() {
  const res = await api.get('/reviews', {params: {bookId: route.params.id}})
  reviews.value = res.data
}

onMounted(async () => {
  try {
    const [bookRes] = await Promise.all([
      api.get(`/books/${route.params.id}`),
      loadReviews()
    ])
    book.value = bookRes.data
    await loadShelfEntry()
  } catch {
    error.value = 'Failed to load book.'
  }
})

async function submitReview() {
  reviewMsg.value = {text: '', ok: true}
  if (!user.value) {
    reviewMsg.value = {text: 'You must be logged in to post a review.', ok: false};
    return
  }
  try {
    await api.post('/reviews', {
      bookId: Number(route.params.id),
      rating: newRating.value,
      content: newContent.value.trim() || null
    })
    reviewMsg.value = {text: 'Review posted!', ok: true}
    newRating.value = 3
    newContent.value = ''
    await loadReviews()
  } catch (e: any) {
    reviewMsg.value = {text: e.response?.data ?? 'Failed to post review.', ok: false}
  }
}

async function reportReview(reviewId: number) {
  if (!user.value) {
    alert('You must be logged in to report');
    return
  }
  const reason = prompt('Reason for report?')
  if (!reason) return
  await api.post('/reports', {reviewId, reason})
  alert('Report submitted!')
}

function renderStars(rating: number) {
  const full = Math.floor(rating)
  const half = rating - full >= 0.25 && rating - full < 0.75
  const almost = rating - full >= 0.75
  const totalFull = almost ? full + 1 : full
  const empty = 5 - totalFull - (half ? 1 : 0)
  return '★'.repeat(totalFull) + (half ? '½' : '') + '☆'.repeat(empty)
}
</script>

<template>
  <div class="book-detail">
    <div v-if="error" class="error-msg">{{ error }}</div>

    <div v-else-if="book">
      <RouterLink to="/books">← Back to Books</RouterLink>
      <h1>{{ book.title }}</h1>
      <p v-if="book.author"><strong>Author:</strong>
        <RouterLink :to="`/authors/${book.author.id}`">{{ book.author.firstName }} {{
            book.author.lastName
          }}
        </RouterLink>
      </p>
      <p v-if="book.publisher"><strong>Publisher:</strong> {{ book.publisher.name }}</p>
      <p v-if="book.series"><strong>Series:</strong>
        <RouterLink :to="`/series/${book.series.id}`">{{ book.series.name }}</RouterLink>
      </p>
      <p><strong>Year:</strong> {{ book.publicationYear }}</p>
      <p><strong>Pages:</strong> {{ book.pageCount }}</p>
      <p v-if="book.mood"><strong>Mood:</strong> {{ book.mood }}</p>
      <p v-if="book.genres?.length"><strong>Genres:</strong> {{ book.genres.map((g: any) => g.name).join(', ') }}</p>

      <!-- Reading Status Section -->
      <div v-if="user" class="reading-status-section">
        <h3>📚 My Reading Status</h3>

        <!-- Not on shelf -->
        <div v-if="!shelfEntry" class="shelf-not-added">
          <p>This book isn't on your shelf yet.</p>
          <div class="shelf-buttons">
            <button @click="addToShelf('TO_READ')" :disabled="shelfLoading" class="btn btn-want-to-read">
              🔖 Want to Read
            </button>
            <button @click="addToShelf('READING')" :disabled="shelfLoading" class="btn btn-start-reading">
              📖 Start Reading
            </button>
            <button @click="addToShelf('FINISHED')" :disabled="shelfLoading" class="btn btn-mark-finished">
              ✅ Mark as Finished
            </button>
          </div>
        </div>

        <!-- On shelf -->
        <div v-else>
          <div class="shelf-controls">
            <select :value="shelfEntry.status" @change="updateShelfStatus(($event.target as HTMLSelectElement).value)"
                    class="status-select">
              <option value="TO_READ">🔖 Want to Read</option>
              <option value="READING">📖 Reading</option>
              <option value="FINISHED">✅ Finished</option>
              <option value="DNF">❌ Did Not Finish</option>
            </select>
            <RouterLink :to="`/journal?readingLogId=${shelfEntry.id}`" class="journal-link">
              📖 Activity Log
            </RouterLink>
          </div>

          <!-- Progress tracking when reading -->
          <div v-if="shelfEntry.status === 'READING' && book.pageCount" class="progress-section">
            <div class="progress-row">
              <input type="number" v-model.number="draftPages"
                     :max="book.pageCount" min="0"
                     @keydown="(e) => !/^\d$/.test(e.key) && !['Backspace','Delete','Tab','ArrowLeft','ArrowRight','ArrowUp','ArrowDown','Home','End'].includes(e.key) && e.preventDefault()"
                     @input="draftPages = draftPages < 0 ? 0 : draftPages > book.pageCount ? book.pageCount : draftPages"
                     class="progress-pages-input"/>
              <span class="progress-info">/ {{ book.pageCount }} pages · <strong>{{
                  progressPercent()
                }}%</strong></span>
              <button @click="updatePages()" class="btn-save-pages" :disabled="shelfLoading">Save</button>
            </div>
            <div class="progress-bar-bg">
              <div class="progress-bar-fill" :style="{ width: progressPercent() + '%' }"></div>
            </div>
          </div>

          <div v-if="shelfEntry.startedAt" class="shelf-dates">
            Started: {{ shelfEntry.startedAt }}
            <span v-if="shelfEntry.finishedAt"> · Finished: {{ shelfEntry.finishedAt }}</span>
          </div>
        </div>

        <p v-if="shelfMsg.text" class="shelf-msg" :class="shelfMsg.ok ? 'shelf-msg--ok' : 'shelf-msg--error'">
          {{ shelfMsg.text }}
        </p>
      </div>

      <!-- Reviews list -->
      <h2 style="margin-top:1.5rem;">Reviews ({{ reviews.length }})</h2>
      <ul v-if="reviews.length" class="reviews-list">
        <li v-for="r in reviews" :key="r.id" class="review-item">
          <span class="review-stars">{{ renderStars(r.rating) }}</span>
          <strong class="review-rating">{{ r.rating.toFixed(2) }} / 5</strong>
          <em class="review-author">({{ r.reader?.username }})</em>
          <p v-if="r.content" class="review-content">{{ r.content }}</p>
          <button @click="reportReview(r.id)" class="btn-report">🚩 Report</button>
        </li>
      </ul>
      <p v-else style="color:gray;">No reviews yet. Be the first!</p>

      <!-- Add review form -->
      <div class="review-form">
        <h3>✍️ Write a Review</h3>

        <div v-if="!user" class="login-prompt">
          <RouterLink to="/login">Log in</RouterLink>
          to post a review.
        </div>

        <div v-else>
          <label><strong>Rating:</strong></label><br/>
          <select v-model="newRating" class="review-rating-select">
            <option v-for="val in ratingOptions" :key="val" :value="val">
              {{ val.toFixed(2) }} ★
            </option>
          </select>
          <p class="review-stars-preview">
            {{ renderStars(newRating) }} <strong>{{ newRating.toFixed(2) }} / 5</strong>
          </p>

          <label><strong>Comment (optional):</strong></label><br/>
          <textarea v-model="newContent" rows="3" placeholder="Share your thoughts…"
                    class="review-textarea"></textarea>

          <p class="review-msg" :class="reviewMsg.ok ? 'review-msg--ok' : 'review-msg--error'">{{ reviewMsg.text }}</p>
          <button @click="submitReview" class="btn-post">Post Review</button>
        </div>
      </div>
    </div>

    <p v-else>Loading...</p>
  </div>

  <ConfirmModal
    v-if="showFinishModal"
    title="Last page reached!"
    :message="`You've read all ${book?.pageCount} pages of &quot;${book?.title}&quot;. Mark it as Finished?`"
    confirm-label="✅ Yes, mark as Finished"
    cancel-label="📖 Keep Reading"
    @confirm="onFinishConfirm"
    @cancel="onFinishCancel"
  />
</template>

<style scoped>
/* ── BookDetailView ─────────────────────────────────────────── */
.book-detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 1rem;
}

/* ── Reading-status card ────────────────────────────────────── */
.reading-status-section {
  margin-top: 1.5rem;
  padding: 1rem;
  border: 1px solid #458588;
  border-radius: 8px;
  background: #32302f;
}

.reading-status-section h3 {
  margin: 0 0 0.75rem;
  color: #83a598;
}

.shelf-not-added p {
  color: #a89984;
  margin: 0 0 0.75rem;
  font-size: 0.95rem;
}

.shelf-buttons {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.btn {
  padding: 7px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: filter 0.12s;
}
.btn:hover { filter: brightness(1.15); }

.btn-want-to-read {
  background: rgba(215, 153, 33, 0.2);
  color: #fabd2f;
  border: 1px solid #d79921;
}

.btn-start-reading {
  background: #458588;
  color: #ebdbb2;
  border: none;
}

.btn-mark-finished {
  background: #98971a;
  color: #ebdbb2;
  border: none;
}

/* ── Shelf controls ─────────────────────────────────────────── */
.shelf-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
  margin-bottom: 0.75rem;
}

.status-select {
  padding: 7px 12px;
  border: 1px solid #504945;
  border-radius: 6px;
  font-size: 0.95rem;
  cursor: pointer;
  background: #3c3836;
  color: #ebdbb2;
}

.journal-link {
  padding: 7px 14px;
  background: rgba(104, 157, 106, 0.15);
  color: #8ec07c;
  border: 1px solid #689d6a;
  border-radius: 6px;
  text-decoration: none;
  font-size: 0.9rem;
}

/* ── Progress tracking ──────────────────────────────────────── */
.progress-section { margin-top: 0.5rem; }

.progress-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.progress-pages-input {
  width: 70px;
  padding: 5px 8px;
  border: 1px solid #504945;
  border-radius: 6px;
  background: #3c3836;
  color: #ebdbb2;
  -moz-appearance: textfield;
}
.progress-pages-input::-webkit-outer-spin-button,
.progress-pages-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.progress-info { font-size: 0.9rem; color: #a89984; }
.progress-info strong { color: #83a598; }

.btn-save-pages {
  padding: 5px 12px;
  background: #458588;
  color: #ebdbb2;
  border: none;
  border-radius: 6px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: filter 0.12s;
}
.btn-save-pages:hover:not(:disabled) { filter: brightness(1.15); }
.btn-save-pages:disabled { opacity: 0.5; cursor: not-allowed; }

.progress-bar-bg {
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

/* ── Shelf dates & messages ─────────────────────────────────── */
.shelf-dates { font-size: 0.8rem; color: #7c6f64; margin-top: 6px; }

.shelf-msg { margin: 8px 0 0; font-size: 0.9rem; }
.shelf-msg--ok { color: #b8bb26; }
.shelf-msg--error { color: #fb4934; }

/* ── Reviews list ───────────────────────────────────────────── */
.reviews-list { list-style: none; padding: 0; }

.review-item {
  padding: 10px;
  border: 1px solid #504945;
  border-radius: 6px;
  margin-bottom: 8px;
  background: #32302f;
}

.review-stars { font-size: 1.1rem; color: #fabd2f; }
.review-rating { margin-left: 6px; color: #ebdbb2; }
.review-author { color: #a89984; margin-left: 8px; }
.review-content { margin: 4px 0; color: #d5c4a1; }

.btn-report {
  font-size: 0.8rem;
  padding: 2px 8px;
  background: none;
  border: 1px solid #504945;
  border-radius: 4px;
  cursor: pointer;
  color: #a89984;
}

/* ── Review form ────────────────────────────────────────────── */
.review-form {
  margin-top: 1.5rem;
  padding: 1rem;
  border: 1px solid #504945;
  border-radius: 8px;
  background: #32302f;
}

.review-form h3 { margin-top: 0; color: #d5c4a1; }
.login-prompt { color: #a89984; }

.review-rating-select {
  padding: 6px 12px;
  margin: 8px 0;
  font-size: 1rem;
  border: 1px solid #504945;
  border-radius: 6px;
  cursor: pointer;
  background: #3c3836;
  color: #ebdbb2;
}

.review-stars-preview { margin: 4px 0 10px; font-size: 1.2rem; color: #fabd2f; }

.review-textarea {
  width: 100%;
  padding: 6px;
  margin-top: 4px;
  font-family: inherit;
  resize: vertical;
  box-sizing: border-box;
  background: #3c3836;
  color: #ebdbb2;
  border: 1px solid #504945;
  border-radius: 4px;
}

.review-msg { margin: 6px 0; }
.review-msg--ok { color: #b8bb26; }
.review-msg--error { color: #fb4934; }

.btn-post {
  padding: 8px 20px;
  background: #458588;
  color: #ebdbb2;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: filter 0.12s;
}
.btn-post:hover { filter: brightness(1.15); }

/* ── Misc ───────────────────────────────────────────────────── */
.error-msg { color: #fb4934; }
</style>

