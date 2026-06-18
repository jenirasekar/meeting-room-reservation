<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useRoomsStore } from '../../stores/rooms'
import { useAuthStore } from '../../stores/auth'
import ReservationForm from '../../components/ReservationForm.vue'
import SkeletonLoader from '../../components/SkeletonLoader.vue'
import AppIcon from '../../components/AppIcon.vue'
import { useToastStore } from '../../stores/toast'

const route = useRoute()
const router = useRouter()
const roomsStore = useRoomsStore()
const auth = useAuthStore()
const toast = useToastStore()

const roomId = Number(route.params.id)
const showBookingForm = ref(false)

onMounted(async () => {
  await roomsStore.fetchRoom(roomId)
})

function onBooked() {
  showBookingForm.value = false
  router.push('/my-reservations')
}
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <!-- Loading -->
    <div v-if="roomsStore.loading" class="space-y-6">
      <SkeletonLoader variant="text" :count="1" />
      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div class="lg:col-span-2 space-y-6">
          <div class="skeleton h-64 w-full rounded-2xl" />
          <SkeletonLoader variant="paragraph" :count="4" />
        </div>
        <div><SkeletonLoader variant="card" :count="1" /></div>
      </div>
    </div>

    <!-- Not found -->
    <div v-else-if="!roomsStore.currentRoom" class="text-center py-20">
      <div class="w-20 h-20 rounded-2xl bg-surface-100 flex items-center justify-center mx-auto mb-4">
        <AppIcon icon="exclamation-circle" :size="36" class="text-surface-300" />
      </div>
      <h2 class="text-xl font-semibold text-surface-700 mb-2">Room Not Found</h2>
      <p class="text-surface-400 mb-4">This room may have been removed or doesn't exist.</p>
      <router-link to="/rooms" class="btn-primary">
        <AppIcon icon="arrow-left" :size="16" />
        Back to Rooms
      </router-link>
    </div>

    <!-- Room detail -->
    <template v-else>
      <!-- Breadcrumb -->
      <nav class="flex items-center gap-2 text-sm mb-6">
        <router-link to="/rooms" class="text-surface-400 hover:text-surface-600 transition-colors">Rooms</router-link>
        <AppIcon icon="chevron-right" :size="12" class="text-surface-300" />
        <span class="text-surface-700 font-medium truncate">{{ roomsStore.currentRoom.name }}</span>
      </nav>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Main content -->
        <div class="lg:col-span-2 space-y-6">
          <!-- Image banner -->
          <div class="relative w-full h-72 sm:h-96 rounded-2xl overflow-hidden bg-gradient-to-br from-primary-400 via-primary-500 to-accent-500">
            <img
              v-if="roomsStore.currentRoom.imageUrl"
              :src="roomsStore.currentRoom.imageUrl"
              :alt="roomsStore.currentRoom.name"
              class="w-full h-full object-cover"
            />
            <div v-else class="w-full h-full flex flex-col items-center justify-center">
              <AppIcon icon="building" :size="72" class="text-white/40" />
              <span class="text-white/40 text-sm mt-3 font-medium">No image available</span>
            </div>
            <!-- Status badge overlay -->
            <div class="absolute top-4 right-4">
              <span
                :class="[
                  'badge text-xs backdrop-blur-md',
                  roomsStore.currentRoom.status === 'available'
                    ? 'bg-emerald-500/20 text-emerald-100 border-emerald-400/30'
                    : 'bg-red-500/20 text-red-100 border-red-400/30'
                ]"
              >
                <span :class="roomsStore.currentRoom.status === 'available' ? 'status-dot-available' : 'status-dot-unavailable'" />
                {{ roomsStore.currentRoom.status }}
              </span>
            </div>
          </div>

          <!-- Room details card -->
          <div class="card">
            <h1 class="text-2xl sm:text-3xl font-bold text-surface-900 mb-4">
              {{ roomsStore.currentRoom.name }}
            </h1>

            <div class="flex flex-wrap gap-4 mb-6">
              <div class="flex items-center gap-2 px-4 py-2 bg-surface-50 rounded-xl text-sm">
                <AppIcon icon="location-marker" :size="16" class="text-primary-500" />
                <span class="text-surface-600">{{ roomsStore.currentRoom.location || 'N/A' }}</span>
              </div>
              <div class="flex items-center gap-2 px-4 py-2 bg-surface-50 rounded-xl text-sm">
                <AppIcon icon="users" :size="16" class="text-primary-500" />
                <span class="text-surface-600">{{ roomsStore.currentRoom.capacity }} people</span>
              </div>
            </div>

            <div>
              <h3 class="text-sm font-semibold text-surface-500 uppercase tracking-wider mb-2">Description</h3>
              <p class="text-surface-600 leading-relaxed">
                {{ roomsStore.currentRoom.description || 'No description provided for this room.' }}
              </p>
            </div>
          </div>

          <!-- Equipment -->
          <div class="card">
            <div class="flex items-center gap-2 mb-4">
              <div class="w-8 h-8 rounded-lg bg-primary-50 flex items-center justify-center">
                <AppIcon icon="check-circle" :size="16" class="text-primary-600" />
              </div>
              <h3 class="text-lg font-semibold text-surface-900">Equipment</h3>
            </div>

            <div v-if="roomsStore.equipment.length" class="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div
                v-for="item in roomsStore.equipment"
                :key="item.id"
                class="flex items-center justify-between bg-surface-50 rounded-xl p-4 border border-surface-100"
              >
                <div class="flex items-center gap-3">
                  <div class="w-8 h-8 rounded-lg bg-white flex items-center justify-center shadow-sm">
                    <AppIcon icon="check" :size="14" class="text-emerald-500" />
                  </div>
                  <span class="text-surface-700 font-medium text-sm">{{ item.name }}</span>
                </div>
                <span class="text-xs text-surface-400 bg-surface-200 px-2 py-0.5 rounded-full">
                  x{{ item.quantity }}
                </span>
              </div>
            </div>
            <div v-else class="text-center py-6">
              <AppIcon icon="information-circle" :size="24" class="text-surface-300 mx-auto mb-2" />
              <p class="text-sm text-surface-400">No equipment listed for this room.</p>
            </div>
          </div>
        </div>

        <!-- Booking sidebar -->
        <div class="lg:col-span-1">
          <div class="lg:sticky lg:top-24">
            <div v-if="showBookingForm">
              <ReservationForm
                :roomId="roomId"
                @success="onBooked"
                @cancel="showBookingForm = false"
              />
            </div>
            <div v-else class="card text-center">
              <div class="w-14 h-14 rounded-2xl bg-primary-50 flex items-center justify-center mx-auto mb-4">
                <AppIcon icon="calendar" :size="28" class="text-primary-500" />
              </div>
              <h3 class="text-lg font-semibold text-surface-900 mb-1">Ready to book?</h3>
              <p class="text-sm text-surface-500 mb-5">
                {{ roomsStore.currentRoom.status === 'available'
                  ? 'This room is available for booking.'
                  : 'This room is currently unavailable.' }}
              </p>
              <button
                @click="showBookingForm = true"
                class="btn-primary w-full btn-lg"
                :disabled="roomsStore.currentRoom.status !== 'available'"
              >
                <AppIcon icon="calendar" :size="18" />
                {{ roomsStore.currentRoom.status === 'available' ? 'Book Now' : 'Not Available' }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>
