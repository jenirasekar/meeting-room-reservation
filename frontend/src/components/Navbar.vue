<script setup>
import { useAuthStore } from '../stores/auth'
import { useRouter, useRoute } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

function goTo(path) {
  router.push(path)
}

function isActive(path) {
  return route.path === path || route.path.startsWith(path + '/')
}

async function handleLogout() {
  await auth.logout()
  router.push('/login')
}
</script>

<template>
  <nav class="fixed top-0 left-0 right-0 z-50 bg-white shadow-sm border-b border-gray-200">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <!-- Logo -->
        <div class="flex items-center gap-2 cursor-pointer" @click="goTo('/')">
          <span class="text-2xl">🏢</span>
          <span class="text-lg font-bold text-gray-800 hidden sm:block">
            MeetingRoom
          </span>
        </div>

        <!-- Navigation Links -->
        <div class="flex items-center gap-1">
          <button
            @click="goTo('/rooms')"
            :class="[
              'px-3 py-2 rounded-lg text-sm font-medium transition-colors',
              isActive('/rooms') && !isActive('/admin')
                ? 'bg-primary-50 text-primary-700'
                : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
            ]"
          >
            Rooms
          </button>

          <button
            v-if="!auth.isAdmin"
            @click="goTo('/my-reservations')"
            :class="[
              'px-3 py-2 rounded-lg text-sm font-medium transition-colors',
              isActive('/my-reservations')
                ? 'bg-primary-50 text-primary-700'
                : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
            ]"
          >
            My Bookings
          </button>

          <!-- Admin menu -->
          <template v-if="auth.isAdmin">
            <button
              @click="goTo('/admin')"
              :class="[
                'px-3 py-2 rounded-lg text-sm font-medium transition-colors',
                route.path === '/admin'
                  ? 'bg-primary-50 text-primary-700'
                  : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
              ]"
            >
              Dashboard
            </button>
            <button
              @click="goTo('/admin/rooms')"
              :class="[
                'px-3 py-2 rounded-lg text-sm font-medium transition-colors',
                isActive('/admin/rooms')
                  ? 'bg-primary-50 text-primary-700'
                  : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
              ]"
            >
              Manage Rooms
            </button>
            <button
              @click="goTo('/admin/reservations')"
              :class="[
                'px-3 py-2 rounded-lg text-sm font-medium transition-colors',
                isActive('/admin/reservations')
                  ? 'bg-primary-50 text-primary-700'
                  : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
              ]"
            >
              Manage Bookings
            </button>
          </template>
        </div>

        <!-- User menu -->
        <div class="flex items-center gap-3">
          <span class="text-sm text-gray-500 hidden md:block">
            {{ auth.user?.username }}
            <span v-if="auth.isAdmin" class="badge bg-purple-100 text-purple-700 ml-1">Admin</span>
          </span>
          <button
            @click="handleLogout"
            class="btn-secondary btn-sm"
          >
            Logout
          </button>
        </div>
      </div>
    </div>
  </nav>
</template>
