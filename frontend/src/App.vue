<script setup lang="ts">
import {computed, ref} from 'vue'
import {useRouter} from 'vue-router'

const router = useRouter()

const userRaw = ref(localStorage.getItem('user'))
const user = computed(() => JSON.parse(userRaw.value || 'null'))
const isModerator = computed(() => user.value?.role === 'MODERATOR' || user.value?.role === 'ADMIN')
const isAdmin = computed(() => user.value?.role === 'ADMIN')
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

// Close dropdown on Escape key (WCAG 2.1.1)
function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape' && dropdownOpen.value) {
    dropdownOpen.value = false
  }
}

document.addEventListener('click', handleGlobalClick)
document.addEventListener('keydown', handleKeydown)

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
  <nav class="app-nav" aria-label="Main navigation">
    <RouterLink to="/" class="nav-brand" aria-label="BookVault home">
      <span aria-hidden="true">📚</span> BookVault
    </RouterLink>
    <span class="nav-sep" aria-hidden="true">|</span>
    <RouterLink to="/books" class="nav-link">Books</RouterLink>
    <span class="nav-sep" aria-hidden="true">|</span>
    <RouterLink to="/authors" class="nav-link">Authors</RouterLink>
    <span class="nav-sep" aria-hidden="true">|</span>
    <RouterLink to="/series" class="nav-link">Series</RouterLink>

    <template v-if="isAdmin">
      <span class="nav-sep" aria-hidden="true">|</span>
      <RouterLink to="/moderator" class="nav-link nav-link--admin">
        <span aria-hidden="true">🛡</span> Admin Panel
      </RouterLink>
    </template>
    <template v-else-if="isModerator">
      <span class="nav-sep" aria-hidden="true">|</span>
      <RouterLink to="/moderator" class="nav-link nav-link--mod">
        <span aria-hidden="true">️</span> Moderator
      </RouterLink>
    </template>

    <span class="nav-spacer" aria-hidden="true"></span>

    <template v-if="isLoggedIn">
      <div id="user-menu" class="user-menu">
        <button
          @click.stop="toggleDropdown"
          class="user-menu-btn"
          :aria-expanded="dropdownOpen"
          aria-haspopup="menu"
          :aria-label="`User menu for ${user.username}. ${dropdownOpen ? 'Menu open' : 'Menu closed'}`"
        >
          {{ user.username }}
          <span class="user-menu-caret" aria-hidden="true">{{ dropdownOpen ? '▲' : '▼' }}</span>
        </button>

        <div
          v-if="dropdownOpen"
          class="dropdown"
          role="menu"
          :aria-label="`${user.username} account menu`"
        >
          <RouterLink to="/my-shelf" @click="closeDropdown" class="dropdown-item" role="menuitem">
            <span aria-hidden="true">📚</span> My Shelf
          </RouterLink>
          <RouterLink to="/dashboard" @click="closeDropdown" class="dropdown-item" role="menuitem">
            <span aria-hidden="true">📊</span> Dashboard
          </RouterLink>
          <button @click="logout" class="dropdown-item dropdown-item--logout" role="menuitem">
            <span aria-hidden="true">🚪</span> Logout
          </button>
        </div>
      </div>
    </template>

    <template v-else>
      <RouterLink to="/login" class="nav-btn nav-btn--login">Login</RouterLink>
      <RouterLink to="/register" class="nav-btn nav-btn--register">Register</RouterLink>
    </template>
  </nav>

  <main id="main-content" tabindex="-1">
    <RouterView/>
  </main>
</template>

<style scoped>
/* Gruvbox palette
   bg-hard: #1d2021  bg: #282828  bg-soft: #32302f
   bg1: #3c3836  bg2: #504945  bg3: #665c54  bg4: #7c6f64
   fg:  #ebdbb2  fg1: #d5c4a1  fg4: #a89984
   yellow: #d79921  bright-yellow: #fabd2f
   aqua:   #689d6a  bright-aqua:   #8ec07c
   blue:   #458588  bright-blue:   #83a598
   green:  #98971a  bright-green:  #b8bb26
   red:    #cc241d  bright-red:    #fb4934
   orange: #d65d0e  bright-orange: #fe8019
*/

.app-nav {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.6rem 1.2rem;
  background: #3c3836;
  border-bottom: 2px solid #504945;
  color: #ebdbb2;
}

.nav-brand {
  color: #fabd2f;
  text-decoration: none;
  font-weight: 700;
  font-size: 1.05rem;
  letter-spacing: 0.02em;
  text-shadow: 0 1px 4px rgba(0,0,0,0.4);
}

.nav-sep {
  color: #665c54;
}

.nav-link {
  color: #d5c4a1;
  text-decoration: none;
  transition: color 0.15s;
}

.nav-link:hover {
  color: #fabd2f;
}

.nav-link--mod {
  color: #fe8019;
  font-weight: 600;
}

.nav-link--mod:hover {
  color: #fabd2f;
}

.nav-link--admin {
  color: #a855f7;
  font-weight: 700;
}

.nav-link--admin:hover {
  color: #7c3aed;
}

.nav-spacer {
  flex: 1;
}

.user-menu {
  position: relative;
}

.user-menu-btn {
  padding: 4px 12px;
  background: transparent;
  color: #ebdbb2;
  border: 1px solid #665c54;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: border-color 0.15s, background 0.15s;
}

.user-menu-btn:hover {
  background: #504945;
  border-color: #fabd2f;
  color: #fabd2f;
}

.user-menu-caret {
  font-size: 0.7rem;
  opacity: 0.7;
}

.dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 6px);
  background: #32302f;
  border: 1px solid #504945;
  border-radius: 8px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.45);
  min-width: 170px;
  z-index: 100;
  overflow: hidden;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  color: #d5c4a1;
  text-decoration: none;
  font-size: 0.9rem;
  border-bottom: 1px solid #3c3836;
  transition: background 0.12s, color 0.12s;
}

.dropdown-item:last-child {
  border-bottom: none;
}

.dropdown-item:hover {
  background: #504945;
  color: #fabd2f;
}

.dropdown-item--logout {
  width: 100%;
  background: none;
  border: none;
  border-bottom: none;
  color: #fb4934;
  cursor: pointer;
  text-align: left;
}

.dropdown-item--logout:hover {
  background: #504945;
  color: #fb4934;
}

.nav-btn {
  padding: 4px 14px;
  color: #1d2021;
  border-radius: 6px;
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: 600;
  transition: filter 0.15s;
}

.nav-btn:hover {
  filter: brightness(1.15);
}

.nav-btn--login {
  background: #458588;
  color: #ebdbb2;
}

.nav-btn--register {
  background: #98971a;
  color: #ebdbb2;
}

.nav-link.router-link-active {
  color: #fabd2f;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.nav-btn:focus-visible {
  outline: 3px solid #fabd2f;
  outline-offset: 2px;
}

main {
  min-height: calc(100vh - 50px);
}

/* Ensure focus outline works on main for skip link */
main:focus {
  outline: none;
}
</style>
