<script setup lang="ts">
import {ref, computed, onMounted} from 'vue'
import api from '@/api'

const series = ref<any[]>([])
const search = ref('')

onMounted(async () => {
  document.title = 'Series — BookVault'
  const res = await api.get('/series')
  series.value = res.data
})

const filtered = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return series.value
  return series.value.filter(s =>
    s.name?.toLowerCase().includes(q) ||
    s.author?.firstName?.toLowerCase().includes(q) ||
    s.author?.lastName?.toLowerCase().includes(q)
  )
})

function volumeDots(count: number) {
  const n = Math.min(count ?? 0, 12)
  return '📗'.repeat(n) + (count > 12 ? ` +${count - 12}` : '')
}

function initials(s: any) {
  const a = s.author
  if (!a) return '?'
  return `${a.firstName?.[0] ?? ''}${a.lastName?.[0] ?? ''}`.toUpperCase()
}
</script>

<template>
  <div class="series-view">

    <div class="series-header">
      <h1><span aria-hidden="true">📚</span> Series</h1>
      <span class="series-count" aria-live="polite">{{ series.length }} series</span>
    </div>

    <div role="search">
      <label for="series-search" class="visually-hidden">Search series by name or author</label>
      <input
        id="series-search"
        v-model="search"
        type="search"
        placeholder="Search by name or author…"
        class="series-search"
        aria-label="Search series by name or author"
      />
    </div>

    <p v-if="filtered.length === 0" class="series-empty" role="status">No series match your search.</p>

    <div class="series-grid" role="list" aria-label="Series">
      <RouterLink
        v-for="s in filtered" :key="s.id"
        :to="`/series/${s.id}`"
        class="series-card"
        role="listitem"
        :aria-label="`${s.name}${s.author ? ' by ' + s.author.firstName + ' ' + s.author.lastName : ''}, ${s.volumeCount ?? '?'} volumes`"
      >
        <!-- Left accent stripe + volume count bubble -->
        <div class="series-stripe" aria-hidden="true">
          <div class="series-vol-bubble">{{ s.volumeCount ?? '?' }}</div>
          <div class="series-vol-label">vol.</div>
        </div>

        <!-- Main content -->
        <div class="series-body">
          <div class="series-name" aria-hidden="true">{{ s.name }}</div>

          <div v-if="s.author" class="series-author" aria-hidden="true">
            <div class="series-author-avatar">{{ initials(s) }}</div>
            <span>{{ s.author.firstName }} {{ s.author.lastName }}</span>
          </div>
          <div v-else class="series-author series-author--none" aria-hidden="true">Unknown author</div>

          <div class="series-dots" aria-hidden="true">
            {{ volumeDots(s.volumeCount) }}
          </div>
        </div>

        <div class="series-arrow" aria-hidden="true">→</div>
      </RouterLink>
    </div>

  </div>
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

.series-view {
  max-width: 960px;
  margin: 0 auto;
  padding: 1.5rem 1rem;
}

.series-header {
  display: flex;
  align-items: baseline;
  gap: 1rem;
  margin-bottom: 1.25rem;
}

.series-header h1 { margin: 0; color: #fabd2f; }
.series-count { font-size: 0.9rem; color: #7c6f64; }

/* ── Search ──────────────────────────────────────────────────── */
.series-search {
  width: 100%;
  padding: 10px 14px;
  margin-bottom: 1.25rem;
  font-size: 0.95rem;
  background: #32302f;
  color: #ebdbb2;
  border: 1px solid #504945;
  border-radius: 8px;
  box-sizing: border-box;
  transition: border-color 0.15s;
}
.series-search::placeholder { color: #7c6f64; }
.series-search:focus-visible { outline: 3px solid #83a598; outline-offset: 1px; border-color: #83a598; }

.series-empty { color: #a89984; font-style: italic; margin-top: 1rem; }

/* ── Grid ────────────────────────────────────────────────────── */
.series-grid {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

/* ── Card ────────────────────────────────────────────────────── */
.series-card {
  display: flex;
  align-items: stretch;
  gap: 0;
  background: #3c3836;
  border: 1px solid #504945;
  border-radius: 12px;
  text-decoration: none;
  color: inherit;
  overflow: hidden;
  transition: border-color 0.15s, background 0.15s;
}

.series-card:hover {
  border-color: #fabd2f;
  background: #32302f;
}

/* ── Left colour stripe ──────────────────────────────────────── */
.series-stripe {
  flex-shrink: 0;
  width: 64px;
  background: #458588;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  padding: 1rem 0;
}

.series-vol-bubble {
  font-size: 1.5rem;
  font-weight: 800;
  color: #1d2021;
  line-height: 1;
}

.series-vol-label {
  font-size: 0.7rem;
  color: #1d2021;
  opacity: 0.75;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* ── Body ────────────────────────────────────────────────────── */
.series-body {
  flex: 1;
  padding: 0.9rem 1.1rem;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.series-name {
  font-size: 1.1rem;
  font-weight: 700;
  color: #d5c4a1;
  transition: color 0.15s;
}

.series-card:hover .series-name { color: #fabd2f; }

/* ── Author row ──────────────────────────────────────────────── */
.series-author {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 0.85rem;
  color: #83a598;
}

.series-author--none { color: #665c54; font-style: italic; }

.series-author-avatar {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #504945;
  color: #d5c4a1;
  font-size: 0.7rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* ── Volume dots ─────────────────────────────────────────────── */
.series-dots {
  font-size: 0.9rem;
  letter-spacing: 1px;
  line-height: 1.2;
  color: #7c6f64;
}

/* ── Arrow ───────────────────────────────────────────────────── */
.series-arrow {
  flex-shrink: 0;
  color: #665c54;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
  padding-right: 1.1rem;
  transition: color 0.15s, transform 0.15s;
}

.series-card:hover .series-arrow {
  color: #fabd2f;
  transform: translateX(3px);
}
</style>
