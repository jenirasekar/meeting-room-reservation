<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useToastStore } from '../../stores/toast'
import AppIcon from '../../components/AppIcon.vue'

const router = useRouter()
const auth = useAuthStore()
const toast = useToastStore()

const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)
const shakeForm = ref(false)

async function handleLogin() {
  error.value = ''
  shakeForm.value = false
  if (!username.value || !password.value) {
    error.value = 'Please fill in all fields'
    shakeForm.value = true
    return
  }

  try {
    loading.value = true
    const result = await auth.login({
      username: username.value,
      password: password.value
    })
    if (result.success) {
      toast.success('Welcome back!')
      router.push('/rooms')
    } else {
      error.value = result.message || 'Login failed'
      shakeForm.value = true
    }
  } catch (e) {
    error.value = e.response?.data?.message || 'Invalid credentials. Please try again.'
    shakeForm.value = true
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-surface-900 px-4 relative overflow-hidden">
    <!-- Animated background blobs -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="bg-blob w-96 h-96 bg-primary-400 top-[-10%] left-[-5%]" style="animation-delay: 0s" />
      <div class="bg-blob-delayed w-80 h-80 bg-primary-700 bottom-[-10%] right-[-5%]" />
      <div class="bg-blob w-64 h-64 bg-primary-500 top-[40%] right-[10%]" style="animation-delay: 1.5s" />
    </div>

    <div class="w-full max-w-md relative z-10">
      <!-- Logo -->
      <div class="text-center mb-8">
        <div class="w-16 h-16 rounded-2xl bg-white/10 backdrop-blur-xl border border-white/10 flex items-center justify-center mx-auto mb-4 shadow-glow">
          <AppIcon icon="building" :size="32" class="text-white" />
        </div>
        <h1 class="text-3xl font-extrabold text-white tracking-tight">
          Meeting<span class="text-primary-300">Room</span>
        </h1>
        <p class="text-surface-400 mt-2 text-sm">Sign in to manage your bookings</p>
      </div>

      <!-- Card -->
      <div
        class="glass-card p-8"
        :class="{ 'animate-shake': shakeForm }"
        @animationend="shakeForm = false"
      >
        <h2 class="text-xl font-semibold text-surface-900 mb-6">Welcome Back</h2>

        <form @submit.prevent="handleLogin" class="space-y-4">
          <!-- Username -->
          <div>
            <label class="block text-sm font-medium text-surface-700 mb-1.5">Username</label>
            <div class="relative">
              <AppIcon icon="user" :size="16" class="input-icon" />
              <input
                v-model="username"
                type="text"
                class="input-field input-with-icon"
                :class="{ error: error }"
                placeholder="Enter your username"
                autocomplete="username"
                required
              />
            </div>
          </div>

          <!-- Password -->
          <div>
            <label class="block text-sm font-medium text-surface-700 mb-1.5">Password</label>
            <div class="relative">
              <AppIcon icon="lock" :size="16" class="input-icon" />
              <input
                v-model="password"
                type="password"
                class="input-field input-with-icon"
                :class="{ error: error }"
                placeholder="Enter your password"
                autocomplete="current-password"
                required
              />
            </div>
          </div>

          <!-- Error -->
          <div v-if="error" class="flex items-start gap-2 text-sm text-red-600 bg-red-50 rounded-xl p-3 border border-red-100 animate-slide-down">
            <AppIcon icon="exclamation-circle" :size="16" class="shrink-0 mt-0.5" />
            {{ error }}
          </div>

          <!-- Submit -->
          <button
            type="submit"
            class="btn-primary w-full btn-lg"
            :disabled="loading"
          >
            <span v-if="loading" class="inline-block w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
            {{ loading ? 'Signing in...' : 'Sign In' }}
          </button>
        </form>

        <p class="text-sm text-center text-surface-500 mt-6">
          Don't have an account?
          <router-link to="/register" class="text-primary-600 hover:text-primary-700 font-semibold transition-colors">
            Create one
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>
