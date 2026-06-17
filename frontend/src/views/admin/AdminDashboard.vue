<script setup>
import { onMounted } from 'vue'
import { useReservationsStore } from '../../stores/reservations'

const store = useReservationsStore()

onMounted(() => {
  store.fetchStats()
})
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-8">Admin Dashboard</h1>

    <!-- Loading -->
    <div v-if="store.loading" class="text-center py-12">
      <p class="text-gray-500">Loading statistics...</p>
    </div>

    <!-- Stats -->
    <template v-else-if="store.stats">
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <div class="card text-center">
          <div class="text-3xl font-bold text-primary-600">{{ store.stats.total_rooms }}</div>
          <div class="text-sm text-gray-500 mt-1">Total Rooms</div>
        </div>
        <div class="card text-center">
          <div class="text-3xl font-bold text-green-600">{{ store.stats.reservations_today }}</div>
          <div class="text-sm text-gray-500 mt-1">Reservations Today</div>
        </div>
        <div class="card text-center">
          <div class="text-3xl font-bold text-yellow-600">{{ store.stats.pending_count }}</div>
          <div class="text-sm text-gray-500 mt-1">Pending Approval</div>
        </div>
        <div class="card text-center">
          <div class="text-3xl font-bold text-blue-600">{{ store.stats.checkins_today }}</div>
          <div class="text-sm text-gray-500 mt-1">Check-ins Today</div>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div class="card">
          <h3 class="text-lg font-semibold mb-4">Reservation Status Overview</h3>
          <div class="space-y-3">
            <div class="flex items-center justify-between">
              <span class="text-gray-600">Approved</span>
              <span class="badge-approved">{{ store.stats.approved_count }}</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-gray-600">Pending</span>
              <span class="badge-pending">{{ store.stats.pending_count }}</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-gray-600">Completed</span>
              <span class="badge-completed">{{ store.stats.completed_count }}</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-gray-600">Rejected</span>
              <span class="badge-rejected">{{ store.stats.rejected_count }}</span>
            </div>
          </div>
        </div>

        <div class="card">
          <h3 class="text-lg font-semibold mb-4">Quick Actions</h3>
          <div class="space-y-3">
            <router-link to="/admin/rooms" class="block btn-primary text-center">
              Manage Rooms
            </router-link>
            <router-link to="/admin/reservations" class="block btn-secondary text-center">
              Manage Reservations
            </router-link>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
