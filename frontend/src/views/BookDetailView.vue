<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api'

const route = useRoute()
const book = ref<any>(null)
const reviews = ref<any[]>([])
const error = ref('')

const user = computed(() => JSON.parse(localStorage.getItem('user') || 'null'))

// Reading status tracking
const shelfEntry = ref<any>(null)
const shelfLoading = ref(false)
const shelfMsg = ref({ text: '', ok: true })

async function loadShelfEntry() {
  if (!user.value) return
  try {
    const res = await api.get('/reading-log', { params: { bookId: route.params.id } })
    shelfEntry.value = res.data
  } catch { shelfEntry.value = null }
}

async function addToShelf(status: string) {
  shelfLoading.value = true
  shelfMsg.value = { text: '', ok: true }
  try {
    const res = await api.post('/reading-log', { bookId: Number(route.params.id), status })
    shelfEntry.value = res.data
    shelfMsg.value = { text: 'Added to shelf!', ok: true }
  } catch (e: any) {
    shelfMsg.value = { text: e.response?.data ?? 'Failed', ok: false }
  } finally { shelfLoading.value = false }
}

async function updateShelfStatus(newStatus: string) {
  if (!shelfEntry.value) return
  shelfLoading.value = true
  try {
    const res = await api.put(`/reading-log/${shelfEntry.value.id}`, { status: newStatus })
    shelfEntry.value = res.data
    shelfMsg.value = { text: 'Status updated!', ok: true }
  } catch (e: any) {
    shelfMsg.value = { text: e.response?.data ?? 'Failed', ok: false }
  } finally { shelfLoading.value = false }
}

async function updatePages() {
  if (!shelfEntry.value) return
  try {
    const res = await api.put(`/reading-log/${shelfEntry.value.id}`, { pagesRead: shelfEntry.value.pagesRead })
    shelfEntry.value = res.data
  } catch {}
}

function progressPercent() {
  const total = book.value?.pageCount
  if (!total || !shelfEntry.value?.pagesRead) return 0
  return Math.min(100, Math.round((shelfEntry.value.pagesRead / total) * 100))
}

// Star rating options: 0.25, 0.50, … 5.00
const ratingOptions = Array.from({ length: 20 }, (_, i) => +((i + 1) * 0.25).toFixed(2))

// Review form state
const newRating = ref<number>(3)
const newContent = ref('')
const reviewMsg = ref({ text: '', ok: true })

async function loadReviews() {
  const res = await api.get('/reviews', { params: { bookId: route.params.id } })
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
  reviewMsg.value = { text: '', ok: true }
  if (!user.value) { reviewMsg.value = { text: 'You must be logged in to post a review.', ok: false }; return }
  try {
    await api.post('/reviews', {
      bookId: Number(route.params.id),
      rating: newRating.value,
      content: newContent.value.trim() || null
    })
    reviewMsg.value = { text: 'Review posted!', ok: true }
    newRating.value = 3
    newContent.value = ''
    await loadReviews()
  } catch (e: any) {
    reviewMsg.value = { text: e.response?.data ?? 'Failed to post review.', ok: false }
  }
}

async function reportReview(reviewId: number) {
  if (!user.value) { alert('You must be logged in to report'); return }
  const reason = prompt('Reason for report?')
  if (!reason) return
  await api.post('/reports', { reviewId, reason })
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
  <div style="max-width:800px; margin:0 auto; padding:1rem;">
    <div v-if="error" style="color:red;">{{ error }}</div>

    <div v-else-if="book">
      <RouterLink to="/books">← Back to Books</RouterLink>
      <h1>{{ book.title }}</h1>
      <p v-if="book.author"><strong>Author:</strong>
        <RouterLink :to="`/authors/${book.author.id}`">{{ book.author.firstName }} {{ book.author.lastName }}</RouterLink>
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
      <div v-if="user" style="margin-top:1.5rem; padding:1rem; border:1px solid #bfdbfe; border-radius:8px; background:#eff6ff;">
        <h3 style="margin:0 0 0.75rem; color:#1d4ed8;">📚 My Reading Status</h3>

        <!-- Not on shelf -->
        <div v-if="!shelfEntry">
          <p style="color:#6b7280; margin:0 0 0.75rem; font-size:0.95rem;">This book isn't on your shelf yet.</p>
          <div style="display:flex; gap:0.5rem; flex-wrap:wrap;">
            <button @click="addToShelf('TO_READ')" :disabled="shelfLoading"
              style="padding:7px 14px; background:#fef08a; color:#854d0e; border:1px solid #fde047; border-radius:6px; cursor:pointer; font-size:0.9rem;">
              🔖 Want to Read
            </button>
            <button @click="addToShelf('READING')" :disabled="shelfLoading"
              style="padding:7px 14px; background:#2563eb; color:white; border:none; border-radius:6px; cursor:pointer; font-size:0.9rem;">
              📖 Start Reading
            </button>
            <button @click="addToShelf('FINISHED')" :disabled="shelfLoading"
              style="padding:7px 14px; background:#16a34a; color:white; border:none; border-radius:6px; cursor:pointer; font-size:0.9rem;">
              ✅ Mark as Finished
            </button>
          </div>
        </div>

        <!-- On shelf -->
        <div v-else>
          <div style="display:flex; align-items:center; gap:1rem; flex-wrap:wrap; margin-bottom:0.75rem;">
            <select :value="shelfEntry.status" @change="updateShelfStatus(($event.target as HTMLSelectElement).value)"
              style="padding:7px 12px; border:1px solid #bfdbfe; border-radius:6px; font-size:0.95rem; cursor:pointer; background:white;">
              <option value="TO_READ">🔖 Want to Read</option>
              <option value="READING">📖 Reading</option>
              <option value="FINISHED">✅ Finished</option>
              <option value="DNF">❌ Did Not Finish</option>
            </select>
            <RouterLink :to="`/journal?readingLogId=${shelfEntry.id}`"
              style="padding:7px 14px; background:#f0fdf4; color:#16a34a; border:1px solid #bbf7d0; border-radius:6px; text-decoration:none; font-size:0.9rem;">
              📖 Activity Log
            </RouterLink>
          </div>

          <!-- Progress tracking when reading -->
          <div v-if="shelfEntry.status === 'READING' && book.pageCount" style="margin-top:0.5rem;">
            <div style="display:flex; align-items:center; gap:8px; margin-bottom:6px;">
              <input type="number" v-model.number="shelfEntry.pagesRead"
                :max="book.pageCount" min="0" @change="updatePages()"
                style="width:70px; padding:5px 8px; border:1px solid #d1d5db; border-radius:6px;" />
              <span style="font-size:0.9rem; color:#6b7280;">/ {{ book.pageCount }} pages · <strong style="color:#2563eb;">{{ progressPercent() }}%</strong></span>
            </div>
            <div style="height:6px; background:#e5e7eb; border-radius:3px; overflow:hidden;">
              <div :style="{ width: progressPercent() + '%', height:'100%', background:'#2563eb', transition:'width 0.3s' }"></div>
            </div>
          </div>

          <div v-if="shelfEntry.startedAt" style="font-size:0.8rem; color:#9ca3af; margin-top:6px;">
            Started: {{ shelfEntry.startedAt }}
            <span v-if="shelfEntry.finishedAt"> · Finished: {{ shelfEntry.finishedAt }}</span>
          </div>
        </div>

        <p v-if="shelfMsg.text" :style="{ color: shelfMsg.ok ? '#16a34a' : '#dc2626', margin:'8px 0 0', fontSize:'0.9rem' }">
          {{ shelfMsg.text }}
        </p>
      </div>

      <!-- Reviews list -->
      <h2 style="margin-top:1.5rem;">Reviews ({{ reviews.length }})</h2>
      <ul v-if="reviews.length" style="list-style:none; padding:0;">
        <li v-for="r in reviews" :key="r.id"
            style="padding:10px; border:1px solid #e5e7eb; border-radius:6px; margin-bottom:8px;">
          <span style="font-size:1.1rem; color:#f59e0b;">{{ renderStars(r.rating) }}</span>
          <strong style="margin-left:6px;">{{ r.rating.toFixed(2) }} / 5</strong>
          <em style="color:#6b7280; margin-left:8px;">({{ r.reader?.username }})</em>
          <p v-if="r.content" style="margin:4px 0 4px;">{{ r.content }}</p>
          <button @click="reportReview(r.id)"
                  style="font-size:0.8rem; padding:2px 8px; background:none; border:1px solid #d1d5db; border-radius:4px; cursor:pointer; color:#6b7280;">
            🚩 Report
          </button>
        </li>
      </ul>
      <p v-else style="color:gray;">No reviews yet. Be the first!</p>

      <!-- Add review form -->
      <div style="margin-top:1.5rem; padding:1rem; border:1px solid #e5e7eb; border-radius:8px; background:#f9fafb;">
        <h3 style="margin-top:0;">✍️ Write a Review</h3>

        <div v-if="!user" style="color:#6b7280;">
          <RouterLink to="/login">Log in</RouterLink> to post a review.
        </div>

        <div v-else>
          <label><strong>Rating:</strong></label><br/>
          <select v-model="newRating" style="padding:6px 12px; margin:8px 0; font-size:1rem; border:1px solid #d1d5db; border-radius:6px; cursor:pointer;">
            <option v-for="val in ratingOptions" :key="val" :value="val">
              {{ val.toFixed(2) }} ★
            </option>
          </select>
          <p style="margin:4px 0 10px; font-size:1.2rem; color:#f59e0b;">
            {{ renderStars(newRating) }} <strong>{{ newRating.toFixed(2) }} / 5</strong>
          </p>

          <label><strong>Comment (optional):</strong></label><br/>
          <textarea v-model="newContent" rows="3" placeholder="Share your thoughts…"
                    style="width:100%; padding:6px; margin-top:4px; font-family:inherit; resize:vertical; box-sizing:border-box;"></textarea>

          <p :style="{color: reviewMsg.ok ? 'green' : 'red', margin:'6px 0'}">{{ reviewMsg.text }}</p>
          <button @click="submitReview"
                  style="padding:8px 20px; background:#2563eb; color:white; border:none; border-radius:6px; cursor:pointer;">
            Post Review
          </button>
        </div>
      </div>
    </div>

    <p v-else>Loading...</p>
  </div>
</template>
