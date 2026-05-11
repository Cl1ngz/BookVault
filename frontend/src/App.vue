<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const userRaw = ref(localStorage.getItem('user'))
const user = computed(() => JSON.parse(userRaw.value || 'null'))
const isModerator = computed(() => user.value?.role === 'MODERATOR')
const isLoggedIn = computed(() => !!user.value?.token)

const dropdownOpen = ref(false)

function toggleDropdown() {
  dropdownOpen.value = !dropdownOpen.value
}

function closeDropdown() {
  dropdownOpen.value = false
}

// Close dropdown when clicking outside
function handleGlobalClick(e: MouseEvent) {
  const target = e.target as HTMLElement
  if (!target.closest('#user-menu')) {
    dropdownOpen.value = false
  }
}
document.addEventListener('click', handleGlobalClick)

function logout() {
  localStorage.removeItem('user')
  userRaw.value = null
  dropdownOpen.value = false
  router.push('/login')
}

window.addEventListener('storage', () => {
  userRaw.value = localStorage.getItem('user')
})
</script>

<template>
  <nav style="display:flex; align-items:center; gap:0.75rem; padding:0.6rem 1rem; background:#1e3a5f; color:white;">
    <RouterLink to="/" style="color:white; text-decoration:none; font-weight:700; font-size:1.05rem;">📚 BookVault</RouterLink>
    <span style="color:#aaa;">|</span>
    <RouterLink to="/books" style="color:white; text-decoration:none;">Books</RouterLink>
    <span style="color:#aaa;">|</span>
    <RouterLink to="/authors" style="color:white; text-decoration:none;">Authors</RouterLink>
    <span style="color:#aaa;">|</span>
    <RouterLink to="/series" style="color:white; text-decoration:none;">Series</RouterLink>

    <template v-if="isModerator">
      <span style="color:#aaa;">|</span>
      <RouterLink to="/moderator" style="color:#fbbf24; text-decoration:none;">🛡️ Moderator</RouterLink>
    </template>

    <span style="flex:1;"></span>

    <template v-if="isLoggedIn">
      <!-- Username button that opens dropdown -->
      <div id="user-menu" style="position:relative;">
        <button
          @click.stop="toggleDropdown"
          style="padding:4px 12px; background:transparent; color:white; border:1px solid rgba(255,255,255,0.3); border-radius:6px; cursor:pointer; font-size:0.9rem; display:flex; align-items:center; gap:6px;">
          👤 {{ user.username }}
          <span style="font-size:0.7rem; opacity:0.7;">{{ dropdownOpen ? '▲' : '▼' }}</span>
        </button>

        <!-- Dropdown menu -->
        <div
          v-if="dropdownOpen"
          style="position:absolute; right:0; top:calc(100% + 6px); background:white; border:1px solid #e5e7eb; border-radius:8px; box-shadow:0 4px 12px rgba(0,0,0,0.15); min-width:170px; z-index:100; overflow:hidden;">
          <RouterLink to="/my-shelf" @click="closeDropdown"
            style="display:flex; align-items:center; gap:8px; padding:10px 16px; color:#1e3a5f; text-decoration:none; font-size:0.9rem; border-bottom:1px solid #f3f4f6;">
            📚 My Shelf
          </RouterLink>
          <RouterLink to="/dashboard" @click="closeDropdown"
            style="display:flex; align-items:center; gap:8px; padding:10px 16px; color:#1e3a5f; text-decoration:none; font-size:0.9rem; border-bottom:1px solid #f3f4f6;">
            📊 Dashboard
          </RouterLink>
          <button @click="logout"
            style="width:100%; padding:10px 16px; background:none; border:none; color:#dc2626; font-size:0.9rem; cursor:pointer; text-align:left; display:flex; align-items:center; gap:8px;">
            🚪 Logout
          </button>
        </div>
      </div>
    </template>

    <template v-else>
      <RouterLink to="/login"
        style="padding:4px 14px; background:#2563eb; color:white; border-radius:6px; text-decoration:none; font-size:0.9rem;">
        Login
      </RouterLink>
      <RouterLink to="/register"
        style="padding:4px 14px; background:#16a34a; color:white; border-radius:6px; text-decoration:none; font-size:0.9rem;">
        Register
      </RouterLink>
    </template>
  </nav>
  <RouterView />
</template>
