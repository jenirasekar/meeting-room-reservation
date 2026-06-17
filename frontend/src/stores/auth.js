import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authAPI } from '../api'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const loading = ref(false)

  const isLoggedIn = computed(() => user.value !== null)
  const isAdmin = computed(() => user.value?.role === 'admin')

  async function fetchUser() {
    try {
      loading.value = true
      const { data } = await authAPI.me()
      if (data.success) {
        user.value = data.data
      }
    } catch (e) {
      user.value = null
    } finally {
      loading.value = false
    }
  }

  async function login(credentials) {
    const { data } = await authAPI.login(credentials)
    if (data.success) {
      user.value = data.data
    }
    return data
  }

  async function register(formData) {
    const { data } = await authAPI.register(formData)
    return data
  }

  async function logout() {
    try {
      await authAPI.logout()
    } catch (e) {
      // ignore
    }
    user.value = null
  }

  return { user, loading, isLoggedIn, isAdmin, fetchUser, login, register, logout }
})
