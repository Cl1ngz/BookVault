<script setup lang="ts">
import {ref, onMounted, computed} from 'vue'
import api from '@/api'


const totalBooks = ref(0)
const totalAuthors = ref(0)
const totalSeries = ref(0)
const totalGenres = ref(0)
const recentBooks = ref<any[]>([])

const user = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('user') || 'null')
  } catch {
    return null
  }
})
const isLoggedIn = computed(() => !!user.value?.token)

onMounted(async () => {
  try {
    const [booksRes, authorsRes, seriesRes, genresRes] = await Promise.all([
      api.get('/books'),
      api.get('/authors'),
      api.get('/series'),
      api.get('/genres'),
    ])
    totalBooks.value = booksRes.data.length
    totalAuthors.value = authorsRes.data.length
    totalSeries.value = seriesRes.data.length
    totalGenres.value = genresRes.data.length
    // newest 6 books (by array position - backend returns all)
    recentBooks.value = [...booksRes.data].slice(-6).reverse()
  } catch { /* silent */
  }
})
</script>

<template>
  <div>
    <!-- Hero -->
    <div class="hero">
      <h1>📚 BookVault</h1>
      <p class="hero-subtitle">
        Your personal reading tracker. Discover books, track your progress, and keep a reading journal.
      </p>
      <div class="hero-actions">
        <RouterLink to="/books" class="hero-btn-primary">Browse Books</RouterLink>
        <template v-if="!isLoggedIn">
          <RouterLink to="/register" class="hero-btn-secondary">Create Account</RouterLink>
          <RouterLink to="/login" class="hero-btn-ghost">Log In</RouterLink>
        </template>
        <template v-else>
          <RouterLink to="/my-shelf" class="hero-btn-secondary">My Shelf</RouterLink>
        </template>
      </div>
    </div>

    <!-- Stats bar -->
    <div class="stats-bar">
      <div class="stats-grid">
        <div>
          <div class="stat-value stat-value--navy">{{ totalBooks }}</div>
          <div class="stat-label">Books</div>
        </div>
        <div>
          <div class="stat-value stat-value--blue">{{ totalAuthors }}</div>
          <div class="stat-label">Authors</div>
        </div>
        <div>
          <div class="stat-value stat-value--purple">{{ totalSeries }}</div>
          <div class="stat-label">Series</div>
        </div>
        <div>
          <div class="stat-value stat-value--green">{{ totalGenres }}</div>
          <div class="stat-label">Genres</div>
        </div>
      </div>
    </div>

    <!-- Features -->
    <div class="features-section">
      <h2>What you can do with BookVault</h2>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">🔍</div>
          <h3>Discover Books</h3>
          <p>Browse the catalog with powerful filters — by genre, mood, pace, page count or publication year.</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">📖</div>
          <h3>Track Your Reading</h3>
          <p>Mark books as <em>To Read</em>, <em>Currently Reading</em>, <em>Finished</em> or <em>Did Not Finish</em>
            and log your page progress.</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">📓</div>
          <h3>Reading Journal</h3>
          <p>Your journal auto-fills with every status change and progress update — just like Storygraph.</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">📊</div>
          <h3>Reading Dashboard</h3>
          <p>See your yearly reading goal progress, total pages read, and recent activity at a glance.</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">⭐</div>
          <h3>Reviews & Ratings</h3>
          <p>Leave half-star ratings and reviews for books you've read. See what other readers think.</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">📚</div>
          <h3>Series & Authors</h3>
          <p>Explore author pages and follow complete series from book one through to the final chapter.</p>
        </div>
      </div>
    </div>

    <!-- Recent books -->
    <div v-if="recentBooks.length" class="recent-section">
      <h2>Recently Added</h2>
      <div class="books-grid">
        <RouterLink v-for="book in recentBooks" :key="book.id" :to="`/books/${book.id}`" class="book-card">
          <div class="book-card-title">{{ book.title }}</div>
          <div v-if="book.author" class="book-card-author">
            {{ book.author.firstName }} {{ book.author.lastName }}
          </div>
          <div class="book-card-meta">
            <span v-if="book.publicationYear">{{ book.publicationYear }}</span>
            <span v-if="book.pageCount"> · {{ book.pageCount }}p</span>
          </div>
          <div v-if="book.genres?.length" class="book-card-genres">
            <span v-for="g in book.genres.slice(0, 3)" :key="g.id" class="genre-tag">
              {{ g.name }}
            </span>
          </div>
        </RouterLink>
      </div>
      <div class="view-all">
        <RouterLink to="/books">View all books →</RouterLink>
      </div>
    </div>

    <!-- CTA for guests -->
    <div v-if="!isLoggedIn" class="cta-section">
      <h2>Ready to start tracking?</h2>
      <p>Create a free account and start your reading journey today.</p>
      <RouterLink to="/register" class="btn-cta">Get Started Free</RouterLink>
    </div>
  </div>
</template>

<style scoped>
.hero {
  background: linear-gradient(135deg, #1e3a5f 0%, #2563eb 100%);
  color: white;
  padding: 4rem 1rem;
  text-align: center;
}

.hero h1 {
  font-size: 2.8rem;
  margin: 0 0 0.5rem;
  font-weight: 800;
  letter-spacing: -1px;
}

.hero-subtitle {
  font-size: 1.2rem;
  margin: 0 0 2rem;
  opacity: 0.88;
  max-width: 540px;
  margin-left: auto;
  margin-right: auto;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.hero-btn-primary {
  padding: 12px 28px;
  background: white;
  color: #1e3a5f;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 700;
  font-size: 1rem;
}

.hero-btn-secondary {
  padding: 12px 28px;
  background: rgba(255, 255, 255, 0.15);
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.5);
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  font-size: 1rem;
}

.hero-btn-ghost {
  padding: 12px 28px;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  font-size: 1rem;
}

.stats-bar {
  background: #f8fafc;
  border-bottom: 1px solid #e5e7eb;
  padding: 1.5rem 1rem;
}

.stats-grid {
  max-width: 900px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 1rem;
  text-align: center;
}

.stat-value {
  font-size: 2rem;
  font-weight: 800;
}

.stat-value--navy {
  color: #1e3a5f;
}

.stat-value--blue {
  color: #2563eb;
}

.stat-value--purple {
  color: #7c3aed;
}

.stat-value--green {
  color: #059669;
}

.stat-label {
  color: #6b7280;
  font-size: 0.9rem;
}

.features-section {
  max-width: 900px;
  margin: 3rem auto;
  padding: 0 1rem;
}

.features-section h2 {
  text-align: center;
  color: #1e3a5f;
  margin-bottom: 2rem;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1.5rem;
}

.feature-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 1.5rem;
  background: white;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.feature-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.feature-card h3 {
  margin: 0 0 0.5rem;
  color: #1e3a5f;
}

.feature-card p {
  color: #6b7280;
  font-size: 0.9rem;
  margin: 0;
}

.recent-section {
  max-width: 900px;
  margin: 0 auto 3rem;
  padding: 0 1rem;
}

.recent-section h2 {
  color: #1e3a5f;
  margin-bottom: 1.25rem;
}

.books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 1rem;
}

.book-card {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 1rem;
  background: white;
  text-decoration: none;
  color: #111;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  display: block;
}

.book-card-title {
  font-weight: 700;
  margin-bottom: 2px;
}

.book-card-author {
  font-size: 0.85rem;
  color: #6b7280;
}

.book-card-meta {
  font-size: 0.8rem;
  color: #9ca3af;
  margin-top: 4px;
}

.book-card-genres {
  margin-top: 6px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.genre-tag {
  padding: 1px 7px;
  background: #eff6ff;
  color: #2563eb;
  border-radius: 999px;
  font-size: 0.75rem;
}

.view-all {
  text-align: center;
  margin-top: 1.25rem;
}

.view-all a {
  color: #2563eb;
  text-decoration: none;
  font-weight: 600;
}

.cta-section {
  background: #eff6ff;
  border-top: 1px solid #bfdbfe;
  padding: 2.5rem 1rem;
  text-align: center;
}

.cta-section h2 {
  color: #1e3a5f;
  margin: 0 0 0.5rem;
}

.cta-section p {
  color: #4b5563;
  margin: 0 0 1.5rem;
}

.btn-cta {
  padding: 12px 32px;
  background: #2563eb;
  color: white;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 700;
  font-size: 1rem;
}
</style>

