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
  document.title = 'BookVault — Your Personal Reading Tracker'
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
    <section class="hero" aria-label="BookVault hero">
      <h1><span aria-hidden="true">📚</span> BookVault</h1>
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
    </section>

    <!-- Stats bar -->
    <section class="stats-bar" aria-label="Library statistics">
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
    </section>

    <!-- Features -->
    <section class="features-section" aria-label="Features">
      <h2>What you can do with BookVault</h2>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon" aria-hidden="true">🔍</div>
          <h3>Discover Books</h3>
          <p>Browse the catalog with powerful filters — by genre, mood, pace, page count or publication year.</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon" aria-hidden="true">📖</div>
          <h3>Track Your Reading</h3>
          <p>Mark books as <em>To Read</em>, <em>Currently Reading</em>, <em>Finished</em> or <em>Did Not Finish</em>
            and log your page progress.</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon" aria-hidden="true">📓</div>
          <h3>Reading Journal</h3>
          <p>Your journal auto-fills with every status change and progress update — just like Storygraph.</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon" aria-hidden="true">📊</div>
          <h3>Reading Dashboard</h3>
          <p>See your yearly reading goal progress, total pages read, and recent activity at a glance.</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon" aria-hidden="true">⭐</div>
          <h3>Reviews & Ratings</h3>
          <p>Leave half-star ratings and reviews for books you've read. See what other readers think.</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon" aria-hidden="true">📚</div>
          <h3>Series & Authors</h3>
          <p>Explore author pages and follow complete series from book one through to the final chapter.</p>
        </div>
      </div>
    </section>

    <!-- Recent books -->
    <section v-if="recentBooks.length" class="recent-section" aria-label="Recently added books">
      <h2>Recently Added</h2>
      <div class="books-grid">
        <RouterLink
          v-for="book in recentBooks"
          :key="book.id"
          :to="`/books/${book.id}`"
          class="book-card"
          :aria-label="`${book.title}${book.author ? ` by ${book.author.firstName} ${book.author.lastName}` : ''}`"
        >
          <div class="book-card-title" aria-hidden="true">{{ book.title }}</div>
          <div v-if="book.author" class="book-card-author" aria-hidden="true">
            {{ book.author.firstName }} {{ book.author.lastName }}
          </div>
          <div class="book-card-meta" aria-hidden="true">
            <span v-if="book.publicationYear">{{ book.publicationYear }}</span>
            <span v-if="book.pageCount"> · {{ book.pageCount }}p</span>
          </div>
          <div v-if="book.genres?.length" class="book-card-genres" aria-hidden="true">
            <span v-for="g in book.genres.slice(0, 3)" :key="g.id" class="genre-tag">
              {{ g.name }}
            </span>
          </div>
        </RouterLink>
      </div>
      <div class="view-all">
        <RouterLink to="/books">View all books →</RouterLink>
      </div>
    </section>

    <!-- CTA for guests -->
    <section v-if="!isLoggedIn" class="cta-section" aria-label="Get started">
      <h2>Ready to start tracking?</h2>
      <p>Create a free account and start your reading journey today.</p>
      <RouterLink to="/register" class="btn-cta">Get Started Free</RouterLink>
    </section>
  </div>
</template>

<style scoped>
.hero {
  background: linear-gradient(135deg, #1d2021 0%, #3c3836 100%);
  color: #ebdbb2;
  padding: 4rem 1rem;
  text-align: center;
  border-bottom: 2px solid #504945;
}

.hero h1 {
  font-size: 2.8rem;
  margin: 0 0 0.5rem;
  font-weight: 800;
  letter-spacing: -1px;
  color: #fabd2f;
  text-shadow: 0 2px 8px rgba(0,0,0,0.5);
}

.hero-subtitle {
  font-size: 1.2rem;
  margin: 0 auto 2rem;
  opacity: 0.9;
  max-width: 540px;
  color: #d5c4a1;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.hero-btn-primary {
  padding: 12px 28px;
  background: #fabd2f;
  color: #1d2021;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 700;
  font-size: 1rem;
  transition: filter 0.15s;
}
.hero-btn-primary:hover { filter: brightness(1.1); }

.hero-btn-secondary {
  padding: 12px 28px;
  background: rgba(69, 133, 136, 0.2);
  color: #83a598;
  border: 2px solid #458588;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  font-size: 1rem;
  transition: background 0.15s;
}
.hero-btn-secondary:hover { background: rgba(69,133,136,0.35); }

.hero-btn-ghost {
  padding: 12px 28px;
  background: rgba(80, 73, 69, 0.4);
  color: #d5c4a1;
  border: 2px solid #665c54;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  font-size: 1rem;
  transition: border-color 0.15s;
}
.hero-btn-ghost:hover { border-color: #fabd2f; color: #fabd2f; }

.stats-bar {
  background: #32302f;
  border-bottom: 1px solid #504945;
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

.stat-value--navy   { color: #83a598; }
.stat-value--blue   { color: #8ec07c; }
.stat-value--purple { color: #d3869b; }
.stat-value--green  { color: #b8bb26; }

.stat-label {
  color: #a89984;
  font-size: 0.9rem;
}

.features-section {
  max-width: 900px;
  margin: 3rem auto;
  padding: 0 1rem;
}

.features-section h2 {
  text-align: center;
  color: #fabd2f;
  margin-bottom: 2rem;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1.5rem;
}

.feature-card {
  border: 1px solid #504945;
  border-radius: 12px;
  padding: 1.5rem;
  background: #3c3836;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  transition: border-color 0.15s;
}
.feature-card:hover { border-color: #fabd2f; }

.feature-icon {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.feature-card h3 {
  margin: 0 0 0.5rem;
  color: #d5c4a1;
}

.feature-card p {
  color: #a89984;
  font-size: 0.9rem;
  margin: 0;
}

.recent-section {
  max-width: 900px;
  margin: 0 auto 3rem;
  padding: 0 1rem;
}

.recent-section h2 {
  color: #fabd2f;
  margin-bottom: 1.25rem;
}

.books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 1rem;
}

.book-card {
  border: 1px solid #504945;
  border-radius: 10px;
  padding: 1rem;
  background: #3c3836;
  text-decoration: none;
  color: #ebdbb2;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.25);
  display: block;
  transition: border-color 0.15s;
}
.book-card:hover { border-color: #fabd2f; }

.book-card-title {
  font-weight: 700;
  margin-bottom: 2px;
  color: #ebdbb2;
}

.book-card-author {
  font-size: 0.85rem;
  color: #a89984;
}

.book-card-meta {
  font-size: 0.8rem;
  color: #7c6f64;
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
  background: #32302f;
  color: #83a598;
  border: 1px solid #458588;
  border-radius: 999px;
  font-size: 0.75rem;
}

.view-all {
  text-align: center;
  margin-top: 1.25rem;
}

.view-all a {
  color: #83a598;
  text-decoration: none;
  font-weight: 600;
}
.view-all a:hover { color: #fabd2f; }

.cta-section {
  background: #32302f;
  border-top: 1px solid #504945;
  padding: 2.5rem 1rem;
  text-align: center;
}

.cta-section h2 {
  color: #fabd2f;
  margin: 0 0 0.5rem;
}

.cta-section p {
  color: #d5c4a1;
  margin: 0 0 1.5rem;
}

.btn-cta {
  padding: 12px 32px;
  background: #458588;
  color: #ebdbb2;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 700;
  font-size: 1rem;
  transition: filter 0.15s;
}
.btn-cta:hover { filter: brightness(1.15); }
</style>

