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
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const error = ref('')
const loading = ref(false)
const shakeForm = ref(false)

async function handleRegister() {
  error.value = ''
  shakeForm.value = false
  if (!username.value || !email.value || !password.value) {
    error.value = 'Please fill in all fields'
    shakeForm.value = true
    return
  }
  if (password.value !== confirmPassword.value) {
    error.value = 'Passwords do not match'
    shakeForm.value = true
    return
  }
  if (password.value.length < 6) {
    error.value = 'Password must be at least 6 characters'
    shakeForm.value = true
    return
  }

  try {
    loading.value = true
    const result = await auth.register({
      username: username.value,
      email: email.value,
      password: password.value
    })
    if (result.success) {
      toast.success('Account created! Please sign in.')
      router.push('/login')
    } else {
      error.value = result.message || 'Registration failed'
      shakeForm.value = true
    }
  } catch (e) {
    error.value = e.response?.data?.message || 'Registration failed'
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
      <div class="bg-blob w-96 h-96 bg-primary-600 top-[-10%] right-[-5%]" />
      <div class="bg-blob-delayed w-80 h-80 bg-primary-400 bottom-[-10%] left-[-5%]" />
      <div class="bg-blob w-64 h-64 bg-primary-500 top-[40%] left-[10%]" style="animation-delay: 2s" />
    </div>

    <div class="w-full max-w-md relative z-10">
      <!-- Logo -->
      <div class="text-center mb-8">
        <div class="w-16 h-16 rounded-2xl bg-white/10 backdrop-blur-xl border border-white/10 flex items-center justify-center mx-auto mb-4 shadow-glow">
          <AppIcon icon="sparkles" :size="32" class="text-white" />
        </div>
        <h1 class="text-3xl font-extrabold text-white tracking-tight">
          Meeting<span class="text-primary-300">Room</span>
        </h1>
        <p class="text-surface-400 mt-2 text-sm">Create your account to get started</p>
      </div>

      <!-- Card -->
      <div
        class="glass-card p-8"
        :class="{ 'animate-shake': shakeForm }"
        @animationend="shakeForm = false"
      >
        <h2 class="text-xl font-semibold text-surface-900 mb-6">Create Account</h2>

        <form @submit.prevent="handleRegister" class="space-y-4">
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
                placeholder="Choose a username"
                required
              />
            </div>
          </div>

          <!-- Email -->
          <div>
            <label class="block text-sm font-medium text-surface-700 mb-1.5">Email</label>
            <div class="relative">
              <AppIcon icon="mail" :size="16" class="input-icon" />
              <input
                v-model="email"
                type="email"
                class="input-field input-with-icon"
                :class="{ error: error }"
                placeholder="your@email.com"
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
                placeholder="At least 6 characters"
                required
              />
            </div>
          </div>

          <!-- Confirm Password -->
          <div>
            <label class="block text-sm font-medium text-surface-700 mb-1.5">Confirm Password</label>
            <div class="relative">
              <AppIcon icon="lock" :size="16" class="input-icon" />
              <input
                v-model="confirmPassword"
                type="password"
                class="input-field input-with-icon"
                :class="{ error: error }"
                placeholder="Confirm your password"
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
            {{ loading ? 'Creating account...' : 'Create Account' }}
          </button>
        </form>

        <p class="text-sm text-center text-surface-500 mt-6">
          Already have an account?
          <router-link to="/login" class="text-primary-600 hover:text-primary-700 font-semibold transition-colors">
            Sign in
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>
