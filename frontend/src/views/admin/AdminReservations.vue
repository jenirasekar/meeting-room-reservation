<script setup>
import { ref, onMounted } from 'vue'
import { useReservationsStore } from '../../stores/reservations'
import { useToastStore } from '../../stores/toast'
import SkeletonLoader from '../../components/SkeletonLoader.vue'
import AppIcon from '../../components/AppIcon.vue'

const store = useReservationsStore()
const toast = useToastStore()
const statusFilter = ref('')
const rejectModal = ref(null)

onMounted(() => {
  store.fetchReservations()
})

function applyFilter() {
  store.fetchReservations({ status: statusFilter.value || undefined })
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

function formatDateShort(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString([], { month: 'short', day: 'numeric' })
}

function formatTimeShort(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

async function handleApprove(id) {
  const result = await store.approveReservation(id)
  if (result.success) {
    toast.success('Reservation approved!')
  } else {
    toast.error(result.message || 'Failed to approve')
  }
}

function openReject(id) {
  rejectModal.value = { id, note: '' }
}

async function confirmReject() {
  if (!rejectModal.value) return
  const result = await store.rejectReservation(rejectModal.value.id, rejectModal.value.note)
  if (result.success) {
    toast.info('Reservation rejected')
    rejectModal.value = null
  } else {
    toast.error(result.message || 'Failed to reject')
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
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8">
      <div>
        <h1 class="text-2xl font-bold text-surface-900">Manage Reservations</h1>
        <p class="text-surface-500 mt-1">Approve or reject booking requests</p>
      </div>
      <button @click="store.fetchReservations()" class="btn-secondary mt-4 sm:mt-0">
        <AppIcon icon="refresh" :size="16" />
        Refresh
      </button>
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

    <!-- Loading -->
    <div v-if="store.loading" class="space-y-4">
      <SkeletonLoader variant="list-item" :count="5" />
    </div>

    <!-- Empty -->
    <div v-else-if="!store.reservations.length" class="text-center py-16">
      <div class="w-20 h-20 rounded-2xl bg-surface-100 flex items-center justify-center mx-auto mb-4">
        <AppIcon icon="calendar" :size="36" class="text-surface-300" />
      </div>
      <h3 class="text-lg font-semibold text-surface-700 mb-1">No reservations</h3>
      <p class="text-surface-400">No bookings match the current filter.</p>
    </div>

    <!-- Reservation list -->
    <div v-else class="space-y-3">
      <div
        v-for="res in store.reservations"
        :key="res.id"
        class="card hover:shadow-soft-lg transition-all"
      >
        <div class="flex flex-col lg:flex-row gap-4">
          <!-- Left info -->
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-3 mb-2">
              <h3 class="text-lg font-semibold text-surface-900 truncate">{{ res.title }}</h3>
              <span :class="statusBadgeClass(res.status)" class="shrink-0">
                <AppIcon :name="statusIcon(res.status)" :size="12" />
                {{ res.status }}
              </span>
            </div>
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-x-4 gap-y-1 text-sm text-surface-500">
              <span class="flex items-center gap-1.5">
                <AppIcon icon="building" :size="13" class="text-surface-400" />
                {{ res.roomName || `Room #${res.roomId}` }}
              </span>
              <span class="flex items-center gap-1.5">
                <AppIcon icon="user" :size="13" class="text-surface-400" />
                {{ res.username || `User #${res.userId}` }}
              </span>
              <span class="flex items-center gap-1.5">
                <AppIcon icon="calendar" :size="13" class="text-surface-400" />
                {{ formatDateShort(res.startTime) }}
              </span>
              <span class="flex items-center gap-1.5">
                <AppIcon icon="clock" :size="13" class="text-surface-400" />
                {{ formatTimeShort(res.startTime) }} – {{ formatTimeShort(res.endTime) }}
              </span>
              <span class="flex items-center gap-1.5 text-surface-400 text-xs">
                Created {{ formatDateShort(res.createdAt) }}
              </span>
            </div>
          </div>

          <!-- Admin actions -->
          <div v-if="res.status === 'pending'" class="flex items-center gap-2 shrink-0">
            <button @click="handleApprove(res.id)" class="btn-success btn-sm">
              <AppIcon icon="check" :size="14" />
              Approve
            </button>
            <button @click="openReject(res.id)" class="btn-danger btn-sm">
              <AppIcon icon="x-mark" :size="14" />
              Reject
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Reject modal -->
    <Teleport to="body">
      <Transition icon="modal-backdrop">
        <div v-if="rejectModal" class="fixed inset-0 z-50 flex items-center justify-center p-4" @click.self="rejectModal = null">
          <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" />
          <Transition icon="modal-content">
            <div v-if="rejectModal" class="relative bg-white rounded-2xl shadow-soft-xl w-full max-w-sm p-6">
              <h3 class="text-lg font-semibold text-surface-900 mb-3">Reject Reservation</h3>
              <div class="mb-4">
                <label class="block text-sm font-medium text-surface-700 mb-1.5">Reason (optional)</label>
                <textarea
                  v-model="rejectModal.note"
                  class="input-field"
                  rows="2"
                  placeholder="Why is this being rejected?"
                />
              </div>
              <div class="flex gap-3">
                <button @click="confirmReject" class="btn-danger flex-1">Reject</button>
                <button @click="rejectModal = null" class="btn-secondary">Cancel</button>
              </div>
            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>
