<script setup>
import { ref, onMounted } from 'vue'
import { useReservationsStore } from '../../stores/reservations'

const store = useReservationsStore()
const statusFilter = ref('')
const rejectNote = ref('')

onMounted(() => {
  store.fetchReservations()
})

function applyFilter() {
  store.fetchReservations({ status: statusFilter.value || undefined })
}

function formatDateTime(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString()
}

async function handleApprove(id) {
  const result = await store.approveReservation(id)
  if (!result.success) alert(result.message || 'Failed to approve')
}

async function handleReject(id) {
  const note = prompt('Rejection reason (optional):')
  const result = await store.rejectReservation(id, note || '')
  if (!result.success) alert(result.message || 'Failed to reject')
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
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Manage Reservations</h1>
        <p class="text-gray-500 mt-1">Approve or reject booking requests</p>
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
        <button @click="store.fetchReservations()" class="btn-secondary">Refresh</button>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="store.loading" class="text-center py-12">
      <p class="text-gray-500">Loading reservations...</p>
    </div>

    <!-- Empty -->
    <div v-else-if="!store.reservations.length" class="text-center py-12">
      <p class="text-gray-500 text-lg">No reservations found</p>
    </div>

    <!-- Reservation list -->
    <div v-else class="space-y-4">
      <div
        v-for="res in store.reservations"
        :key="res.id"
        class="card hover:shadow-md transition-shadow"
      >
        <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div class="flex-1">
            <div class="flex items-center gap-3 mb-2">
              <h3 class="text-lg font-semibold text-gray-900">{{ res.title }}</h3>
              <span :class="statusBadgeClass(res.status)">{{ res.status }}</span>
            </div>
            <div class="grid grid-cols-1 sm:grid-cols-3 gap-2 text-sm text-gray-500">
              <div>
                <span class="font-medium">Room:</span> {{ res.roomName || `#${res.roomId}` }}
              </div>
              <div>
                <span class="font-medium">User:</span> {{ res.username || `#${res.userId}` }}
              </div>
              <div>
                <span class="font-medium">Start:</span> {{ formatDateTime(res.startTime) }}
              </div>
              <div>
                <span class="font-medium">End:</span> {{ formatDateTime(res.endTime) }}
              </div>
              <div>
                <span class="font-medium">Created:</span> {{ formatDateTime(res.createdAt) }}
              </div>
            </div>
          </div>

          <!-- Admin actions -->
          <div v-if="res.status === 'pending'" class="flex items-center gap-2 shrink-0">
            <button @click="handleApprove(res.id)" class="btn-success btn-sm">
              Approve
            </button>
            <button @click="handleReject(res.id)" class="btn-danger btn-sm">
              Reject
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
