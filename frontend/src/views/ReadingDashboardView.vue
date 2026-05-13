<script setup lang="ts">
import {ref, onMounted, computed} from 'vue'
import {useRouter} from 'vue-router'
import api from '@/api'


const router = useRouter()
const user = computed(() => JSON.parse(localStorage.getItem('user') || 'null'))

const stats = ref<any>({})
const goals = ref<any[]>([])
const recentJournal = ref<any[]>([])

const newGoalYear = ref(new Date().getFullYear())
const newGoalTarget = ref(12)
const goalMsg = ref({text: '', ok: true})
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
  goalMsg.value = {text: '', ok: true}
  try {
    const res = await api.post('/reading-goals', {
      year: newGoalYear.value,
      targetBooks: newGoalTarget.value,
    })
    const idx = goals.value.findIndex(g => g.year === res.data.year)
    if (idx >= 0) goals.value[idx] = res.data
    else goals.value.unshift(res.data)
    goalMsg.value = {text: 'Goal saved!', ok: true}
  } catch (e: any) {
    goalMsg.value = {text: e.response?.data ?? 'Failed to save goal', ok: false}
  }
}

async function deleteGoal(goal: any) {
  if (!confirm(`Delete goal for ${goal.year}?`)) return
  await api.delete(`/reading-goals/${goal.id}`)
  goals.value = goals.value.filter(g => g.id !== goal.id)
}

onMounted(() => {
  if (!user.value) {
    router.push('/login');
    return
  }
  loadData()
})
</script>

<template>
  <div class="dashboard">
    <h1>📊 Reading Dashboard</h1>

    <div v-if="loading" class="dashboard-loading">Loading…</div>
    <div v-else>

      <!-- Stats cards — background/border/text color are data-driven, kept as :style -->
      <div class="stats-grid">
        <div v-for="card in [
          { label: '📖 Reading',      value: stats.reading ?? 0,          color: '#eff6ff', border: '#bfdbfe', text: '#1d4ed8' },
          { label: '🔖 Want to Read', value: stats.toRead ?? 0,           color: '#fefce8', border: '#fef08a', text: '#854d0e' },
          { label: '✅ Finished',     value: stats.finished ?? 0,         color: '#f0fdf4', border: '#bbf7d0', text: '#15803d' },
          { label: '❌ DNF',          value: stats.dnf ?? 0,              color: '#fef2f2', border: '#fecaca', text: '#dc2626' },
          { label: '🗓️ This Year',   value: stats.finishedThisYear ?? 0, color: '#faf5ff', border: '#e9d5ff', text: '#7e22ce' },
        ]" :key="card.label"
             class="stat-card"
             :style="{ background: card.color, border: `1px solid ${card.border}` }">
          <div class="stat-value" :style="{ color: card.text }">{{ card.value }}</div>
          <div class="stat-label">{{ card.label }}</div>
        </div>
      </div>

      <!-- Yearly goal widget -->
      <div class="dash-card">
        <h2>🎯 {{ currentYear }} Reading Goal</h2>

        <div v-if="currentYearGoal">
          <div class="goal-progress-top">
            <span class="goal-target">
              <strong>{{ stats.finishedThisYear ?? 0 }}</strong> / <strong>{{ currentYearGoal.targetBooks }}</strong> books
            </span>
            <span class="goal-pct">{{ goalProgress }}%</span>
          </div>
          <div class="goal-bar-bg">
            <div class="goal-bar-fill" :style="{ width: goalProgress + '%' }"></div>
          </div>
          <p v-if="goalProgress >= 100" class="goal-achieved">🎉 Goal achieved!</p>
        </div>
        <p v-else class="no-goal-msg">No goal set for {{ currentYear }}.</p>

        <!-- Set / update goal form -->
        <div class="goal-form">
          <h3>Set a Goal</h3>
          <div class="goal-inputs">
            <div class="goal-input-group">
              <label>Year</label><br/>
              <input type="number" v-model.number="newGoalYear" :min="2020" :max="2030" class="goal-input"/>
            </div>
            <div class="goal-input-group">
              <label>Books Target</label><br/>
              <input type="number" v-model.number="newGoalTarget" min="1" class="goal-input goal-input--wide"/>
            </div>
            <button @click="saveGoal" class="btn-save-goal">Save Goal</button>
          </div>
          <p v-if="goalMsg.text" class="goal-msg" :class="goalMsg.ok ? 'goal-msg--ok' : 'goal-msg--error'">
            {{ goalMsg.text }}
          </p>
        </div>
      </div>

      <!-- Past goals -->
      <div v-if="goals.length" class="dash-card">
        <h2>📅 All Goals</h2>
        <table class="goals-table">
          <thead class="goals-thead">
          <tr>
            <th class="goals-th">Year</th>
            <th class="goals-th">Target</th>
            <th class="goals-th"></th>
          </tr>
          </thead>
          <tbody class="goals-tbody">
          <tr v-for="g in goals" :key="g.id">
            <td class="goals-td goals-td--bold">{{ g.year }}</td>
            <td class="goals-td">{{ g.targetBooks }} books</td>
            <td class="goals-td">
              <button @click="deleteGoal(g)" class="btn-delete-goal">Delete</button>
            </td>
          </tr>
          </tbody>
        </table>
      </div>

      <!-- Recent reading activity -->
      <div class="dash-card">
        <div class="activity-header">
          <h2>📖 Recent Activity</h2>
          <RouterLink to="/journal" class="activity-link">View all →</RouterLink>
        </div>
        <p v-if="!recentJournal.length" class="activity-empty">No activity yet. Start reading!</p>
        <div v-for="entry in recentJournal" :key="entry.id" class="activity-entry">
          <div class="activity-date">{{ entry.entryDate }}</div>
          <RouterLink :to="`/books/${entry.readingLog?.book?.id}`" class="activity-book-link">
            {{ entry.readingLog?.book?.title }}
          </RouterLink>
          <div class="activity-type">
            <template v-if="entry.entryType === 'STATUS_CHANGE'">
              <span v-if="entry.status === 'READING'" class="activity-started">Started reading</span>
              <span v-else-if="entry.status === 'FINISHED'" class="activity-finished">Finished</span>
              <span v-else-if="entry.status === 'DNF'" class="activity-dnf">Did not finish</span>
            </template>
            <template v-else>
              <span class="activity-progress-pct">
                {{
                  entry.readingLog?.book?.pageCount
                      ? Math.round((entry.cumulativePages / entry.readingLog.book.pageCount) * 100) + '%'
                      : entry.cumulativePages + ' pages'
                }}
              </span>
              <span class="activity-progress-meta">
                {{ entry.cumulativePages }} / {{ entry.readingLog?.book?.pageCount ?? '?' }} pages
              </span>
            </template>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.dashboard {
  max-width: 900px;
  margin: 0 auto;
  padding: 1.5rem;
}

.dashboard h1 {
  margin-bottom: 1.5rem;
}

.dashboard-loading {
  color: gray;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-card {
  padding: 1rem;
  border-radius: 10px;
  text-align: center;
}

.stat-value {
  font-size: 2rem;
  font-weight: bold;
}

.stat-label {
  font-size: 0.85rem;
  color: #6b7280;
  margin-top: 4px;
}

.dash-card {
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 1.5rem;
  background: white;
  margin-bottom: 2rem;
}

.dash-card h2 {
  margin: 0 0 1rem;
}

.goal-progress-top {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.goal-target {
  font-size: 0.95rem;
}

.goal-pct {
  font-weight: bold;
  color: #2563eb;
}

.goal-bar-bg {
  height: 10px;
  background: #e5e7eb;
  border-radius: 5px;
  overflow: hidden;
}

.goal-bar-fill {
  height: 100%;
  background: #2563eb;
  transition: width 0.5s;
}

.goal-achieved {
  color: #16a34a;
  font-weight: 600;
  margin-top: 8px;
}

.no-goal-msg {
  color: #6b7280;
  margin-bottom: 0.5rem;
}

.goal-form {
  margin-top: 1.25rem;
  padding-top: 1.25rem;
  border-top: 1px solid #f3f4f6;
}

.goal-form h3 {
  margin: 0 0 0.75rem;
  font-size: 1rem;
}

.goal-inputs {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
  align-items: flex-end;
}

.goal-input-group label {
  font-size: 0.85rem;
  color: #6b7280;
}

.goal-input {
  width: 80px;
  padding: 6px 8px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
}

.goal-input--wide {
  width: 90px;
}

.btn-save-goal {
  padding: 8px 20px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.goal-msg {
  margin-top: 8px;
  font-size: 0.9rem;
}

.goal-msg--ok {
  color: #16a34a;
}

.goal-msg--error {
  color: #dc2626;
}

.goals-table {
  width: 100%;
  border-collapse: collapse;
}

.goals-thead tr {
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.goals-th {
  padding: 8px 12px;
  color: #6b7280;
  font-size: 0.9rem;
}

.goals-tbody tr {
  border-bottom: 1px solid #f3f4f6;
}

.goals-td {
  padding: 8px 12px;
}

.goals-td--bold {
  font-weight: 600;
}

.btn-delete-goal {
  padding: 3px 8px;
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fecaca;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.activity-header h2 {
  margin: 0;
}

.activity-link {
  font-size: 0.9rem;
  color: #2563eb;
}

.activity-empty {
  color: gray;
  font-style: italic;
}

.activity-entry {
  padding: 10px 0;
  border-bottom: 1px solid #f3f4f6;
}

.activity-date {
  font-size: 0.8rem;
  color: #9ca3af;
  margin-bottom: 3px;
}

.activity-book-link {
  font-size: 0.85rem;
  font-weight: 600;
  color: #1e3a5f;
  text-decoration: none;
}

.activity-type {
  font-size: 0.9rem;
  margin-top: 3px;
}

.activity-started {
  color: #2563eb;
  font-weight: 600;
}

.activity-finished {
  color: #16a34a;
  font-weight: 600;
}

.activity-dnf {
  color: #dc2626;
  font-weight: 600;
}

.activity-progress-pct {
  font-weight: 700;
}

.activity-progress-meta {
  color: #6b7280;
  font-size: 0.85rem;
  margin-left: 6px;
}
</style>


