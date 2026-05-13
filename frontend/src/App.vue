<script setup lang="ts">
import {computed, ref} from 'vue'
import {useRouter} from 'vue-router'

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
  <nav class="app-nav">
    <RouterLink to="/" class="nav-brand">📚 BookVault</RouterLink>
    <span class="nav-sep">|</span>
    <RouterLink to="/books" class="nav-link">Books</RouterLink>
    <span class="nav-sep">|</span>
    <RouterLink to="/authors" class="nav-link">Authors</RouterLink>
    <span class="nav-sep">|</span>
    <RouterLink to="/series" class="nav-link">Series</RouterLink>

    <template v-if="isModerator">
      <span class="nav-sep">|</span>
      <RouterLink to="/moderator" class="nav-link nav-link--mod">️ Moderator</RouterLink>
    </template>

    <span class="nav-spacer"></span>

    <template v-if="isLoggedIn">
      <div id="user-menu" class="user-menu">
        <button @click.stop="toggleDropdown" class="user-menu-btn">
          {{ user.username }}
          <span class="user-menu-caret">{{ dropdownOpen ? '▲' : '▼' }}</span>
        </button>

        <div v-if="dropdownOpen" class="dropdown">
          <RouterLink to="/my-shelf" @click="closeDropdown" class="dropdown-item">
            📚 My Shelf
          </RouterLink>
          <RouterLink to="/dashboard" @click="closeDropdown" class="dropdown-item">
            📊 Dashboard
          </RouterLink>
          <button @click="logout" class="dropdown-item dropdown-item--logout">
            🚪 Logout
          </button>
        </div>
      </div>
    </template>

    <template v-else>
      <RouterLink to="/login" class="nav-btn nav-btn--login">Login</RouterLink>
      <RouterLink to="/register" class="nav-btn nav-btn--register">Register</RouterLink>
    </template>
  </nav>
  <RouterView/>
</template>

<style scoped>
.app-nav {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.6rem 1rem;
  background: #1e3a5f;
  color: white;
}

.nav-brand {
  color: white;
  text-decoration: none;
  font-weight: 700;
  font-size: 1.05rem;
}

.nav-sep {
  color: #aaa;
}

.nav-link {
  color: white;
  text-decoration: none;
}

.nav-link--mod {
  color: #fbbf24;
}

.nav-spacer {
  flex: 1;
}

/* ── User menu ───────────────────────────────────────────────── */
.user-menu {
  position: relative;
}

.user-menu-btn {
  padding: 4px 12px;
  background: transparent;
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 6px;
}

.user-menu-caret {
  font-size: 0.7rem;
  opacity: 0.7;
}

.dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 6px);
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 170px;
  z-index: 100;
  overflow: hidden;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  color: #1e3a5f;
  text-decoration: none;
  font-size: 0.9rem;
  border-bottom: 1px solid #f3f4f6;
}

.dropdown-item--logout {
  width: 100%;
  background: none;
  border: none;
  border-bottom: none;
  color: #dc2626;
  cursor: pointer;
  text-align: left;
}

/* ── Auth buttons ────────────────────────────────────────────── */
.nav-btn {
  padding: 4px 14px;
  color: white;
  border-radius: 6px;
  text-decoration: none;
  font-size: 0.9rem;
}

.nav-btn--login {
  background: #2563eb;
}

.nav-btn--register {
  background: #16a34a;
}
</style>
