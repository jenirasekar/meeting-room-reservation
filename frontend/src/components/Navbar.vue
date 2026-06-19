<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter, useRoute } from 'vue-router'
import AppIcon from './AppIcon.vue'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const scrolled = ref(false)
const mobileMenuOpen = ref(false)
const userMenuOpen = ref(false)

function onScroll() {
  scrolled.value = window.scrollY > 10
}
onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onUnmounted(() => window.removeEventListener('scroll', onScroll))

function goTo(path) {
  router.push(path)
  mobileMenuOpen.value = false
}

function isActive(path) {
  return route.path === path || route.path.startsWith(path + '/')
}

async function handleLogout() {
  userMenuOpen.value = false
  mobileMenuOpen.value = false
  await auth.logout()
  router.push('/login')
}

const navLinks = [
  { path: '/rooms', label: 'Rooms', icon: 'building', show: () => true },
  { path: '/my-reservations', label: 'My Bookings', icon: 'calendar', show: () => !auth.isAdmin },
  { path: '/admin', label: 'Dashboard', icon: 'chart-bar', show: () => auth.isAdmin, exact: true },
  { path: '/admin/rooms', label: 'Manage Rooms', icon: 'settings', show: () => auth.isAdmin },
  { path: '/admin/reservations', label: 'Manage Bookings', icon: 'check-circle', show: () => auth.isAdmin },
  { path: '/admin/users', label: 'Manage Users', icon: 'users', show: () => auth.isAdmin },
]
</script>

<template>
  <nav
    :class="[
      'fixed top-0 left-0 right-0 z-50 transition-all duration-300',
      scrolled
        ? 'bg-white/90 backdrop-blur-xl shadow-soft border-b border-surface-100'
        : 'bg-white/80 backdrop-blur-xl border-b border-transparent'
    ]"
  >
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <!-- Logo -->
        <div class="flex items-center gap-2.5 cursor-pointer group" @click="goTo('/')">
          <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-primary-500 to-primary-700 flex items-center justify-center shadow-sm shadow-primary-500/30 group-hover:shadow-md group-hover:shadow-primary-500/40 transition-shadow">
            <AppIcon icon="building" :size="18" class="text-white" />
          </div>
          <span class="text-lg font-bold text-surface-800 hidden sm:block tracking-tight">
            Meeting<span class="text-primary-600">Room</span>
          </span>
        </div>

        <!-- Desktop nav links -->
        <div class="hidden md:flex items-center gap-1">
          <template v-for="link in navLinks" :key="link.path">
            <button
              v-if="link.show()"
              @click="goTo(link.path)"
              :class="[
                'px-3.5 py-2 rounded-xl text-sm font-medium transition-all duration-200 flex items-center gap-1.5',
                (link.exact ? route.path === link.path : isActive(link.path))
                  ? 'bg-primary-50 text-primary-700'
                  : 'text-surface-600 hover:text-surface-900 hover:bg-surface-100'
              ]"
            >
              <AppIcon :icon="link.icon" :size="16" />
              {{ link.label }}
            </button>
          </template>
        </div>

        <!-- Right section -->
        <div class="flex items-center gap-3">
          <!-- Desktop user menu -->
          <div class="hidden md:flex items-center gap-3">
            <div class="relative">
              <button
                @click="userMenuOpen = !userMenuOpen"
                class="flex items-center gap-2 px-3 py-1.5 rounded-xl hover:bg-surface-100 transition-colors"
              >
                <div class="w-8 h-8 rounded-full bg-gradient-to-br from-primary-400 to-accent-500 flex items-center justify-center text-white text-xs font-bold shadow-sm">
                  {{ (auth.user?.username || 'U')[0].toUpperCase() }}
                </div>
                <span class="text-sm font-medium text-surface-700 hidden lg:block">
                  {{ auth.user?.username }}
                </span>
                <span v-if="auth.isAdmin" class="badge bg-purple-50 text-purple-700 border-purple-200 text-[10px]">
                  Admin
                </span>
              </button>

              <!-- Dropdown -->
              <Transition name="modal-content">
                <div
                  v-if="userMenuOpen"
                  class="absolute right-0 top-full mt-2 w-48 bg-white rounded-xl shadow-soft-xl border border-surface-100 py-1 overflow-hidden"
                  @click="userMenuOpen = false"
                >
                  <div class="px-4 py-2 border-b border-surface-100">
                    <p class="text-sm font-medium text-surface-800">{{ auth.user?.username }}</p>
                    <p class="text-xs text-surface-400">{{ auth.isAdmin ? 'Administrator' : 'User' }}</p>
                  </div>
                  <button
                    @click="handleLogout"
                    class="w-full flex items-center gap-2 px-4 py-2.5 text-sm text-red-600 hover:bg-red-50 transition-colors"
                  >
                    <AppIcon icon="logout" :size="16" />
                    Sign Out
                  </button>
                </div>
              </Transition>

              <!-- Backdrop -->
              <div
                v-if="userMenuOpen"
                class="fixed inset-0 z-[-1]"
                @click="userMenuOpen = false"
              />
            </div>
          </div>

          <!-- Mobile hamburger -->
          <button
            @click="mobileMenuOpen = !mobileMenuOpen"
            class="md:hidden p-2 rounded-xl hover:bg-surface-100 transition-colors"
          >
            <AppIcon :icon="mobileMenuOpen ? 'close' : 'menu'" :size="22" class="text-surface-600" />
          </button>
        </div>
      </div>
    </div>

    <!-- Mobile drawer -->
    <Transition name="modal-content">
      <div
        v-if="mobileMenuOpen"
        class="md:hidden border-t border-surface-100 bg-white/95 backdrop-blur-xl"
      >
        <div class="px-4 py-3 space-y-1">
          <template v-for="link in navLinks" :key="link.path">
            <button
              v-if="link.show()"
              @click="goTo(link.path)"
              :class="[
                'w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all',
                (link.exact ? route.path === link.path : isActive(link.path))
                  ? 'bg-primary-50 text-primary-700'
                  : 'text-surface-600 hover:bg-surface-100'
              ]"
            >
              <AppIcon :icon="link.icon" :size="18" />
              {{ link.label }}
            </button>
          </template>

          <!-- Mobile user info -->
          <div class="border-t border-surface-100 mt-2 pt-3 px-3">
            <div class="flex items-center gap-3 mb-3">
              <div class="w-9 h-9 rounded-full bg-gradient-to-br from-primary-400 to-accent-500 flex items-center justify-center text-white text-sm font-bold">
                {{ (auth.user?.username || 'U')[0].toUpperCase() }}
              </div>
              <div>
                <p class="text-sm font-medium text-surface-800">{{ auth.user?.username }}</p>
                <p v-if="auth.isAdmin" class="text-xs text-purple-600">Administrator</p>
              </div>
            </div>
            <button
              @click="handleLogout"
              class="w-full flex items-center gap-2 px-3 py-2 rounded-xl text-sm text-red-600 hover:bg-red-50 transition-colors"
            >
              <AppIcon icon="logout" :size="18" />
              Sign Out
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </nav>
</template>
