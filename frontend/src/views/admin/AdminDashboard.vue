<script setup>
import { onMounted } from 'vue'
import { useReservationsStore } from '../../stores/reservations'
import SkeletonLoader from '../../components/SkeletonLoader.vue'
import AppIcon from '../../components/AppIcon.vue'

const store = useReservationsStore()

onMounted(() => {
  store.fetchStats()
})

const statCards = [
  { key: 'total_rooms', label: 'Total Rooms', icon: 'building', color: 'from-primary-500 to-primary-600', iconBg: 'bg-primary-100', iconColor: 'text-primary-600' },
  { key: 'total_users', label: 'Total Users', icon: 'users', color: 'from-violet-500 to-violet-600', iconBg: 'bg-violet-100', iconColor: 'text-violet-600' },
  { key: 'reservations_today', label: 'Bookings Today', icon: 'calendar', color: 'from-emerald-500 to-emerald-600', iconBg: 'bg-emerald-100', iconColor: 'text-emerald-600' },
  { key: 'pending_count', label: 'Pending Approval', icon: 'clock', color: 'from-amber-500 to-amber-600', iconBg: 'bg-amber-100', iconColor: 'text-amber-600' },
  { key: 'checkins_today', label: 'Check-ins Today', icon: 'check-circle', color: 'from-blue-500 to-blue-600', iconBg: 'bg-blue-100', iconColor: 'text-blue-600' },
]
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-surface-900">Dashboard</h1>
      <p class="text-surface-500 mt-1">Overview of your meeting room system</p>
    </div>

    <!-- Loading -->
    <div v-if="store.loading">
      <SkeletonLoader variant="stats" :count="5" />
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
        <SkeletonLoader variant="card" :count="2" />
      </div>
    </div>

    <!-- Stats -->
    <template v-else-if="store.stats">
      <!-- Stat cards -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-6 mb-8">
        <div
          v-for="stat in statCards"
          :key="stat.key"
          class="card hover:shadow-soft-lg transition-all group"
        >
          <div class="flex items-start justify-between mb-3">
            <div :class="['w-11 h-11 rounded-xl flex items-center justify-center', stat.iconBg]">
              <AppIcon :icon="stat.icon" :size="20" :class="stat.iconColor" />
            </div>
            <div :class="['w-2 h-2 rounded-full', stat.iconColor.replace('text-', 'bg-')]" />
          </div>
          <div :class="['text-3xl font-extrabold bg-clip-text text-transparent bg-gradient-to-r', stat.color]">
            {{ store.stats[stat.key] ?? 0 }}
          </div>
          <div class="text-sm text-surface-500 mt-1">{{ stat.label }}</div>
        </div>
      </div>

      <!-- Bottom grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- Status breakdown -->
        <div class="card">
          <div class="flex items-center gap-2 mb-5">
            <div class="w-8 h-8 rounded-lg bg-primary-50 flex items-center justify-center">
              <AppIcon icon="chart-bar" :size="16" class="text-primary-600" />
            </div>
            <h3 class="text-lg font-semibold text-surface-900">Reservation Status</h3>
          </div>

          <div class="space-y-1">
            <div class="flex items-center justify-between py-3 px-4 rounded-xl hover:bg-surface-50 transition-colors">
              <div class="flex items-center gap-3">
                <span class="w-2.5 h-2.5 rounded-full bg-emerald-500" />
                <span class="text-surface-600 text-sm">Approved</span>
              </div>
              <span class="badge-approved w-5 text-center rounded">{{ store.stats.approved_count }}</span>
            </div>
            <div class="flex items-center justify-between py-3 px-4 rounded-xl hover:bg-surface-50 transition-colors">
              <div class="flex items-center gap-3">
                <span class="w-2.5 h-2.5 rounded-full bg-amber-500" />
                <span class="text-surface-600 text-sm">Pending</span>
              </div>
              <span class="badge-pending w-5 text-center rounded">{{ store.stats.pending_count }}</span>
            </div>
            <div class="flex items-center justify-between py-3 px-4 rounded-xl hover:bg-surface-50 transition-colors">
              <div class="flex items-center gap-3">
                <span class="w-2.5 h-2.5 rounded-full bg-blue-500" />
                <span class="text-surface-600 text-sm">Completed</span>
              </div>
              <span class="badge-completed w-5 text-center rounded">{{ store.stats.completed_count }}</span>
            </div>
            <div class="flex items-center justify-between py-3 px-4 rounded-xl hover:bg-surface-50 transition-colors">
              <div class="flex items-center gap-3">
                <span class="w-2.5 h-2.5 rounded-full bg-red-500" />
                <span class="text-surface-600 text-sm">Rejected</span>
              </div>
              <span class="badge-rejected w-5 text-center rounded">{{ store.stats.rejected_count }}</span>
            </div>
          </div>
        </div>

        <!-- Quick actions -->
        <div class="card">
          <div class="flex items-center gap-2 mb-5">
            <div class="w-8 h-8 rounded-lg bg-primary-50 flex items-center justify-center">
              <AppIcon icon="sparkles" :size="16" class="text-primary-600" />
            </div>
            <h3 class="text-lg font-semibold text-surface-900">Quick Actions</h3>
          </div>

          <div class="space-y-3">
            <router-link
              to="/admin/rooms"
              class="flex items-center gap-4 p-4 rounded-xl bg-surface-50 hover:bg-surface-100 border border-surface-100 transition-all group"
            >
              <div class="w-10 h-10 rounded-xl bg-primary-100 flex items-center justify-center group-hover:bg-primary-200 transition-colors">
                <AppIcon icon="settings" :size="20" class="text-primary-600" />
              </div>
              <div class="flex-1">
                <p class="font-medium text-surface-800 text-sm">Manage Rooms</p>
                <p class="text-xs text-surface-400">Add, edit, or remove meeting rooms</p>
              </div>
              <AppIcon icon="chevron-right" :size="16" class="text-surface-300 group-hover:text-surface-500 transition-colors" />
            </router-link>

            <router-link
              to="/admin/reservations"
              class="flex items-center gap-4 p-4 rounded-xl bg-surface-50 hover:bg-surface-100 border border-surface-100 transition-all group"
            >
              <div class="w-10 h-10 rounded-xl bg-emerald-100 flex items-center justify-center group-hover:bg-emerald-200 transition-colors">
                <AppIcon icon="check-circle" :size="20" class="text-emerald-600" />
              </div>
              <div class="flex-1">
                <p class="font-medium text-surface-800 text-sm">Manage Reservations</p>
                <p class="text-xs text-surface-400">Approve or reject booking requests</p>
              </div>
              <AppIcon icon="chevron-right" :size="16" class="text-surface-300 group-hover:text-surface-500 transition-colors" />
            </router-link>
          </div>
        </div>
      </div>
    </template>

    <!-- Error / no data -->
    <div v-else class="text-center py-16">
      <div class="w-20 h-20 rounded-2xl bg-surface-100 flex items-center justify-center mx-auto mb-4">
        <AppIcon icon="exclamation-circle" :size="36" class="text-surface-300" />
      </div>
      <p class="text-surface-500">Could not load dashboard data.</p>
      <button @click="store.fetchStats()" class="btn btn-secondary mt-4">
        <AppIcon icon="refresh" :size="16" />
        Retry
      </button>
    </div>
  </div>
</template>
