<script setup>
import { useRouter } from 'vue-router'
import AppIcon from './AppIcon.vue'

const props = defineProps({
  room: { type: Object, required: true },
  delay: { type: Number, default: 0 },
})

const router = useRouter()

function viewDetail() {
  router.push(`/rooms/${props.room.id}`)
}
</script>

<template>
  <div
    class="card-hover group overflow-hidden"
    :style="{ animationDelay: delay + 's' }"
    :class="delay ? 'stagger-item' : ''"
    @click="viewDetail"
  >
    <!-- Image -->
    <div class="relative w-full h-48 rounded-xl mb-4 overflow-hidden bg-gradient-to-br from-primary-400 via-primary-500 to-accent-500">
      <img
        v-if="room.imageUrl"
        :src="room.imageUrl"
        :alt="room.name"
        class="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
      />
      <div v-else class="w-full h-full flex flex-col items-center justify-center">
        <AppIcon icon="building" :size="48" class="text-white/60" />
        <span class="text-white/50 text-xs mt-2 font-medium">No Image</span>
      </div>
      <!-- Hover overlay -->
      <div class="absolute inset-0 bg-gradient-to-t from-black/40 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-end p-4">
        <span class="text-white text-sm font-medium">View Details →</span>
      </div>
    </div>

    <!-- Info -->
    <div class="space-y-2.5">
      <div class="flex items-start justify-between gap-2">
        <h3 class="text-lg font-semibold text-surface-900 group-hover:text-primary-600 transition-colors leading-snug">
          {{ room.name }}
        </h3>
        <span
          :class="[
            'badge shrink-0 text-[11px]',
            room.status === 'available'
              ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
              : 'bg-red-50 text-red-700 border-red-200'
          ]"
        >
          <span :class="room.status === 'available' ? 'status-dot-available' : 'status-dot-unavailable'" />
          {{ room.status }}
        </span>
      </div>

      <div class="flex items-center gap-4 text-sm text-surface-500">
        <span class="flex items-center gap-1.5">
          <AppIcon icon="location-marker" :size="14" class="text-surface-400" />
          {{ room.location || 'N/A' }}
        </span>
        <span class="flex items-center gap-1.5">
          <AppIcon icon="users" :size="14" class="text-surface-400" />
          {{ room.capacity }}
        </span>
      </div>

      <p class="text-sm text-surface-400 line-clamp-2 leading-relaxed">
        {{ room.description || 'No description available for this room.' }}
      </p>
    </div>
  </div>
</template>
