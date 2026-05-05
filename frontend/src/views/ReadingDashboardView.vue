<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api'

const router = useRouter()
const user = computed(() => JSON.parse(localStorage.getItem('user') || 'null'))

const stats = ref<any>({})
const goals = ref<any[]>([])
const recentJournal = ref<any[]>([])

const newGoalYear = ref(new Date().getFullYear())
const newGoalTarget = ref(12)
const goalMsg = ref({ text: '', ok: true })
const loading = ref(true)

const currentYear = new Date().getFullYear()
const currentYearGoal = computed(() => goals.value.find(g => g.year === currentYear))
const goalProgress = computed(() => {
  const goal = currentYearGoal.value
  if (!goal) return 0
  return Math.min(100, Math.round(((stats.value.finishedThisYear ?? 0) / goal.targetBooks) * 100))
})

async function loadData() {
  loading.value = true
  try {
    const [statsRes, goalsRes, journalRes] = await Promise.all([
      api.get('/reading-log/stats'),
      api.get('/reading-goals'),
      api.get('/journal'),
    ])
    stats.value = statsRes.data
    goals.value = goalsRes.data
    recentJournal.value = journalRes.data.slice(0, 5)
  } finally {
    loading.value = false
  }
}

async function saveGoal() {
  goalMsg.value = { text: '', ok: true }
  try {
    const res = await api.post('/reading-goals', {
      year: newGoalYear.value,
      targetBooks: newGoalTarget.value,
    })
    const idx = goals.value.findIndex(g => g.year === res.data.year)
    if (idx >= 0) goals.value[idx] = res.data
    else goals.value.unshift(res.data)
    goalMsg.value = { text: 'Goal saved!', ok: true }
  } catch (e: any) {
    goalMsg.value = { text: e.response?.data ?? 'Failed to save goal', ok: false }
  }
}

async function deleteGoal(goal: any) {
  if (!confirm(`Delete goal for ${goal.year}?`)) return
  await api.delete(`/reading-goals/${goal.id}`)
  goals.value = goals.value.filter(g => g.id !== goal.id)
}

onMounted(() => {
  if (!user.value) { router.push('/login'); return }
  loadData()
})
</script>

<template>
  <div style="max-width:900px; margin:0 auto; padding:1.5rem;">
    <h1 style="margin-bottom:1.5rem;">📊 Reading Dashboard</h1>

    <div v-if="loading" style="color:gray;">Loading…</div>
    <div v-else>

      <!-- Stats cards -->
      <div style="display:grid; grid-template-columns:repeat(auto-fill, minmax(150px, 1fr)); gap:1rem; margin-bottom:2rem;">
        <div v-for="card in [
          { label: '📖 Reading', value: stats.reading ?? 0, color: '#eff6ff', border: '#bfdbfe', text: '#1d4ed8' },
          { label: '🔖 Want to Read', value: stats.toRead ?? 0, color: '#fefce8', border: '#fef08a', text: '#854d0e' },
          { label: '✅ Finished', value: stats.finished ?? 0, color: '#f0fdf4', border: '#bbf7d0', text: '#15803d' },
          { label: '❌ DNF', value: stats.dnf ?? 0, color: '#fef2f2', border: '#fecaca', text: '#dc2626' },
          { label: '🗓️ This Year', value: stats.finishedThisYear ?? 0, color: '#faf5ff', border: '#e9d5ff', text: '#7e22ce' },
        ]" :key="card.label"
          :style="{
            padding: '1rem',
            borderRadius: '10px',
            background: card.color,
            border: `1px solid ${card.border}`,
            textAlign: 'center'
          }">
          <div :style="{ fontSize: '2rem', fontWeight: 'bold', color: card.text }">{{ card.value }}</div>
          <div style="font-size:0.85rem; color:#6b7280; margin-top:4px;">{{ card.label }}</div>
        </div>
      </div>

      <!-- Yearly goal widget -->
      <div style="border:1px solid #e5e7eb; border-radius:10px; padding:1.5rem; background:white; margin-bottom:2rem;">
        <h2 style="margin:0 0 1rem;">🎯 {{ currentYear }} Reading Goal</h2>

        <div v-if="currentYearGoal">
          <div style="display:flex; justify-content:space-between; margin-bottom:8px;">
            <span style="font-size:0.95rem;">
              <strong>{{ stats.finishedThisYear ?? 0 }}</strong> / <strong>{{ currentYearGoal.targetBooks }}</strong> books
            </span>
            <span style="font-weight:bold; color:#2563eb;">{{ goalProgress }}%</span>
          </div>
          <div style="height:10px; background:#e5e7eb; border-radius:5px; overflow:hidden;">
            <div :style="{ width: goalProgress + '%', height: '100%', background: '#2563eb', transition: 'width 0.5s' }"></div>
          </div>
          <p v-if="goalProgress >= 100" style="color:#16a34a; font-weight:600; margin-top:8px;">🎉 Goal achieved!</p>
        </div>
        <p v-else style="color:#6b7280; margin-bottom:0.5rem;">No goal set for {{ currentYear }}.</p>

        <!-- Set / update goal form -->
        <div style="margin-top:1.25rem; padding-top:1.25rem; border-top:1px solid #f3f4f6;">
          <h3 style="margin:0 0 0.75rem; font-size:1rem;">Set a Goal</h3>
          <div style="display:flex; gap:1rem; flex-wrap:wrap; align-items:flex-end;">
            <div>
              <label style="font-size:0.85rem; color:#6b7280;">Year</label><br/>
              <input type="number" v-model.number="newGoalYear" :min="2020" :max="2030"
                style="width:80px; padding:6px 8px; border:1px solid #d1d5db; border-radius:6px;" />
            </div>
            <div>
              <label style="font-size:0.85rem; color:#6b7280;">Books Target</label><br/>
              <input type="number" v-model.number="newGoalTarget" min="1"
                style="width:90px; padding:6px 8px; border:1px solid #d1d5db; border-radius:6px;" />
            </div>
            <button @click="saveGoal"
              style="padding:8px 20px; background:#2563eb; color:white; border:none; border-radius:6px; cursor:pointer;">
              Save Goal
            </button>
          </div>
          <p v-if="goalMsg.text" :style="{ color: goalMsg.ok ? '#16a34a' : '#dc2626', marginTop:'8px', fontSize:'0.9rem' }">
            {{ goalMsg.text }}
          </p>
        </div>
      </div>

      <!-- Past goals -->
      <div v-if="goals.length" style="border:1px solid #e5e7eb; border-radius:10px; padding:1.5rem; background:white; margin-bottom:2rem;">
        <h2 style="margin:0 0 1rem;">📅 All Goals</h2>
        <table style="width:100%; border-collapse:collapse;">
          <thead>
            <tr style="text-align:left; border-bottom:1px solid #e5e7eb;">
              <th style="padding:8px 12px; color:#6b7280; font-size:0.9rem;">Year</th>
              <th style="padding:8px 12px; color:#6b7280; font-size:0.9rem;">Target</th>
              <th style="padding:8px 12px;"></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="g in goals" :key="g.id" style="border-bottom:1px solid #f3f4f6;">
              <td style="padding:8px 12px; font-weight:600;">{{ g.year }}</td>
              <td style="padding:8px 12px;">{{ g.targetBooks }} books</td>
              <td style="padding:8px 12px;">
                <button @click="deleteGoal(g)"
                  style="padding:3px 8px; background:#fef2f2; color:#dc2626; border:1px solid #fecaca; border-radius:4px; cursor:pointer; font-size:0.8rem;">
                  Delete
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Recent reading activity -->
      <div style="border:1px solid #e5e7eb; border-radius:10px; padding:1.5rem; background:white;">
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1rem;">
          <h2 style="margin:0;">📖 Recent Activity</h2>
          <RouterLink to="/journal" style="font-size:0.9rem; color:#2563eb;">View all →</RouterLink>
        </div>
        <p v-if="!recentJournal.length" style="color:gray; font-style:italic;">No activity yet. Start reading!</p>
        <div v-for="entry in recentJournal" :key="entry.id"
          style="padding:10px 0; border-bottom:1px solid #f3f4f6;">
          <div style="font-size:0.8rem; color:#9ca3af; margin-bottom:3px;">{{ entry.entryDate }}</div>
          <RouterLink :to="`/books/${entry.readingLog?.book?.id}`"
            style="font-size:0.85rem; font-weight:600; color:#1e3a5f; text-decoration:none;">
            {{ entry.readingLog?.book?.title }}
          </RouterLink>
          <div style="font-size:0.9rem; margin-top:3px;">
            <template v-if="entry.entryType === 'STATUS_CHANGE'">
              <span v-if="entry.status === 'READING'" style="color:#2563eb; font-weight:600;">Started reading</span>
              <span v-else-if="entry.status === 'FINISHED'" style="color:#16a34a; font-weight:600;">Finished</span>
              <span v-else-if="entry.status === 'DNF'" style="color:#dc2626; font-weight:600;">Did not finish</span>
            </template>
            <template v-else>
              <span style="font-weight:700;">
                {{ entry.readingLog?.book?.pageCount
                    ? Math.round((entry.cumulativePages / entry.readingLog.book.pageCount) * 100) + '%'
                    : entry.cumulativePages + ' pages' }}
              </span>
              <span style="color:#6b7280; font-size:0.85rem; margin-left:6px;">
                {{ entry.cumulativePages }} / {{ entry.readingLog?.book?.pageCount ?? '?' }} pages
              </span>
            </template>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>


