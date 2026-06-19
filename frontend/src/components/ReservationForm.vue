<script setup>
import { ref, watch } from 'vue'
import { useReservationsStore } from '../stores/reservations'
import { useRoomsStore } from '../stores/rooms'
import { useToastStore } from '../stores/toast'
import AppIcon from './AppIcon.vue'

const props = defineProps({
  roomId: { type: Number, required: true }
})

const emit = defineEmits(['success', 'cancel'])

const reservationsStore = useReservationsStore()
const roomsStore = useRoomsStore()
const toast = useToastStore()

const title = ref('')
const date = ref('')
const startTime = ref('09:00')
const endTime = ref('10:00')
const submitting = ref(false)
const error = ref('')
const conflictWarning = ref('')
const loadingSlots = ref(false)
const bookedSlots = ref([])

watch(date, async (newDate) => {
  conflictWarning.value = ''
  bookedSlots.value = []
  if (!newDate) return

  try {
    loadingSlots.value = true
    const { data } = await roomsStore.fetchAvailability(props.roomId, newDate)
    if (data?.success) bookedSlots.value = data.data ?? []
  } catch (e) {
    console.error('Failed to fetch availability:', e)
  } finally {
    loadingSlots.value = false
  }
})

function parseLocalDateTime(str) {
  if (!str) return null
  const [datePart, timePart] = str.split('T')
  const [year, month, day] = datePart.split('-').map(Number)
  const [hour, minute, second] = (timePart || '00:00:00').split(':').map(Number)
  return new Date(year, month - 1, day, hour, minute, second || 0)
}

function formatTime(date) {
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

function checkConflict() {
  if (!date.value || !bookedSlots.value.length) return false

  // Use same manual parsing as parseLocalDateTime for cross-browser consistency
  const selectedStart = parseLocalDateTime(`${date.value}T${startTime.value}:00`)
  const selectedEnd = parseLocalDateTime(`${date.value}T${endTime.value}:00`)
  if (!selectedStart || !selectedEnd) return false

  for (const slot of bookedSlots.value) {
    const slotStart = parseLocalDateTime(slot.startTime)
    const slotEnd = parseLocalDateTime(slot.endTime)
    if (!slotStart || !slotEnd) continue
    if (selectedStart < slotEnd && selectedEnd > slotStart) {
      conflictWarning.value = `Conflicts with "${slot.title}" (${formatTime(slotStart)} – ${formatTime(slotEnd)})`
      return true
    }
  }
  conflictWarning.value = ''
  return false
}

async function handleSubmit() {
  error.value = ''

  if (!title.value.trim()) { error.value = 'Meeting title is required'; return }
  if (!date.value) { error.value = 'Please select a date'; return }
  if (!startTime.value || !endTime.value) { error.value = 'Start and end time are required'; return }
  if (startTime.value >= endTime.value) { error.value = 'End time must be after start time'; return }

  if (checkConflict()) {
    error.value = conflictWarning.value
    return
  }

  try {
    submitting.value = true
    const resData = {
      room_id: props.roomId,
      title: title.value.trim(),
      start_time: `${date.value}T${startTime.value}:00`,
      end_time: `${date.value}T${endTime.value}:00`
    }
    const result = await reservationsStore.createReservation(resData)
    if (result.success) {
      toast.success('Reservation submitted! Waiting for admin approval.')
      emit('success', result)
    } else {
      error.value = result.message || 'Failed to create reservation'
    }
  } catch (e) {
    error.value = e.response?.data?.message || 'Failed to create reservation'
  } finally {
    submitting.value = false
  }
}

function getToday() {
  return new Date().toISOString().split('T')[0]
}
</script>

<template>
  <div class="card">
    <div class="flex items-center gap-2 mb-5">
      <div class="w-8 h-8 rounded-lg bg-primary-50 flex items-center justify-center">
        <AppIcon icon="calendar" :size="16" class="text-primary-600" />
      </div>
      <h3 class="text-lg font-semibold text-surface-900">Book This Room</h3>
    </div>

    <form @submit.prevent="handleSubmit" class="space-y-4">
      <!-- Meeting Title -->
      <div>
        <label class="block text-sm font-medium text-surface-700 mb-1.5">
          Meeting Title <span class="text-red-400">*</span>
        </label>
        <input
          v-model="title"
          type="text"
          class="input-field"
          :class="{ error: error && !title.trim() }"
          placeholder="e.g., Sprint Planning Session"
          required
        />
      </div>

      <!-- Date -->
      <div>
        <label class="block text-sm font-medium text-surface-700 mb-1.5">
          Date <span class="text-red-400">*</span>
        </label>
        <input
          v-model="date"
          type="date"
          class="input-field"
          :min="getToday()"
          required
        />
        <div class="mt-1.5">
          <p v-if="loadingSlots" class="text-xs text-surface-400 flex items-center gap-1">
            <span class="inline-block w-3 h-3 border-2 border-surface-300 border-t-primary-500 rounded-full animate-spin" />
            Checking availability...
          </p>
          <p v-else-if="date && bookedSlots.length" class="text-xs text-surface-500 flex items-center gap-1">
            <span class="w-1.5 h-1.5 rounded-full bg-amber-400" />
            {{ bookedSlots.length }} booking(s) on this date
          </p>
          <p v-else-if="date && !loadingSlots" class="text-xs text-emerald-600 flex items-center gap-1">
            <span class="w-1.5 h-1.5 rounded-full bg-emerald-400" />
            No bookings — date is free
          </p>
        </div>
      </div>

      <!-- Time range -->
      <div>
        <label class="block text-sm font-medium text-surface-700 mb-1.5">
          Time <span class="text-red-400">*</span>
        </label>
        <div class="grid grid-cols-2 gap-3">
          <div class="relative">
            <AppIcon icon="clock" :size="14" class="input-icon left-3" />
            <input v-model="startTime" type="time" class="input-field input-with-icon" required />
          </div>
          <div class="relative">
            <AppIcon icon="clock" :size="14" class="input-icon left-3" />
            <input v-model="endTime" type="time" class="input-field input-with-icon" required />
          </div>
        </div>
      </div>

      <!-- Booked slots -->
      <div v-if="bookedSlots.length" class="bg-surface-50 rounded-xl p-3.5 border border-surface-100">
        <p class="text-xs font-semibold text-surface-500 uppercase tracking-wider mb-2.5">Booked Slots</p>
        <div
          v-for="slot in bookedSlots"
          :key="slot.id"
          class="flex items-center justify-between text-xs text-surface-500 py-1.5 border-b border-surface-100 last:border-0"
        >
          <span class="font-medium text-surface-700 truncate mr-2">{{ slot.title }}</span>
          <span class="shrink-0 text-surface-400">
            {{ formatTime(parseLocalDateTime(slot.startTime)) }} – {{ formatTime(parseLocalDateTime(slot.endTime)) }}
          </span>
        </div>
      </div>

      <!-- Warnings / Errors -->
      <div v-if="conflictWarning && !error" class="flex items-start gap-2 text-sm text-amber-700 bg-amber-50 rounded-xl p-3 border border-amber-200">
        <AppIcon icon="exclamation-circle" :size="16" class="shrink-0 mt-0.5 text-amber-500" />
        {{ conflictWarning }}
      </div>

      <div v-if="error" class="flex items-start gap-2 text-sm text-red-700 bg-red-50 rounded-xl p-3 border border-red-200">
        <AppIcon icon="x-mark" :size="16" class="shrink-0 mt-0.5 text-red-500" />
        {{ error }}
      </div>

      <!-- Actions -->
      <div class="flex gap-3 pt-2">
        <button
          type="submit"
          class="btn btn-primary flex-1"
          :disabled="submitting || loadingSlots"
        >
          <span v-if="submitting" class="inline-block w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
          <AppIcon v-else icon="check" :size="16" />
          {{ submitting ? 'Booking...' : 'Book Room' }}
        </button>
        <button type="button" class="btn btn-secondary" @click="emit('cancel')">
          Cancel
        </button>
      </div>
    </form>
  </div>
</template>
