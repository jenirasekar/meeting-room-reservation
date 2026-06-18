<script setup>
import AppIcon from '../components/AppIcon.vue'
</script>

<template>
  <div class="min-h-[80vh] flex items-center justify-center px-4 relative overflow-hidden">
    <!-- Floating background shapes -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute w-64 h-64 bg-primary-100/50 rounded-full blur-3xl top-1/4 left-1/4 animate-float" />
      <div class="absolute w-48 h-48 bg-accent-100/50 rounded-full blur-3xl bottom-1/4 right-1/3 animate-float-delayed" />
    </div>

    <div class="text-center relative">
      <!-- Animated 404 -->
      <div class="text-[120px] sm:text-[160px] font-extrabold leading-none mb-2">
        <span class="gradient-text bg-gradient-to-r from-primary-500 via-accent-500 to-primary-500 bg-[length:200%_auto] animate-shimmer">4</span>
        <span class="inline-block animate-bounce-in" style="animation-delay: 0.2s">
          <span class="gradient-text bg-gradient-to-r from-accent-500 to-primary-500">0</span>
        </span>
        <span class="gradient-text bg-gradient-to-r from-primary-500 via-accent-500 to-primary-500 bg-[length:200%_auto] animate-shimmer" style="animation-delay: 0.4s">4</span>
      </div>

      <div class="w-16 h-16 rounded-2xl bg-surface-100 flex items-center justify-center mx-auto mb-5">
        <AppIcon icon="exclamation-circle" :size="32" class="text-surface-300" />
      </div>

      <h1 class="text-2xl font-bold text-surface-900 mb-2">Page Not Found</h1>
      <p class="text-surface-500 mb-8 max-w-sm mx-auto">
        The page you're looking for doesn't exist or has been moved. Let's get you back on track.
      </p>

      <div class="flex items-center justify-center gap-3">
        <router-link to="/rooms" class="btn-primary btn-lg">
          <AppIcon icon="building" :size="18" />
          Browse Rooms
        </router-link>
        <button @click="$router.back()" class="btn-secondary btn-lg">
          <AppIcon icon="arrow-left" :size="18" />
          Go Back
        </button>
      </div>
    </div>
  </div>
</template>
