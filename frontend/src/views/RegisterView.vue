<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api'

const router = useRouter()
const username = ref('')
const email = ref('')
const password = ref('')
const error = ref('')

async function register() {
  try {
    await api.post('/auth/register', {
      username: username.value,
      email: email.value,
      password: password.value
    })
    router.push('/login')
  } catch {
    error.value = 'Registration failed'
  }
}
</script>

<template>
  <div>
    <h1>Register</h1>
    <input v-model="username" placeholder="Username" /><br/>
    <input v-model="email" type="email" placeholder="Email" /><br/>
    <input v-model="password" type="password" placeholder="Password" /><br/>
    <button @click="register">Register</button>
    <p style="color:red">{{ error }}</p>
    <p>Already have an account? <RouterLink to="/login">Login</RouterLink></p>
  </div>
</template>
