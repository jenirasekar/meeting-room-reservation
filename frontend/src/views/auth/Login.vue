<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function handleLogin() {
  error.value = ''
  if (!username.value || !password.value) {
    error.value = 'Please fill in all fields'
    return
  }

  try {
    loading.value = true
    const result = await auth.login({
      username: username.value,
      password: password.value
    })
    if (result.success) {
      router.push('/rooms')
    } else {
      error.value = result.message || 'Login failed'
    }
  } catch (e) {
    error.value = e.response?.data?.message || 'Login failed. Check your credentials.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary-500 to-primary-700 px-4">
    <div class="w-full max-w-md">
      <div class="text-center mb-8">
        <span class="text-5xl">🏢</span>
        <h1 class="text-3xl font-bold text-white mt-4">MeetingRoom</h1>
        <p class="text-primary-100 mt-2">Sign in to book meeting rooms</p>
      </div>

      <div class="bg-white rounded-2xl shadow-xl p-8">
        <h2 class="text-xl font-semibold text-gray-900 mb-6">Sign In</h2>

        <form @submit.prevent="handleLogin" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Username</label>
            <input
              v-model="username"
              type="text"
              class="input-field"
              placeholder="Enter your username"
              autocomplete="username"
              required
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Password</label>
            <input
              v-model="password"
              type="password"
              class="input-field"
              placeholder="Enter your password"
              autocomplete="current-password"
              required
            />
          </div>

          <div v-if="error" class="text-sm text-red-600 bg-red-50 rounded-lg p-3">
            {{ error }}
          </div>

          <button
            type="submit"
            class="btn-primary w-full py-3"
            :disabled="loading"
          >
            {{ loading ? 'Signing in...' : 'Sign In' }}
          </button>
        </form>

        <p class="text-sm text-center text-gray-500 mt-6">
          Don't have an account?
          <router-link to="/register" class="text-primary-600 hover:text-primary-700 font-medium">
            Register
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>
