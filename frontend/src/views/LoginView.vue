<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api'

const router = useRouter()
const email = ref('')
const password = ref('')
const error = ref('')

async function login() {
  try {
    const res = await api.post('/auth/login', { email: email.value, password: password.value })
    localStorage.setItem('user', JSON.stringify(res.data))
    window.dispatchEvent(new Event('storage'))
    router.push('/books')
  } catch {
    error.value = 'Invalid email or password'
  }
}
</script>

<template>
  <div>
    <h1>Login</h1>
    <input v-model="email" type="email" placeholder="Email" /><br/>
    <input v-model="password" type="password" placeholder="Password" /><br/>
    <button @click="login">Login</button>
    <p style="color:red">{{ error }}</p>
    <p>No account? <RouterLink to="/register">Register</RouterLink></p>
  </div>
</template>
