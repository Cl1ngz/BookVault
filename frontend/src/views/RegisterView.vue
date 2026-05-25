<script setup lang="ts">
import {ref, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import api from '@/api'


const router = useRouter()
const username = ref('')
const email = ref('')
const password = ref('')
const error = ref('')

onMounted(() => {
  document.title = 'Register — BookVault'
})

async function register() {
  try {
    const res = await api.post('/auth/register', {
      username: username.value,
      email: email.value,
      password: password.value
    })
    localStorage.setItem('user', JSON.stringify(res.data))
    window.dispatchEvent(new Event('storage'))
    router.push('/books')
  } catch {
    error.value = 'Registration failed'
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card" role="region" aria-label="Register form">
      <h1>Register</h1>
      <form @submit.prevent="register" novalidate>
        <div class="form-group">
          <label for="reg-username" class="form-label">Username</label>
          <input
            id="reg-username"
            v-model="username"
            type="text"
            autocomplete="username"
            class="auth-input"
            required
            :aria-describedby="error ? 'reg-error' : undefined"
          />
        </div>
        <div class="form-group">
          <label for="reg-email" class="form-label">Email address</label>
          <input
            id="reg-email"
            v-model="email"
            type="email"
            autocomplete="email"
            class="auth-input"
            required
            :aria-describedby="error ? 'reg-error' : undefined"
          />
        </div>
        <div class="form-group">
          <label for="reg-password" class="form-label">Password</label>
          <input
            id="reg-password"
            v-model="password"
            type="password"
            autocomplete="new-password"
            class="auth-input"
            required
            :aria-describedby="error ? 'reg-error' : undefined"
          />
        </div>
        <button type="submit" class="auth-btn">Register</button>
        <p
          v-if="error"
          id="reg-error"
          role="alert"
          aria-live="assertive"
          class="auth-error"
        >{{ error }}</p>
        <p v-else class="auth-error" aria-hidden="true"></p>
      </form>
      <p class="auth-link">Already have an account?
        <RouterLink to="/login">Login</RouterLink>
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
  background: #98971a;
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
  background: #98971a;
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

