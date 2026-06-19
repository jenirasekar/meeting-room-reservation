<script setup>
import { ref, onMounted } from 'vue'
import { useRoomsStore } from '../../stores/rooms'
import RoomCard from '../../components/RoomCard.vue'
import SkeletonLoader from '../../components/SkeletonLoader.vue'
import AppIcon from '../../components/AppIcon.vue'

const roomsStore = useRoomsStore()

const capacityFilter = ref('')
const statusFilter = ref('')
const searchQuery = ref('')

onMounted(() => {
  roomsStore.fetchRooms()
})

function applyFilters() {
  roomsStore.fetchRooms({
    capacity: capacityFilter.value || undefined,
    status: statusFilter.value || undefined
  })
}

function clearFilters() {
  capacityFilter.value = ''
  statusFilter.value = ''
  searchQuery.value = ''
  roomsStore.fetchRooms()
}

const filteredRooms = () => {
  if (!searchQuery.value.trim()) return roomsStore.rooms
  const q = searchQuery.value.toLowerCase()
  return roomsStore.rooms.filter(r =>
    r.name.toLowerCase().includes(q) ||
    (r.location && r.location.toLowerCase().includes(q)) ||
    (r.description && r.description.toLowerCase().includes(q))
  )
}
</script>

<template>
  <div>
    <!-- Hero section -->
    <div class="bg-gradient-to-br from-surface-900 via-surface-800 to-primary-950">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 sm:py-16">
        <div class="max-w-2xl">
          <h1 class="text-3xl sm:text-4xl font-extrabold text-white tracking-tight mb-3">
            Find Your Perfect
            <span class="gradient-text bg-gradient-to-r from-primary-400 to-accent-400">Meeting Room</span>
          </h1>
          <p class="text-surface-400 text-lg mb-6">
            Browse available rooms, check equipment, and book instantly.
          </p>
          <!-- Search bar -->
          <div class="relative max-w-md">
            <AppIcon icon="search" :size="18" class="absolute left-4 top-1/2 -translate-y-1/2 text-surface-400" />
            <input
              v-model="searchQuery"
              type="text"
              class="w-full pl-11 pr-4 py-3 rounded-xl bg-white/10 border border-white/10 text-white placeholder-surface-400 focus:outline-none focus:ring-2 focus:ring-primary-500/50 focus:border-primary-400/50 backdrop-blur-sm transition-all text-sm"
              placeholder="Search rooms by name, location..."
            />
          </div>
        </div>
      </div>
    </div>

    <!-- Content -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Stats row -->
      <div class="flex items-center gap-4 mb-6 text-sm text-surface-500">
        <div class="flex items-center gap-1.5">
          <AppIcon icon="building" :size="16" class="text-surface-400" />
          <span v-if="!roomsStore.loading">{{ roomsStore.rooms.length }} room(s)</span>
          <span v-else class="skeleton h-4 w-12 rounded" />
        </div>
      </div>

      <!-- Filters -->
      <div class="card mb-8">
        <div class="flex flex-col sm:flex-row gap-3 items-end">
          <div class="flex-1 w-full sm:w-auto">
            <label class="block text-xs font-medium text-surface-500 uppercase tracking-wider mb-1.5">Capacity</label>
            <select v-model="capacityFilter" class="input-field">
              <option value="">Any size</option>
              <option value="5">5+ people</option>
              <option value="10">10+ people</option>
              <option value="20">20+ people</option>
              <option value="30">30+ people</option>
            </select>
          </div>
          <div class="flex-1 w-full sm:w-auto">
            <label class="block text-xs font-medium text-surface-500 uppercase tracking-wider mb-1.5">Status</label>
            <select v-model="statusFilter" class="input-field">
              <option value="">All</option>
              <option value="available">Available</option>
              <option value="unavailable">Unavailable</option>
            </select>
          </div>
          <button @click="applyFilters" class="btn btn-primary">
            <AppIcon icon="filter" :size="16" />
            Apply
          </button>
          <button @click="clearFilters" class="btn btn-ghost text-sm">
            Clear filters
          </button>
        </div>
      </div>

      <!-- Loading skeleton -->
      <div v-if="roomsStore.loading" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <SkeletonLoader variant="card" :count="6" />
      </div>

      <!-- Empty state -->
      <div v-else-if="!roomsStore.rooms.length" class="text-center py-16">
        <div class="w-20 h-20 rounded-2xl bg-surface-100 flex items-center justify-center mx-auto mb-4">
          <AppIcon icon="building" :size="36" class="text-surface-300" />
        </div>
        <h3 class="text-lg font-semibold text-surface-700 mb-1">No rooms found</h3>
        <p class="text-surface-400 mb-4">Try adjusting your filters or check back later.</p>
        <button @click="clearFilters" class="btn btn-secondary">Clear Filters</button>
      </div>

      <!-- Search empty -->
      <div v-else-if="searchQuery && !filteredRooms().length" class="text-center py-16">
        <div class="w-20 h-20 rounded-2xl bg-surface-100 flex items-center justify-center mx-auto mb-4">
          <AppIcon icon="search" :size="36" class="text-surface-300" />
        </div>
        <h3 class="text-lg font-semibold text-surface-700 mb-1">No matching rooms</h3>
        <p class="text-surface-400">Try a different search term.</p>
      </div>

      <!-- Room grid -->
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        <RoomCard
          v-for="(room, idx) in filteredRooms()"
          :key="room.id"
          :room="room"
          :delay="idx * 0.05"
        />
      </div>
    </div>
  </div>
</template>
