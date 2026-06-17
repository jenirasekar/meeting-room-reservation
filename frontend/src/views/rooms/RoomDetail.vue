<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useRoomsStore } from '../../stores/rooms'
import { useAuthStore } from '../../stores/auth'
import ReservationForm from '../../components/ReservationForm.vue'

const route = useRoute()
const router = useRouter()
const roomsStore = useRoomsStore()
const auth = useAuthStore()

const roomId = Number(route.params.id)
const showBookingForm = ref(false)

onMounted(async () => {
  await roomsStore.fetchRoom(roomId)
})

function onBooked() {
  showBookingForm.value = false
  alert('Reservation submitted! Waiting for admin approval.')
  router.push('/my-reservations')
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <!-- Loading -->
    <div v-if="roomsStore.loading" class="text-center py-12">
      <p class="text-gray-500">Loading room details...</p>
    </div>

    <!-- Not found -->
    <div v-else-if="!roomsStore.currentRoom" class="text-center py-12">
      <p class="text-gray-500 text-lg">Room not found</p>
      <router-link to="/rooms" class="text-primary-600 hover:underline mt-2 inline-block">
        Back to rooms
      </router-link>
    </div>

    <!-- Room detail -->
    <template v-else>
      <div class="flex items-center gap-2 mb-6">
        <button @click="router.back()" class="text-gray-500 hover:text-gray-700">&larr; Back</button>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <!-- Room info -->
        <div class="lg:col-span-2 space-y-6">
          <!-- Image -->
          <div class="w-full h-64 bg-gradient-to-br from-primary-400 to-primary-600 rounded-xl flex items-center justify-center">
            <img
              v-if="roomsStore.currentRoom.imageUrl"
              :src="roomsStore.currentRoom.imageUrl"
              :alt="roomsStore.currentRoom.name"
              class="w-full h-full object-cover rounded-xl"
            />
            <span v-else class="text-6xl text-white/70">🏢</span>
          </div>

          <!-- Details -->
          <div class="card">
            <div class="flex items-start justify-between mb-4">
              <h1 class="text-2xl font-bold text-gray-900">{{ roomsStore.currentRoom.name }}</h1>
              <span
                :class="[
                  'badge',
                  roomsStore.currentRoom.status === 'available'
                    ? 'bg-green-100 text-green-700'
                    : 'bg-red-100 text-red-700'
                ]"
              >
                {{ roomsStore.currentRoom.status }}
              </span>
            </div>

            <div class="grid grid-cols-2 gap-4 mb-6">
              <div class="flex items-center gap-2 text-gray-600">
                <span>📍</span> {{ roomsStore.currentRoom.location || 'N/A' }}
              </div>
              <div class="flex items-center gap-2 text-gray-600">
                <span>👥</span> Capacity: {{ roomsStore.currentRoom.capacity }} people
              </div>
            </div>

            <div class="prose prose-sm max-w-none text-gray-600">
              <h3 class="text-sm font-medium text-gray-900 mb-2">Description</h3>
              <p>{{ roomsStore.currentRoom.description || 'No description provided.' }}</p>
            </div>
          </div>

          <!-- Equipment -->
          <div v-if="roomsStore.equipment.length" class="card">
            <h3 class="text-lg font-semibold mb-4">Equipment</h3>
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div
                v-for="item in roomsStore.equipment"
                :key="item.id"
                class="flex items-center justify-between bg-gray-50 rounded-lg p-3"
              >
                <span class="text-gray-700">{{ item.name }}</span>
                <span class="text-sm text-gray-500">x{{ item.quantity }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Booking sidebar -->
        <div class="lg:col-span-1">
          <div v-if="showBookingForm">
            <ReservationForm
              :roomId="roomId"
              @success="onBooked"
              @cancel="showBookingForm = false"
            />
          </div>
          <div v-else class="card text-center">
            <p class="text-gray-500 mb-4">Ready to book this room?</p>
            <button
              @click="showBookingForm = true"
              class="btn-primary w-full"
              :disabled="roomsStore.currentRoom.status !== 'available'"
            >
              {{ roomsStore.currentRoom.status === 'available' ? 'Book Now' : 'Not Available' }}
            </button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
