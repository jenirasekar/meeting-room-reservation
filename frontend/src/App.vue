<script setup>
import { useAuthStore } from './stores/auth'
import Navbar from './components/Navbar.vue'

const auth = useAuthStore()
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Navbar v-if="auth.isLoggedIn" />
    <main :class="auth.isLoggedIn ? 'pt-16' : ''">
      <router-view />
    </main>
  </div>
</template>
