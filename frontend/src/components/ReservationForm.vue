<script setup>
import { ref, watch } from 'vue'
import { useReservationsStore } from '../stores/reservations'
import { useRoomsStore } from '../stores/rooms'

const props = defineProps({
  roomId: { type: Number, required: true }
})

const emit = defineEmits(['success', 'cancel'])

const reservationsStore = useReservationsStore()
const roomsStore = useRoomsStore()

const title = ref('')
const date = ref('')
const startTime = ref('09:00')
const endTime = ref('10:00')
const submitting = ref(false)
const error = ref('')
const conflictWarning = ref('')
const loadingSlots = ref(false)   // local loading — doesn't affect parent page
const bookedSlots = ref([])       // local slots — doesn't affect parent page

// Watch for date changes → AJAX fetch availability
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

// Bug 8 fix: parse LocalDateTime string ("2026-06-15T09:00:00") safely as local time
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

  const selectedStart = new Date(`${date.value}T${startTime.value}:00`)
  const selectedEnd = new Date(`${date.value}T${endTime.value}:00`)

  for (const slot of bookedSlots.value) {
    const slotStart = parseLocalDateTime(slot.startTime)
    const slotEnd = parseLocalDateTime(slot.endTime)
    if (!slotStart || !slotEnd) continue
    if (selectedStart < slotEnd && selectedEnd > slotStart) {
      conflictWarning.value = `⚠️ This time conflicts with "${slot.title}" (${formatTime(slotStart)} - ${formatTime(slotEnd)})`
      return true
    }
  }
  conflictWarning.value = ''
  return false
}

async function handleSubmit() {
  error.value = ''

  if (!title.value.trim()) { error.value = 'Title is required'; return }
  if (!date.value) { error.value = 'Date is required'; return }
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
    <h3 class="text-lg font-semibold mb-4">Book This Room</h3>

    <form @submit.prevent="handleSubmit" class="space-y-4">
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Meeting Title</label>
        <input v-model="title" type="text" class="input-field" placeholder="e.g., Sprint Planning" required />
      </div>

      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Date</label>
        <input v-model="date" type="date" class="input-field" :min="getToday()" required />
        <p v-if="loadingSlots" class="text-xs text-gray-400 mt-1">Checking availability...</p>
        <p v-else-if="date && bookedSlots.length" class="text-xs text-gray-500 mt-1">
          {{ bookedSlots.length }} booking(s) on this date
        </p>
        <p v-else-if="date && !loadingSlots" class="text-xs text-green-600 mt-1">
          No bookings on this date
        </p>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Start Time</label>
          <input v-model="startTime" type="time" class="input-field" required />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">End Time</label>
          <input v-model="endTime" type="time" class="input-field" required />
        </div>
      </div>

      <!-- Booked slots display -->
      <div v-if="bookedSlots.length" class="bg-gray-50 rounded-lg p-3">
        <p class="text-xs font-medium text-gray-600 mb-2">Booked slots on this date:</p>
        <div
          v-for="slot in bookedSlots"
          :key="slot.id"
          class="flex items-center justify-between text-xs text-gray-500 py-1"
        >
          <span>{{ slot.title }}</span>
          <span>
            {{ formatTime(parseLocalDateTime(slot.startTime)) }} -
            {{ formatTime(parseLocalDateTime(slot.endTime)) }}
          </span>
        </div>
      </div>

      <div v-if="conflictWarning && !error" class="text-sm text-yellow-700 bg-yellow-50 rounded-lg p-3">
        {{ conflictWarning }}
      </div>

      <div v-if="error" class="text-sm text-red-600 bg-red-50 rounded-lg p-3">{{ error }}</div>

      <div class="flex gap-3 pt-2">
        <button type="submit" class="btn-primary flex-1" :disabled="submitting || loadingSlots">
          {{ submitting ? 'Booking...' : 'Book Room' }}
        </button>
        <button type="button" class="btn-secondary" @click="emit('cancel')">Cancel</button>
      </div>
    </form>
  </div>
</template>
