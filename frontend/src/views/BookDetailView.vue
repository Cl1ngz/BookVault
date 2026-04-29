<script setup lang="ts">
import {ref, onMounted} from 'vue'
import {useRoute} from 'vue-router'
import api from '@/api'

const route = useRoute()
const book = ref<any>(null)
const reviews = ref<any[]>([])

onMounted(async () => {
  const [bookRes, reviewRes] = await Promise.all([
    api.get(`/book/${route.params.id}`),
    api.get(`/reviews`, {params: {bookId: route.params.id}})
  ])
  book.value = bookRes.data
  reviews.value = reviewRes.data
})

const user = JSON.parse(localStorage.getItem('user') || 'null')

async function reportReview(reviewId: number) {
  if (!user) {
    alert('You must be logged in to report');
    return
  }
  const reason = prompt('Reason for report?')
  if (!reason) return
  await api.post('/reports', {
    reviewId,
    reporterType: 'reader',
    reporterId: user.id,
    reason
  })
  alert('Report submitted!')
}

</script>

<template>
  <div v-if="book">
    <RouterLink to="/books">← Back to Books</RouterLink>
    <h1>{{ book.tytul }}</h1>
    <p><strong>Author:</strong> {{ book.author?.firstName }} {{ book.author?.lastName }}</p>
    <p><strong>Publisher:</strong> {{ book.publisher?.name }}</p>
    <p><strong>Year:</strong> {{ book.publicationYear }}</p>
    <p><strong>Pages:</strong> {{ book.pageCount }}</p>
    <p><strong>Mood:</strong> {{ book.mood }}</p>
    <p><strong>Genres:</strong> {{ book.genres?.map((g: any) => g.nazwa).join(', ') }}</p>

    <h2>Reviews</h2>
    <ul v-if="reviews.length">
      <li v-for="r in reviews" :key="r.id">
        ⭐ {{ r.rating }}/5 — {{ r.content }} <em>({{ r.reader?.username }})</em>
      </li>
      <li v-for="r in reviews" :key="r.id">
        ⭐ {{ r.rating }}/5 — {{ r.content }}
        <em>({{ r.reader?.username }})</em>
        <button @click="reportReview(r.id)">🚩 Report</button>
      </li>
    </ul>
    <p v-else>No reviews yet.</p>
  </div>
  <p v-else>Loading...</p>
</template>
