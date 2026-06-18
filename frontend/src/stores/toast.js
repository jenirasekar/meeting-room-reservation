import { defineStore } from 'pinia'
import { ref } from 'vue'

let nextId = 0

export const useToastStore = defineStore('toast', () => {
  const toasts = ref([])

  function addToast(message, type = 'info', duration = 4000) {
    const id = ++nextId
    toasts.value.push({ id, message, type, duration })
    if (duration > 0) {
      setTimeout(() => removeToast(id), duration)
    }
    return id
  }

  function removeToast(id) {
    const idx = toasts.value.findIndex(t => t.id === id)
    if (idx !== -1) toasts.value.splice(idx, 1)
  }

  function success(message, duration) { return addToast(message, 'success', duration) }
  function error(message, duration) { return addToast(message, 'error', duration ?? 6000) }
  function info(message, duration) { return addToast(message, 'info', duration) }
  function warning(message, duration) { return addToast(message, 'warning', duration ?? 5000) }

  return { toasts, addToast, removeToast, success, error, info, warning }
})
