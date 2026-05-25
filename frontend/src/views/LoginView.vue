<script setup lang="ts">
import {ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import api from '@/api'


const router = useRouter()
const email = ref('')
const password = ref('')
const error = ref('')

onMounted(() => {
  document.title = 'Login — BookVault'
})

async function login() {
  try {
    const res = await api.post('/auth/login', {email: email.value, password: password.value})
    localStorage.setItem('user', JSON.stringify(res.data))
    window.dispatchEvent(new Event('storage'))
    router.push('/books')
  } catch {
    error.value = 'Invalid email or password'
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card" role="region" aria-label="Login form">
      <h1>Login</h1>
      <form @submit.prevent="login" novalidate>
        <div class="form-group">
          <label for="login-email" class="form-label">Email address</label>
          <input
            id="login-email"
            v-model="email"
            type="email"
            autocomplete="email"
            class="auth-input"
            required
            :aria-describedby="error ? 'login-error' : undefined"
          />
        </div>
        <div class="form-group">
          <label for="login-password" class="form-label">Password</label>
          <input
            id="login-password"
            v-model="password"
            type="password"
            autocomplete="current-password"
            class="auth-input"
            required
            :aria-describedby="error ? 'login-error' : undefined"
          />
        </div>
        <button type="submit" class="auth-btn">Login</button>
        <p
          v-if="error"
          id="login-error"
          role="alert"
          aria-live="assertive"
          class="auth-error"
        >{{ error }}</p>
        <p v-else class="auth-error" aria-hidden="true"></p>
      </form>
      <p class="auth-link">No account?
        <RouterLink to="/register">Register</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  padding: 3rem 1rem;
}

.auth-card {
  background: #3c3836;
  border: 1px solid #504945;
  border-radius: 12px;
  padding: 2rem;
  width: 100%;
  max-width: 380px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.4);
}

.auth-card h1 {
  margin: 0 0 1.5rem;
  color: #fabd2f;
  text-align: center;
}

.form-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 0.75rem;
}

.form-label {
  font-size: 0.88rem;
  color: #a89984;
  margin-bottom: 4px;
  font-weight: 500;
}

.auth-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #504945;
  border-radius: 8px;
  background: #32302f;
  color: #ebdbb2;
  font-size: 0.95rem;
  box-sizing: border-box;
}
.auth-input::placeholder { color: #7c6f64; }
.auth-input:focus-visible { outline: 3px solid #83a598; outline-offset: 1px; border-color: #83a598; }

.auth-btn {
  width: 100%;
  padding: 10px;
  background: #458588;
  color: #ebdbb2;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  margin-top: 0.5rem;
  transition: filter 0.12s;
}
.auth-btn:hover { filter: brightness(1.15); }
.auth-btn:focus-visible { outline: 3px solid #fabd2f; outline-offset: 2px; }

.auth-error { color: #fb4934; margin-top: 0.75rem; min-height: 1.2em; text-align: center; }
.auth-link { color: #a89984; margin-top: 1rem; text-align: center; }
</style>

