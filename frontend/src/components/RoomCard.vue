<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  room: {
    type: Object,
    required: true
  }
})

const router = useRouter()

function viewDetail() {
  router.push(`/rooms/${props.room.id}`)
}
</script>

<template>
  <div
    class="card hover:shadow-md transition-shadow cursor-pointer group"
    @click="viewDetail"
  >
    <!-- Image placeholder -->
    <div class="w-full h-48 bg-gradient-to-br from-primary-400 to-primary-600 rounded-lg mb-4 flex items-center justify-center overflow-hidden">
      <img
        v-if="room.imageUrl"
        :src="room.imageUrl"
        :alt="room.name"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
      />
      <span v-else class="text-5xl text-white/70">🏢</span>
    </div>

    <!-- Info -->
    <div class="space-y-2">
      <div class="flex items-start justify-between">
        <h3 class="text-lg font-semibold text-gray-900 group-hover:text-primary-600 transition-colors">
          {{ room.name }}
        </h3>
        <span
          :class="[
            'badge text-xs',
            room.status === 'available' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
          ]"
        >
          {{ room.status }}
        </span>
      </div>

      <div class="flex items-center gap-4 text-sm text-gray-500">
        <span class="flex items-center gap-1">
          <span>📍</span> {{ room.location || 'N/A' }}
        </span>
        <span class="flex items-center gap-1">
          <span>👥</span> {{ room.capacity }} people
        </span>
      </div>

      <p class="text-sm text-gray-600 line-clamp-2">
        {{ room.description || 'No description' }}
      </p>
    </div>
  </div>
</template>
