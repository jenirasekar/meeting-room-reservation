<script setup>
import { useToastStore } from '../stores/toast'
import AppIcon from './AppIcon.vue'

const toast = useToastStore()

const iconMap = {
  success: 'check-circle',
  error: 'x-mark',
  info: 'information-circle',
  warning: 'exclamation-circle',
}

const colorMap = {
  success: 'bg-emerald-50 border-emerald-200 text-emerald-800',
  error: 'bg-red-50 border-red-200 text-red-800',
  info: 'bg-blue-50 border-blue-200 text-blue-800',
  warning: 'bg-amber-50 border-amber-200 text-amber-800',
}

const iconColorMap = {
  success: 'text-emerald-500',
  error: 'text-red-500',
  info: 'text-blue-500',
  warning: 'text-amber-500',
}
</script>

<template>
  <Teleport to="body">
    <div class="fixed top-4 right-4 z-[100] flex flex-col gap-2 pointer-events-none">
      <div
        v-for="t in toast.toasts"
        :key="t.id"
        :class="[
          'pointer-events-auto flex items-center gap-3 px-4 py-3 rounded-xl border shadow-soft-lg',
          'animate-slide-in-right max-w-sm',
          colorMap[t.type]
        ]"
      >
        <AppIcon :name="iconMap[t.type]" :size="20" :class="iconColorMap[t.type]" />
        <span class="text-sm font-medium flex-1">{{ t.message }}</span>
        <button
          @click="toast.removeToast(t.id)"
          class="shrink-0 opacity-60 hover:opacity-100 transition-opacity"
        >
          <AppIcon icon="x-mark" :size="14" />
        </button>
      </div>
    </div>
  </Teleport>
</template>
