<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api'

const route = useRoute()
const book = ref<any>(null)
const reviews = ref<any[]>([])
const error = ref('')

const user = computed(() => JSON.parse(localStorage.getItem('user') || 'null'))

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
