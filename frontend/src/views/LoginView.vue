<script setup lang="ts">
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import api from '@/api'


const router = useRouter()
const email = ref('')
const password = ref('')
const error = ref('')

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
    <div class="auth-card">
      <h1>Login</h1>
      <input v-model="email" type="email" placeholder="Email" class="auth-input"/><br/>
      <input v-model="password" type="password" placeholder="Password" class="auth-input"/><br/>
      <button @click="login" class="auth-btn">Login</button>
      <p class="auth-error">{{ error }}</p>
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

.auth-input {
  width: 100%;
  padding: 10px 12px;
  margin-bottom: 0.75rem;
  border: 1px solid #504945;
  border-radius: 8px;
  background: #32302f;
  color: #ebdbb2;
  font-size: 0.95rem;
  box-sizing: border-box;
}
.auth-input::placeholder { color: #7c6f64; }
.auth-input:focus { outline: none; border-color: #83a598; }

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

.auth-error { color: #fb4934; margin-top: 0.75rem; min-height: 1.2em; text-align: center; }
.auth-link { color: #a89984; margin-top: 1rem; text-align: center; }
</style>

