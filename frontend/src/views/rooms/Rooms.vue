<script setup>
import { ref, onMounted } from 'vue'
import { useRoomsStore } from '../../stores/rooms'
import RoomCard from '../../components/RoomCard.vue'

const roomsStore = useRoomsStore()

const capacityFilter = ref('')
const statusFilter = ref('')

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
  roomsStore.fetchRooms()
}
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-8">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Meeting Rooms</h1>
        <p class="text-gray-500 mt-1">Browse and book available rooms</p>
      </div>
    </div>

    <!-- Filters -->
    <div class="card mb-6">
      <div class="flex flex-col sm:flex-row gap-4 items-end">
        <div class="flex-1">
          <label class="block text-sm font-medium text-gray-700 mb-1">Min Capacity</label>
          <select v-model="capacityFilter" class="input-field">
            <option value="">Any</option>
            <option value="5">5+ people</option>
            <option value="10">10+ people</option>
            <option value="20">20+ people</option>
            <option value="30">30+ people</option>
          </select>
        </div>
        <div class="flex-1">
          <label class="block text-sm font-medium text-gray-700 mb-1">Status</label>
          <select v-model="statusFilter" class="input-field">
            <option value="">All</option>
            <option value="available">Available</option>
            <option value="unavailable">Unavailable</option>
          </select>
        </div>
        <button @click="applyFilters" class="btn-primary">Apply Filters</button>
        <button @click="clearFilters" class="btn-secondary">Clear</button>
      </div>
    </div>

    <!-- Room grid -->
    <div v-if="roomsStore.loading" class="text-center py-12">
      <p class="text-gray-500">Loading rooms...</p>
    </div>

    <div v-else-if="!roomsStore.rooms.length" class="text-center py-12">
      <p class="text-gray-500 text-lg">No rooms found</p>
      <p class="text-gray-400 mt-1">Try adjusting your filters</p>
    </div>

    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <RoomCard
        v-for="room in roomsStore.rooms"
        :key="room.id"
        :room="room"
      />
    </div>
  </div>
</template>
