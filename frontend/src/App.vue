<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// Use a ref so the nav re-renders when user logs in/out
const userRaw = ref(localStorage.getItem('user'))
const user = computed(() => JSON.parse(userRaw.value || 'null'))
const isModerator = computed(() => user.value?.role === 'MODERATOR')
const isLoggedIn = computed(() => !!user.value?.token)

function logout() {
  localStorage.removeItem('user')
  userRaw.value = null
  router.push('/login')
}

// Keep nav in sync if another tab logs in/out
window.addEventListener('storage', () => {
  userRaw.value = localStorage.getItem('user')
})
</script>

<template>
  <nav style="display:flex; align-items:center; gap:0.75rem; padding:0.6rem 1rem; background:#1e3a5f; color:white;">
    <RouterLink to="/books" style="color:white; text-decoration:none;">Books</RouterLink>
    <span style="color:#aaa;">|</span>
    <RouterLink to="/authors" style="color:white; text-decoration:none;">Authors</RouterLink>
    <span style="color:#aaa;">|</span>
    <RouterLink to="/readers" style="color:white; text-decoration:none;">Readers</RouterLink>
    <span style="color:#aaa;">|</span>
    <RouterLink to="/series" style="color:white; text-decoration:none;">Series</RouterLink>

    <template v-if="isModerator">
      <span style="color:#aaa;">|</span>
      <RouterLink to="/moderator" style="color:#fbbf24; text-decoration:none;">🛡️ Moderator</RouterLink>
    </template>

    <!-- right side -->
    <span style="flex:1;"></span>

    <template v-if="isLoggedIn">
      <span style="color:#aaa; font-size:0.9rem;">👤 {{ user.username }}</span>
      <button
        @click="logout"
        style="padding:4px 14px; background:#dc2626; color:white; border:none; border-radius:6px; cursor:pointer; font-size:0.9rem;">
        Logout
      </button>
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
