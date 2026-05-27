<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api'

const route = useRoute()
const token = route.params.token as string

const owner = ref<string>('')
const shelf = ref<any[]>([])
const loading = ref(true)
const error = ref('')

const STATUS_LABELS: Record<string, string> = {
  TO_READ: '🔖 Want to Read',
  READING: '📖 Reading',
  FINISHED: '✅ Finished',
  DNF: '❌ Did Not Finish',
}

onMounted(async () => {
  try {
    const res = await api.get(`/shared/${token}`)
    owner.value = res.data.owner
    shelf.value = res.data.shelf
    document.title = `${owner.value}'s Shelf — BookVault`
  } catch (e: any) {
    error.value = e.response?.status === 404
        ? 'This share link is invalid or has been revoked.'
        : 'Failed to load shelf.'
  } finally {
    loading.value = false
  }
})

function booksInStatus(status: string) {
  return shelf.value.filter(e => e.status === status)
}
</script>

<template>
  <div class="shared-shelf">

    <div v-if="loading" class="state-msg" aria-live="polite">Loading shared shelf…</div>

    <div v-else-if="error" class="state-error" role="alert">
      <span aria-hidden="true">🔒</span> {{ error }}
      <RouterLink to="/books" class="back-link">← Browse Books</RouterLink>
    </div>

    <template v-else>
      <h1>
        <span aria-hidden="true">📚</span>
        {{ owner }}'s Shelf
      </h1>
      <p class="subtitle">This is a public reading shelf shared by <strong>{{ owner }}</strong>.</p>

      <div v-for="status in ['READING', 'TO_READ', 'FINISHED', 'DNF']" :key="status">
        <template v-if="booksInStatus(status).length">
          <h2 class="status-heading">{{ STATUS_LABELS[status] }}
            <span class="count">({{ booksInStatus(status).length }})</span>
          </h2>
          <ul class="book-list" :aria-label="`${STATUS_LABELS[status]} books`">
            <li v-for="entry in booksInStatus(status)" :key="entry.id" class="book-item">
              <RouterLink :to="`/books/${entry.book?.id}`" class="book-title">
                {{ entry.book?.title }}
              </RouterLink>
              <span v-if="entry.book?.author" class="book-meta">
                — {{ entry.book.author.firstName }} {{ entry.book.author.lastName }}
              </span>
              <span v-if="entry.status === 'READING' && entry.book?.pageCount" class="book-progress">
                · {{ entry.pagesRead ?? 0 }} / {{ entry.book.pageCount }} pages
              </span>
              <span v-if="entry.finishedAt" class="book-date"> · finished {{ entry.finishedAt }}</span>
            </li>
          </ul>
        </template>
      </div>

      <p v-if="shelf.length === 0" class="state-msg">This shelf is empty.</p>

      <div class="footer">
        <RouterLink to="/" class="back-link">← BookVault Home</RouterLink>
      </div>
    </template>
  </div>
</template>

<style scoped>
.shared-shelf {
  max-width: 800px;
  margin: 2rem auto;
  padding: 0 1.25rem;
}

h1 {
  color: #fabd2f;
  margin-bottom: 0.25rem;
}

.subtitle {
  color: #a89984;
  margin-bottom: 2rem;
  font-size: 0.95rem;
}

.status-heading {
  color: #83a598;
  font-size: 1.05rem;
  margin: 1.5rem 0 0.5rem;
  border-bottom: 1px solid #504945;
  padding-bottom: 4px;
}

.count {
  font-size: 0.85rem;
  color: #7c6f64;
  font-weight: normal;
}

.book-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.book-item {
  padding: 8px 12px;
  background: #3c3836;
  border: 1px solid #504945;
  border-radius: 7px;
  font-size: 0.93rem;
}

.book-title {
  color: #d5c4a1;
  text-decoration: none;
  font-weight: 600;
}
.book-title:hover { color: #fabd2f; }

.book-meta { color: #a89984; }
.book-progress { color: #83a598; }
.book-date { color: #7c6f64; font-size: 0.85rem; }

.state-msg {
  color: #a89984;
  text-align: center;
  padding: 3rem;
}

.state-error {
  color: #fb4934;
  text-align: center;
  padding: 3rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: center;
}

.back-link {
  color: #83a598;
  text-decoration: none;
  font-size: 0.9rem;
}
.back-link:hover { color: #fabd2f; }

.footer {
  margin-top: 2.5rem;
  padding-top: 1rem;
  border-top: 1px solid #504945;
}
</style>

