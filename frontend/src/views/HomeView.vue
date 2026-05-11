<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import api from '@/api'

const totalBooks   = ref(0)
const totalAuthors = ref(0)
const totalSeries  = ref(0)
const totalGenres  = ref(0)
const recentBooks  = ref<any[]>([])

const user = computed(() => {
  try { return JSON.parse(localStorage.getItem('user') || 'null') } catch { return null }
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
    totalBooks.value   = booksRes.data.length
    totalAuthors.value = authorsRes.data.length
    totalSeries.value  = seriesRes.data.length
    totalGenres.value  = genresRes.data.length
    // newest 6 books (by array position - backend returns all)
    recentBooks.value  = [...booksRes.data].slice(-6).reverse()
  } catch { /* silent */ }
})
</script>

<template>
  <div>
    <!-- Hero -->
    <div style="background: linear-gradient(135deg, #1e3a5f 0%, #2563eb 100%); color:white; padding: 4rem 1rem; text-align:center;">
      <h1 style="font-size:2.8rem; margin:0 0 0.5rem; font-weight:800; letter-spacing:-1px;">📚 BookVault</h1>
      <p style="font-size:1.2rem; margin:0 0 2rem; opacity:0.88; max-width:540px; margin-left:auto; margin-right:auto;">
        Your personal reading tracker. Discover books, track your progress, and keep a reading journal.
      </p>
      <div style="display:flex; justify-content:center; gap:1rem; flex-wrap:wrap;">
        <RouterLink to="/books"
          style="padding:12px 28px; background:white; color:#1e3a5f; border-radius:8px; text-decoration:none; font-weight:700; font-size:1rem;">
          Browse Books
        </RouterLink>
        <template v-if="!isLoggedIn">
          <RouterLink to="/register"
            style="padding:12px 28px; background:rgba(255,255,255,0.15); color:white; border:2px solid rgba(255,255,255,0.5); border-radius:8px; text-decoration:none; font-weight:600; font-size:1rem;">
            Create Account
          </RouterLink>
          <RouterLink to="/login"
            style="padding:12px 28px; background:rgba(255,255,255,0.1); color:white; border:2px solid rgba(255,255,255,0.3); border-radius:8px; text-decoration:none; font-weight:600; font-size:1rem;">
            Log In
          </RouterLink>
        </template>
        <template v-else>
          <RouterLink to="/my-shelf"
            style="padding:12px 28px; background:rgba(255,255,255,0.15); color:white; border:2px solid rgba(255,255,255,0.5); border-radius:8px; text-decoration:none; font-weight:600; font-size:1rem;">
            My Shelf
          </RouterLink>
        </template>
      </div>
    </div>

    <!-- Stats bar -->
    <div style="background:#f8fafc; border-bottom:1px solid #e5e7eb; padding:1.5rem 1rem;">
      <div style="max-width:900px; margin:0 auto; display:grid; grid-template-columns:repeat(auto-fit,minmax(160px,1fr)); gap:1rem; text-align:center;">
        <div>
          <div style="font-size:2rem; font-weight:800; color:#1e3a5f;">{{ totalBooks }}</div>
          <div style="color:#6b7280; font-size:0.9rem;">Books</div>
        </div>
        <div>
          <div style="font-size:2rem; font-weight:800; color:#2563eb;">{{ totalAuthors }}</div>
          <div style="color:#6b7280; font-size:0.9rem;">Authors</div>
        </div>
        <div>
          <div style="font-size:2rem; font-weight:800; color:#7c3aed;">{{ totalSeries }}</div>
          <div style="color:#6b7280; font-size:0.9rem;">Series</div>
        </div>
        <div>
          <div style="font-size:2rem; font-weight:800; color:#059669;">{{ totalGenres }}</div>
          <div style="color:#6b7280; font-size:0.9rem;">Genres</div>
        </div>
      </div>
    </div>

    <!-- Features -->
    <div style="max-width:900px; margin:3rem auto; padding:0 1rem;">
      <h2 style="text-align:center; color:#1e3a5f; margin-bottom:2rem;">What you can do with BookVault</h2>
      <div style="display:grid; grid-template-columns:repeat(auto-fit, minmax(240px,1fr)); gap:1.5rem;">
        <div style="border:1px solid #e5e7eb; border-radius:12px; padding:1.5rem; background:white; box-shadow:0 1px 4px rgba(0,0,0,0.05);">
          <div style="font-size:2rem; margin-bottom:0.5rem;">🔍</div>
          <h3 style="margin:0 0 0.5rem; color:#1e3a5f;">Discover Books</h3>
          <p style="color:#6b7280; font-size:0.9rem; margin:0;">Browse the catalog with powerful filters — by genre, mood, pace, page count or publication year.</p>
        </div>
        <div style="border:1px solid #e5e7eb; border-radius:12px; padding:1.5rem; background:white; box-shadow:0 1px 4px rgba(0,0,0,0.05);">
          <div style="font-size:2rem; margin-bottom:0.5rem;">📖</div>
          <h3 style="margin:0 0 0.5rem; color:#1e3a5f;">Track Your Reading</h3>
          <p style="color:#6b7280; font-size:0.9rem; margin:0;">Mark books as <em>To Read</em>, <em>Currently Reading</em>, <em>Finished</em> or <em>Did Not Finish</em> and log your page progress.</p>
        </div>
        <div style="border:1px solid #e5e7eb; border-radius:12px; padding:1.5rem; background:white; box-shadow:0 1px 4px rgba(0,0,0,0.05);">
          <div style="font-size:2rem; margin-bottom:0.5rem;">📓</div>
          <h3 style="margin:0 0 0.5rem; color:#1e3a5f;">Reading Journal</h3>
          <p style="color:#6b7280; font-size:0.9rem; margin:0;">Your journal auto-fills with every status change and progress update — just like Storygraph.</p>
        </div>
        <div style="border:1px solid #e5e7eb; border-radius:12px; padding:1.5rem; background:white; box-shadow:0 1px 4px rgba(0,0,0,0.05);">
          <div style="font-size:2rem; margin-bottom:0.5rem;">📊</div>
          <h3 style="margin:0 0 0.5rem; color:#1e3a5f;">Reading Dashboard</h3>
          <p style="color:#6b7280; font-size:0.9rem; margin:0;">See your yearly reading goal progress, total pages read, and recent activity at a glance.</p>
        </div>
        <div style="border:1px solid #e5e7eb; border-radius:12px; padding:1.5rem; background:white; box-shadow:0 1px 4px rgba(0,0,0,0.05);">
          <div style="font-size:2rem; margin-bottom:0.5rem;">⭐</div>
          <h3 style="margin:0 0 0.5rem; color:#1e3a5f;">Reviews & Ratings</h3>
          <p style="color:#6b7280; font-size:0.9rem; margin:0;">Leave half-star ratings and reviews for books you've read. See what other readers think.</p>
        </div>
        <div style="border:1px solid #e5e7eb; border-radius:12px; padding:1.5rem; background:white; box-shadow:0 1px 4px rgba(0,0,0,0.05);">
          <div style="font-size:2rem; margin-bottom:0.5rem;">📚</div>
          <h3 style="margin:0 0 0.5rem; color:#1e3a5f;">Series & Authors</h3>
          <p style="color:#6b7280; font-size:0.9rem; margin:0;">Explore author pages and follow complete series from book one through to the final chapter.</p>
        </div>
      </div>
    </div>

    <!-- Recent books -->
    <div v-if="recentBooks.length" style="max-width:900px; margin:0 auto 3rem; padding:0 1rem;">
      <h2 style="color:#1e3a5f; margin-bottom:1.25rem;">Recently Added</h2>
      <div style="display:grid; grid-template-columns:repeat(auto-fill,minmax(240px,1fr)); gap:1rem;">
        <RouterLink v-for="book in recentBooks" :key="book.id" :to="`/books/${book.id}`"
          style="border:1px solid #e5e7eb; border-radius:10px; padding:1rem; background:white; text-decoration:none; color:#111; box-shadow:0 1px 3px rgba(0,0,0,0.06); display:block;">
          <div style="font-weight:700; margin-bottom:2px;">{{ book.title }}</div>
          <div v-if="book.author" style="font-size:0.85rem; color:#6b7280;">
            {{ book.author.firstName }} {{ book.author.lastName }}
          </div>
          <div style="font-size:0.8rem; color:#9ca3af; margin-top:4px;">
            <span v-if="book.publicationYear">{{ book.publicationYear }}</span>
            <span v-if="book.pageCount"> · {{ book.pageCount }}p</span>
          </div>
          <div v-if="book.genres?.length" style="margin-top:6px; display:flex; flex-wrap:wrap; gap:4px;">
            <span v-for="g in book.genres.slice(0,3)" :key="g.id"
              style="padding:1px 7px; background:#eff6ff; color:#2563eb; border-radius:999px; font-size:0.75rem;">
              {{ g.name }}
            </span>
          </div>
        </RouterLink>
      </div>
      <div style="text-align:center; margin-top:1.25rem;">
        <RouterLink to="/books" style="color:#2563eb; text-decoration:none; font-weight:600;">
          View all books →
        </RouterLink>
      </div>
    </div>

    <!-- CTA for guests -->
    <div v-if="!isLoggedIn"
      style="background:#eff6ff; border-top:1px solid #bfdbfe; padding:2.5rem 1rem; text-align:center;">
      <h2 style="color:#1e3a5f; margin:0 0 0.5rem;">Ready to start tracking?</h2>
      <p style="color:#4b5563; margin:0 0 1.5rem;">Create a free account and start your reading journey today.</p>
      <RouterLink to="/register"
        style="padding:12px 32px; background:#2563eb; color:white; border-radius:8px; text-decoration:none; font-weight:700; font-size:1rem;">
        Get Started Free
      </RouterLink>
    </div>
  </div>
</template>

