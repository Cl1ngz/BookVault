<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || 'null')
if (!user || user.role !== 'ADMIN') router.push('/books')

// ── State ─────────────────────────────────────────────────────────────────────
const readers = ref<any[]>([])
const loading = ref(false)
const message = ref({ text: '', ok: true })
const searchQuery = ref('')

// Ban modal
const banTarget = ref<any>(null)
const banDays = ref(7)

// ── Fetch ─────────────────────────────────────────────────────────────────────
async function loadReaders() {
  loading.value = true
  try {
    const res = await api.get('/admin/readers')
    readers.value = res.data
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  document.title = 'Admin Panel — BookVault'
  loadReaders()
})

// ── Computed ──────────────────────────────────────────────────────────────────
const filteredReaders = computed(() => {
  const q = searchQuery.value.toLowerCase()
  if (!q) return readers.value
  return readers.value.filter(r =>
    r.username?.toLowerCase().includes(q) || r.email?.toLowerCase().includes(q)
  )
})

function isBanned(reader: any): boolean {
  if (!reader.bannedUntil) return false
  return new Date(reader.bannedUntil) >= new Date(new Date().toDateString())
}

// ── Actions ───────────────────────────────────────────────────────────────────
async function setRole(reader: any, role: string) {
  try {
    const res = await api.put(`/admin/readers/${reader.id}/role`, { role })
    reader.role = res.data.role
    showMsg(`${reader.username}'s role updated to ${role}`, true)
  } catch (e: any) {
    showMsg(e.response?.data ?? 'Failed to update role', false)
  }
}

async function confirmBan() {
  if (!banTarget.value) return
  try {
    const res = await api.put(`/admin/readers/${banTarget.value.id}/ban`, { days: banDays.value })
    banTarget.value.bannedUntil = res.data.bannedUntil || null
    showMsg(res.data.message, true)
  } catch (e: any) {
    showMsg(e.response?.data ?? 'Failed to ban user', false)
  } finally {
    banTarget.value = null
  }
}

async function unban(reader: any) {
  try {
    const res = await api.put(`/admin/readers/${reader.id}/ban`, { days: 0 })
    reader.bannedUntil = null
    showMsg(res.data.message, true)
  } catch (e: any) {
    showMsg(e.response?.data ?? 'Failed to unban', false)
  }
}

async function deleteReader(reader: any) {
  if (!confirm(`Permanently delete account "${reader.username}"? This cannot be undone.`)) return
  try {
    await api.delete(`/admin/readers/${reader.id}`)
    readers.value = readers.value.filter(r => r.id !== reader.id)
    showMsg(`Account "${reader.username}" deleted`, true)
  } catch (e: any) {
    showMsg(e.response?.data ?? 'Failed to delete', false)
  }
}

function showMsg(text: string, ok: boolean) {
  message.value = { text, ok }
  setTimeout(() => { message.value.text = '' }, 4000)
}
</script>

<template>
  <main class="admin-panel" aria-labelledby="admin-title">
    <h1 id="admin-title">Admin Panel</h1>

    <!-- Message -->
    <p v-if="message.text" role="alert" :class="['msg', message.ok ? 'ok' : 'err']">
      {{ message.text }}
    </p>

    <!-- Search -->
    <div class="toolbar">
      <label for="search-input">Search users:</label>
      <input
        id="search-input"
        v-model="searchQuery"
        type="search"
        placeholder="Username or email…"
        aria-label="Search users by username or email"
      />
      <button @click="loadReaders" aria-label="Refresh user list">↻ Refresh</button>
    </div>

    <p v-if="loading" aria-live="polite">Loading…</p>

    <!-- Users table -->
    <div class="table-wrapper" v-if="!loading">
      <table aria-label="User list">
        <thead>
          <tr>
            <th scope="col">ID</th>
            <th scope="col">Username</th>
            <th scope="col">Email</th>
            <th scope="col">Role</th>
            <th scope="col">Banned until</th>
            <th scope="col">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="reader in filteredReaders"
            :key="reader.id"
            :class="{ 'row-banned': isBanned(reader) }"
          >
            <td>{{ reader.id }}</td>
            <td>{{ reader.username }}</td>
            <td>{{ reader.email }}</td>

            <!-- Role selector -->
            <td>
              <select
                v-if="reader.role !== 'ADMIN'"
                :value="reader.role"
                @change="setRole(reader, ($event.target as HTMLSelectElement).value)"
                :aria-label="`Change role for ${reader.username}`"
              >
                <option value="USER">USER</option>
                <option value="MODERATOR">MODERATOR</option>
              </select>
              <span v-else class="badge admin">ADMIN</span>
            </td>

            <!-- Ban status -->
            <td>
              <span v-if="isBanned(reader)" class="badge banned">{{ reader.bannedUntil }}</span>
              <span v-else class="badge ok">—</span>
            </td>

            <!-- Actions -->
            <td class="actions" v-if="reader.role !== 'ADMIN'">
              <button
                v-if="!isBanned(reader)"
                class="btn-ban"
                @click="banTarget = reader; banDays = 7"
                :aria-label="`Ban ${reader.username}`"
              >
                🚫 Ban
              </button>
              <button
                v-else
                class="btn-unban"
                @click="unban(reader)"
                :aria-label="`Unban ${reader.username}`"
              >
                ✅ Unban
              </button>
              <button
                class="btn-delete"
                @click="deleteReader(reader)"
                :aria-label="`Delete account ${reader.username}`"
              >
                🗑 Delete
              </button>
            </td>
            <td v-else>—</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Ban duration modal -->
    <div v-if="banTarget" class="modal-overlay" role="dialog" aria-modal="true" :aria-label="`Ban ${banTarget.username}`">
      <div class="modal">
        <h2>Ban "{{ banTarget.username }}"</h2>
        <label for="ban-days">Number of days:</label>
        <input
          id="ban-days"
          v-model.number="banDays"
          type="number"
          min="1"
          max="365"
          aria-required="true"
        />
        <div class="modal-actions">
          <button class="btn-ban" @click="confirmBan">Confirm Ban</button>
          <button @click="banTarget = null">Cancel</button>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.admin-panel {
  max-width: 1100px;
  margin: 2rem auto;
  padding: 0 1rem;
}

h1 { margin-bottom: 1.5rem; }

.toolbar {
  display: flex;
  gap: .75rem;
  align-items: center;
  margin-bottom: 1.25rem;
  flex-wrap: wrap;
}

.toolbar input {
  flex: 1;
  min-width: 200px;
  padding: .45rem .7rem;
  border: 1px solid #ccc;
  border-radius: 6px;
}

.table-wrapper { overflow-x: auto; }

table {
  width: 100%;
  border-collapse: collapse;
  font-size: .95rem;
}

th, td {
  padding: .6rem .85rem;
  border: 1px solid #e0e0e0;
  text-align: left;
  vertical-align: middle;
}

th { background: #f5f5f5; font-weight: 600; }

.row-banned { background: #fff3f3; }

.badge {
  padding: .2rem .55rem;
  border-radius: 4px;
  font-size: .82rem;
  font-weight: 600;
}
.badge.admin  { background: #e0e7ff; color: #3730a3; }
.badge.banned { background: #fee2e2; color: #b91c1c; }
.badge.ok     { color: #9ca3af; }

.actions { display: flex; gap: .5rem; flex-wrap: wrap; }

button {
  padding: .35rem .7rem;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: .85rem;
}

.btn-ban    { background: #f97316; color: #fff; }
.btn-unban  { background: #22c55e; color: #fff; }
.btn-delete { background: #ef4444; color: #fff; }

select {
  padding: .3rem;
  border-radius: 4px;
  border: 1px solid #d1d5db;
}

/* Modal */
.modal-overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,.45);
  display: flex; align-items: center; justify-content: center;
  z-index: 100;
}
.modal {
  background: #fff;
  padding: 2rem;
  border-radius: 10px;
  width: 320px;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.modal h2 { margin: 0; }
.modal input {
  padding: .45rem .7rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  width: 100%;
}
.modal-actions { display: flex; gap: .75rem; justify-content: flex-end; }

.msg { padding: .65rem 1rem; border-radius: 6px; margin-bottom: 1rem; }
.msg.ok  { background: #dcfce7; color: #166534; }
.msg.err { background: #fee2e2; color: #b91c1c; }
</style>

