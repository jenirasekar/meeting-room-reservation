<script setup>
import { useAuthStore } from './stores/auth'
import Navbar from './components/Navbar.vue'
import Toast from './components/Toast.vue'

const auth = useAuthStore()
</script>

<template>
  <div class="min-h-screen bg-surface-50">
    <Navbar v-if="auth.isLoggedIn" />
    <main :class="auth.isLoggedIn ? 'pt-16' : ''">
      <router-view v-slot="{ Component, route }">
        <Transition name="page" mode="out-in">
          <component :is="Component" :key="route.path" />
        </Transition>
      </router-view>
    </main>
    <Toast />
  </div>
</template>
