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
          { label: '📖 Reading',      value: stats.reading ?? 0,          color: '#32302f', border: '#458588', text: '#83a598' },
          { label: '🔖 Want to Read', value: stats.toRead ?? 0,           color: '#32302f', border: '#d79921', text: '#fabd2f' },
          { label: '✅ Finished',     value: stats.finished ?? 0,         color: '#32302f', border: '#98971a', text: '#b8bb26' },
          { label: '❌ DNF',          value: stats.dnf ?? 0,              color: '#32302f', border: '#cc241d', text: '#fb4934' },
          { label: '🗓️ This Year',   value: stats.finishedThisYear ?? 0, color: '#32302f', border: '#b16286', text: '#d3869b' },
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

.dashboard h1 { margin-bottom: 1.5rem; color: #fabd2f; }
.dashboard-loading { color: #a89984; }

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

.stat-value { font-size: 2rem; font-weight: bold; }
.stat-label { font-size: 0.85rem; color: #a89984; margin-top: 4px; }

.dash-card {
  border: 1px solid #504945;
  border-radius: 10px;
  padding: 1.5rem;
  background: #3c3836;
  margin-bottom: 2rem;
}

.dash-card h2 { margin: 0 0 1rem; color: #d5c4a1; }

.goal-progress-top { display: flex; justify-content: space-between; margin-bottom: 8px; }
.goal-target { font-size: 0.95rem; color: #d5c4a1; }
.goal-pct { font-weight: bold; color: #83a598; }

.goal-bar-bg {
  height: 10px;
  background: #504945;
  border-radius: 5px;
  overflow: hidden;
}

.goal-bar-fill {
  height: 100%;
  background: #458588;
  transition: width 0.5s;
}

.goal-achieved { color: #b8bb26; font-weight: 600; margin-top: 8px; }
.no-goal-msg { color: #a89984; margin-bottom: 0.5rem; }

.goal-form {
  margin-top: 1.25rem;
  padding-top: 1.25rem;
  border-top: 1px solid #504945;
}

.goal-form h3 { margin: 0 0 0.75rem; font-size: 1rem; color: #d5c4a1; }

.goal-inputs { display: flex; gap: 1rem; flex-wrap: wrap; align-items: flex-end; }

.goal-input-group label { font-size: 0.85rem; color: #a89984; }

.goal-input {
  width: 80px;
  padding: 6px 8px;
  border: 1px solid #504945;
  border-radius: 6px;
  background: #32302f;
  color: #ebdbb2;
}
.goal-input:focus { outline: none; border-color: #83a598; }

.goal-input--wide { width: 90px; }

.btn-save-goal {
  padding: 8px 20px;
  background: #458588;
  color: #ebdbb2;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: filter 0.12s;
}
.btn-save-goal:hover { filter: brightness(1.15); }

.goal-msg { margin-top: 8px; font-size: 0.9rem; }
.goal-msg--ok { color: #b8bb26; }
.goal-msg--error { color: #fb4934; }

.goals-table { width: 100%; border-collapse: collapse; }

.goals-thead tr {
  text-align: left;
  border-bottom: 1px solid #504945;
}

.goals-th { padding: 8px 12px; color: #a89984; font-size: 0.9rem; }

.goals-tbody tr { border-bottom: 1px solid #3c3836; }

.goals-td { padding: 8px 12px; color: #d5c4a1; }
.goals-td--bold { font-weight: 600; }

.btn-delete-goal {
  padding: 3px 8px;
  background: rgba(204, 36, 29, 0.15);
  color: #fb4934;
  border: 1px solid #cc241d;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
}

.activity-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; }
.activity-header h2 { margin: 0; }
.activity-link { font-size: 0.9rem; color: #83a598; }

.activity-empty { color: #a89984; font-style: italic; }

.activity-entry {
  padding: 10px 0;
  border-bottom: 1px solid #504945;
}

.activity-date { font-size: 0.8rem; color: #7c6f64; margin-bottom: 3px; }

.activity-book-link {
  font-size: 0.85rem;
  font-weight: 600;
  color: #d5c4a1;
  text-decoration: none;
}
.activity-book-link:hover { color: #fabd2f; }

.activity-type { font-size: 0.9rem; margin-top: 3px; }
.activity-started { color: #83a598; font-weight: 600; }
.activity-finished { color: #b8bb26; font-weight: 600; }
.activity-dnf { color: #fb4934; font-weight: 600; }
.activity-progress-pct { font-weight: 700; color: #d5c4a1; }
.activity-progress-meta { color: #a89984; font-size: 0.85rem; margin-left: 6px; }
</style>


