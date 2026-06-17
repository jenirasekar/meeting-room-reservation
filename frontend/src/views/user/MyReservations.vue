<script setup>
import { ref, onMounted } from 'vue'
import { useReservationsStore } from '../../stores/reservations'

const reservationsStore = useReservationsStore()
const statusFilter = ref('')

onMounted(() => {
  reservationsStore.fetchReservations()
})

function applyFilter() {
  reservationsStore.fetchReservations({ status: statusFilter.value || undefined })
}

function formatDateTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

async function handleCancel(id) {
  if (!confirm('Are you sure you want to cancel this reservation?')) return
  const result = await reservationsStore.cancelReservation(id)
  if (result.success) {
    await reservationsStore.fetchReservations()
  } else {
    alert(result.message || 'Failed to cancel')
  }
}

async function handleCheckin(id) {
  if (!confirm('Confirm check-in for this reservation?')) return
  const result = await reservationsStore.checkin(id)
  if (result.success) {
    alert('Check-in successful!')
    await reservationsStore.fetchReservations()
  } else {
    alert(result.message || 'Check-in failed')
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
</script>

<template>
  <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">My Reservations</h1>
        <p class="text-gray-500 mt-1">View and manage your bookings</p>
      </div>

      <div class="flex gap-2 mt-4 sm:mt-0">
        <select v-model="statusFilter" class="input-field w-auto" @change="applyFilter">
          <option value="">All Status</option>
          <option value="pending">Pending</option>
          <option value="approved">Approved</option>
          <option value="rejected">Rejected</option>
          <option value="cancelled">Cancelled</option>
          <option value="completed">Completed</option>
        </select>
        <button @click="reservationsStore.fetchReservations()" class="btn-secondary">
          Refresh
        </button>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="reservationsStore.loading" class="text-center py-12">
      <p class="text-gray-500">Loading reservations...</p>
    </div>

    <!-- Empty -->
    <div v-else-if="!reservationsStore.reservations.length" class="text-center py-12">
      <p class="text-gray-500 text-lg">No reservations found</p>
      <router-link to="/rooms" class="text-primary-600 hover:underline mt-2 inline-block">
        Browse rooms
      </router-link>
    </div>

    <!-- Reservation list -->
    <div v-else class="space-y-4">
      <div
        v-for="res in reservationsStore.reservations"
        :key="res.id"
        class="card hover:shadow-md transition-shadow"
      >
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div class="flex-1">
            <div class="flex items-center gap-3 mb-2">
              <h3 class="text-lg font-semibold text-gray-900">{{ res.title }}</h3>
              <span :class="statusBadgeClass(res.status)">{{ res.status }}</span>
            </div>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-2 text-sm text-gray-500">
              <div>
                <span class="font-medium">Room:</span> {{ res.roomName || `Room #${res.roomId}` }}
              </div>
              <div>
                <span class="font-medium">Start:</span> {{ formatDateTime(res.startTime) }}
              </div>
              <div>
                <span class="font-medium">End:</span> {{ formatDateTime(res.endTime) }}
              </div>
              <div v-if="res.username">
                <span class="font-medium">User:</span> {{ res.username }}
              </div>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex items-center gap-2 shrink-0">
            <button
              v-if="res.status === 'approved'"
              @click="handleCheckin(res.id)"
              class="btn-success btn-sm"
            >
              Check-in
            </button>
            <button
              v-if="res.status === 'pending' || res.status === 'approved'"
              @click="handleCancel(res.id)"
              class="btn-danger btn-sm"
            >
              Cancel
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
