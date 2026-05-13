<script setup lang="ts">
import {ref, onMounted} from 'vue'
import api from '@/api'

const authors = ref<any[]>([])

onMounted(async () => {
  const res = await api.get('/authors')
  authors.value = res.data
})

function initials(a: any) {
  return `${a.firstName?.[0] ?? ''}${a.lastName?.[0] ?? ''}`.toUpperCase()
}

function truncate(text: string | null | undefined, len = 140) {
  if (!text) return null
  return text.length > len ? text.slice(0, len).trimEnd() + '…' : text
}
</script>

<template>
  <div class="authors-view">
    <div class="authors-header">
      <h1>✍️ Authors</h1>
      <span class="authors-count">{{ authors.length }} authors</span>
    </div>

    <div class="authors-grid">
      <RouterLink
        v-for="a in authors" :key="a.id"
        :to="`/authors/${a.id}`"
        class="author-card"
      >
        <!-- Avatar -->
        <div class="author-avatar">{{ initials(a) }}</div>

        <!-- Info -->
        <div class="author-info">
          <div class="author-name">{{ a.firstName }} {{ a.lastName }}</div>

          <div class="author-meta">
            <span v-if="a.nationality" class="author-nationality">
              🌍 {{ a.nationality }}
            </span>
            <span v-if="a.birthDate" class="author-birth">
              · 🎂 {{ new Date(a.birthDate).getFullYear() }}
            </span>
          </div>

          <p v-if="a.biography" class="author-bio">{{ truncate(a.biography) }}</p>
          <p v-else class="author-bio author-bio--empty">No biography available.</p>
        </div>

        <div class="author-arrow">→</div>
      </RouterLink>
    </div>
  </div>
</template>

<style scoped>
.authors-view {
  max-width: 960px;
  margin: 0 auto;
  padding: 1.5rem 1rem;
}

.authors-header {
  display: flex;
  align-items: baseline;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.authors-header h1 {
  margin: 0;
  color: #fabd2f;
}

.authors-count {
  font-size: 0.9rem;
  color: #7c6f64;
}

/* ── Grid ────────────────────────────────────────────────────── */
.authors-grid {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

/* ── Card ────────────────────────────────────────────────────── */
.author-card {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1rem 1.25rem;
  background: #3c3836;
  border: 1px solid #504945;
  border-radius: 12px;
  text-decoration: none;
  color: inherit;
  transition: border-color 0.15s, background 0.15s;
}

.author-card:hover {
  border-color: #fabd2f;
  background: #32302f;
}

/* ── Avatar ──────────────────────────────────────────────────── */
.author-avatar {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #458588;
  color: #1d2021;
  font-size: 1.1rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: 0.5px;
}

/* ── Info ────────────────────────────────────────────────────── */
.author-info {
  flex: 1;
  min-width: 0;
}

.author-name {
  font-size: 1.05rem;
  font-weight: 700;
  color: #d5c4a1;
  margin-bottom: 3px;
}

.author-card:hover .author-name {
  color: #fabd2f;
}

.author-meta {
  font-size: 0.82rem;
  color: #a89984;
  margin-bottom: 6px;
}

.author-nationality { color: #83a598; }
.author-birth { color: #a89984; }

.author-bio {
  font-size: 0.85rem;
  color: #a89984;
  line-height: 1.5;
  margin: 0;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.author-bio--empty {
  font-style: italic;
  color: #665c54;
}

/* ── Arrow ───────────────────────────────────────────────────── */
.author-arrow {
  flex-shrink: 0;
  color: #665c54;
  font-size: 1.1rem;
  align-self: center;
  transition: color 0.15s, transform 0.15s;
}

.author-card:hover .author-arrow {
  color: #fabd2f;
  transform: translateX(3px);
}
</style>
