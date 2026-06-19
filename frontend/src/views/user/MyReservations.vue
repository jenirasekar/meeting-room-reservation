<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useReservationsStore } from '../../stores/reservations'
import { useToastStore } from '../../stores/toast'
import SkeletonLoader from '../../components/SkeletonLoader.vue'
import AppIcon from '../../components/AppIcon.vue'

const reservationsStore = useReservationsStore()
const toast = useToastStore()
const statusFilter = ref('')
const searchQuery = ref('')
const cancelConfirmId = ref(null)
const now = ref(new Date())

const filteredReservations = computed(() => {
  if (!searchQuery.value.trim()) return reservationsStore.reservations
  const q = searchQuery.value.toLowerCase()
  return reservationsStore.reservations.filter(r =>
    r.title.toLowerCase().includes(q) ||
    (r.roomName && r.roomName.toLowerCase().includes(q))
  )
})

let timer = null
onMounted(() => {
  reservationsStore.fetchReservations()
  timer = setInterval(() => { now.value = new Date() }, 30_000)
})
onUnmounted(() => {
  clearInterval(timer)
})

function canCheckin(res) {
  if (res.status !== 'approved') return false
  const start = new Date(res.startTime)
  const end = new Date(res.endTime)
  return now.value >= start && now.value <= end
}

function applyFilter() {
  reservationsStore.fetchReservations({ status: statusFilter.value || undefined })
}

const statusTabs = [
  { value: '', label: 'All' },
  { value: 'pending', label: 'Pending' },
  { value: 'approved', label: 'Approved' },
  { value: 'completed', label: 'Completed' },
  { value: 'cancelled', label: 'Cancelled' },
  { value: 'rejected', label: 'Rejected' },
]

function formatDateTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

function formatDateOnly(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString([], { weekday: 'short', month: 'short', day: 'numeric' })
}

function formatTimeOnly(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

async function handleCancel(id) {
  if (cancelConfirmId.value === id) {
    const result = await reservationsStore.cancelReservation(id)
    if (result.success) {
      toast.success('Reservation cancelled')
      await reservationsStore.fetchReservations()
    } else {
      toast.error(result.message || 'Failed to cancel')
    }
    cancelConfirmId.value = null
  } else {
    cancelConfirmId.value = id
  }
}

async function handleCheckin(id) {
  const result = await reservationsStore.checkin(id)
  if (result.success) {
    toast.success('Check-in successful!')
    await reservationsStore.fetchReservations()
  } else {
    toast.error(result.message || 'Check-in failed')
  }
}

function statusBadgeClass(status) {
  const map = {
    pending: 'badge-pending',
    approved: 'badge-approved',
    rejected: 'badge-rejected',
    cancelled: 'badge-cancelled',
    completed: 'badge-completed'
  }
  return map[status] || 'badge'
}

function statusIcon(status) {
  const map = {
    pending: 'clock',
    approved: 'check-circle',
    rejected: 'x-mark',
    cancelled: 'close',
    completed: 'check',
  }
  return map[status] || 'information-circle'
}
</script>

<template>
  <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8">
      <div>
        <h1 class="text-2xl font-bold text-surface-900">My Reservations</h1>
        <p class="text-surface-500 mt-1">Track and manage your room bookings</p>
      </div>
      <router-link to="/rooms" class="btn btn-primary mt-4 sm:mt-0">
        <AppIcon icon="plus" :size="16" />
        New Booking
      </router-link>
    </div>

    <!-- Filter tabs -->
    <div class="flex gap-1.5 mb-6 overflow-x-auto pb-2">
      <button
        v-for="tab in statusTabs"
        :key="tab.value"
        @click="statusFilter = tab.value; applyFilter()"
        :class="[
          'px-4 py-2 rounded-xl text-sm font-medium transition-all shrink-0',
          statusFilter === tab.value
            ? 'bg-primary-600 text-white shadow-sm shadow-primary-600/25'
            : 'bg-white text-surface-500 hover:text-surface-700 hover:bg-surface-100 border border-surface-200'
        ]"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- Search -->
    <div class="relative max-w-sm mb-6">
      <AppIcon icon="search" :size="16" class="absolute left-3 top-1/2 -translate-y-1/2 text-surface-400 z-10 pointer-events-none" />
      <input
        v-model="searchQuery"
        type="text"
        class="w-full pl-10 pr-4 py-2.5 rounded-xl bg-white border border-surface-200 text-sm placeholder-surface-400 focus:outline-none focus:ring-2 focus:ring-primary-500/30 focus:border-primary-400"
        placeholder="Search by title or room name..."
      />
    </div>

    <!-- Loading -->
    <div v-if="reservationsStore.loading" class="space-y-4">
      <SkeletonLoader variant="list-item" :count="4" />
    </div>

    <!-- Empty -->
    <div v-else-if="!reservationsStore.reservations.length" class="text-center py-16">
      <div class="w-20 h-20 rounded-2xl bg-surface-100 flex items-center justify-center mx-auto mb-4">
        <AppIcon icon="calendar" :size="36" class="text-surface-300" />
      </div>
      <h3 class="text-lg font-semibold text-surface-700 mb-1">No reservations yet</h3>
      <p class="text-surface-400 mb-4">Start by browsing available rooms.</p>
      <router-link to="/rooms" class="btn btn-primary">
        <AppIcon icon="building" :size="16" />
        Browse Rooms
      </router-link>
    </div>

    <!-- Search empty -->
    <div v-else-if="!filteredReservations.length" class="text-center py-16">
      <div class="w-20 h-20 rounded-2xl bg-surface-100 flex items-center justify-center mx-auto mb-4">
        <AppIcon icon="search" :size="36" class="text-surface-300" />
      </div>
      <h3 class="text-lg font-semibold text-surface-700 mb-1">No matching reservations</h3>
      <p class="text-surface-400">Try a different search term.</p>
    </div>

    <!-- Reservation timeline -->
    <div v-else class="space-y-4">
      <div
        v-for="res in filteredReservations"
        :key="res.id"
        class="card stagger-item hover:shadow-soft-lg transition-all"
      >
        <div class="flex flex-col sm:flex-row gap-4">
          <!-- Left: time column -->
          <div class="sm:w-36 shrink-0 text-sm">
            <div class="flex sm:flex-col sm:items-center items-center gap-2 sm:gap-1.5 sm:text-center">
              <AppIcon icon="calendar" :size="16" class="text-surface-400 sm:mx-auto" />
              <span class="text-surface-600 font-medium">{{ formatDateOnly(res.startTime) }}</span>
              <span class="text-surface-400 sm:text-xs whitespace-nowrap">
                {{ formatTimeOnly(res.startTime) }} – {{ formatTimeOnly(res.endTime) }}
              </span>
            </div>
          </div>

          <!-- Divider -->
          <div class="hidden sm:block w-px bg-surface-100 self-stretch shrink-0" />

          <!-- Right: content -->
          <div class="flex-1 min-w-0">
            <div class="flex items-center justify-between gap-3 mb-2">
              <h3 class="text-lg font-semibold text-surface-900 truncate">{{ res.title }}</h3>
              <span :class="statusBadgeClass(res.status)" class="inline-flex items-center justify-center shrink-0 gap-1.5 min-w-[100px] sm:min-w-[120px] rounded-full px-3 py-1 text-sm font-medium">
                <AppIcon :icon="statusIcon(res.status)" :size="12" />
                {{ res.status }}
              </span>
            </div>

            <div class="flex flex-wrap gap-x-6 gap-y-1 text-sm text-surface-500">
              <span class="flex items-center gap-1.5">
                <AppIcon icon="building" :size="13" class="text-surface-400" />
                {{ res.roomName || `Room #${res.roomId}` }}
              </span>
              <span v-if="res.username" class="flex items-center gap-1.5">
                <AppIcon icon="user" :size="13" class="text-surface-400" />
                {{ res.username }}
              </span>
            </div>

            <!-- Actions -->
            <div class="flex items-center gap-2 mt-3 pt-3 border-t border-surface-100">
              <button
                v-if="canCheckin(res)"
                @click="handleCheckin(res.id)"
                class="btn btn-success btn-sm"
              >
                <AppIcon icon="check" :size="14" />
                Check-in
              </button>
              <span
                v-else-if="res.status === 'approved' && new Date(res.startTime) > now"
                class="text-xs text-surface-400 flex items-center gap-1"
              >
                <AppIcon icon="clock" :size="12" />
                Check-in opens {{ new Date(res.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) }}
              </span>
              <button
                v-if="res.status === 'pending' || res.status === 'approved'"
                @click="handleCancel(res.id)"
                :class="cancelConfirmId === res.id ? 'btn btn-danger btn-sm animate-shake' : 'btn btn-ghost btn-sm text-red-500 hover:bg-red-50'"
              >
                <AppIcon icon="x-mark" :size="14" />
                {{ cancelConfirmId === res.id ? 'Confirm cancel?' : 'Cancel' }}
              </button>
              <button
                v-if="cancelConfirmId === res.id"
                @click="cancelConfirmId = null"
                class="btn btn-ghost btn-sm"
              >
                Keep
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
