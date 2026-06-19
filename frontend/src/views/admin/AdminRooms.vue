<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoomsStore } from '../../stores/rooms'
import { equipmentAPI, uploadAPI } from '../../api'
import { useToastStore } from '../../stores/toast'
import AppIcon from '../../components/AppIcon.vue'

const roomsStore = useRoomsStore()
const toast = useToastStore()

const searchQuery = ref('')
const filteredRooms = computed(() => {
  if (!searchQuery.value.trim()) return roomsStore.rooms
  const q = searchQuery.value.toLowerCase()
  return roomsStore.rooms.filter(r =>
    r.name.toLowerCase().includes(q) ||
    (r.location && r.location.toLowerCase().includes(q))
  )
})

const showForm = ref(false)
const editingRoom = ref(null)
const form = ref({
  name: '', location: '', capacity: 10, description: '', status: 'available', imageUrl: ''
})
const formError = ref('')
const formSubmitting = ref(false)
const uploading = ref(false)
const fileInput = ref(null)

// Equipment management
const showEquipForm = ref(false)
const equipRoomId = ref(0)
const equipRoomName = ref('')
const equipForm = ref({ name: '', quantity: 1 })

onMounted(() => {
  roomsStore.fetchRooms()
})

function resetForm() {
  form.value = { name: '', location: '', capacity: 10, description: '', status: 'available', imageUrl: '' }
  formError.value = ''
  uploading.value = false
  if (fileInput.value) fileInput.value.value = ''
}

function openCreateForm() {
  editingRoom.value = null
  resetForm()
  showForm.value = true
}

function openEditForm(room) {
  editingRoom.value = room
  resetForm()
  form.value = {
    name: room.name,
    location: room.location || '',
    capacity: room.capacity,
    description: room.description || '',
    status: room.status,
    imageUrl: room.imageUrl || ''
  }
  showForm.value = true
}

async function handleFileSelect(e) {
  const file = e.target.files?.[0]
  if (!file) return

  // Validate
  if (!file.type.startsWith('image/')) {
    formError.value = 'Please select an image file (JPEG, PNG, GIF, WebP)'
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    formError.value = 'Image must be under 5 MB'
    return
  }

  formError.value = ''
  try {
    uploading.value = true
    const { data } = await uploadAPI.roomImage(file)
    if (data.success) {
      form.value.imageUrl = data.data // data.data is the URL string
      toast.success('Photo uploaded!')
    } else {
      formError.value = data.message || 'Upload failed'
    }
  } catch (err) {
    formError.value = err.response?.data?.message || 'Upload failed'
  } finally {
    uploading.value = false
  }
}

function clearImage() {
  form.value.imageUrl = ''
  if (fileInput.value) fileInput.value.value = ''
}

async function handleSave() {
  formError.value = ''
  if (!form.value.name.trim()) {
    formError.value = 'Room name is required'
    return
  }

  try {
    formSubmitting.value = true
    let result
    if (editingRoom.value) {
      result = await roomsStore.updateRoom(editingRoom.value.id, form.value)
    } else {
      result = await roomsStore.createRoom(form.value)
    }
    if (result.success) {
      showForm.value = false
      toast.success(editingRoom.value ? 'Room updated!' : 'Room created!')
      await roomsStore.fetchRooms()
    } else {
      formError.value = result.message || 'Failed to save'
    }
  } catch (e) {
    formError.value = e.response?.data?.message || 'Failed to save'
  } finally {
    formSubmitting.value = false
  }
}

async function handleDelete(room) {
  if (!confirm(`Delete room "${room.name}"? This cannot be undone.`)) return
  const result = await roomsStore.deleteRoom(room.id)
  if (result.success) {
    toast.success('Room deleted')
  } else {
    toast.error(result.message || 'Failed to delete')
  }
}

function openEquipManager(room) {
  equipRoomId.value = room.id
  equipRoomName.value = room.name
  showEquipForm.value = true
  roomsStore.fetchRoom(room.id)
}

async function addEquipment() {
  if (!equipForm.value.name.trim()) return
  await equipmentAPI.add(equipRoomId.value, equipForm.value)
  equipForm.value = { name: '', quantity: 1 }
  toast.success('Equipment added')
  await roomsStore.fetchRoom(equipRoomId.value)
}

async function deleteEquipment(id) {
  if (!confirm('Remove this equipment?')) return
  await equipmentAPI.delete(id)
  toast.info('Equipment removed')
  await roomsStore.fetchRoom(equipRoomId.value)
}
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="flex items-center justify-between mb-8">
      <div>
        <h1 class="text-2xl font-bold text-surface-900">Manage Rooms</h1>
        <p class="text-surface-500 mt-1">Add, edit, or remove meeting rooms</p>
      </div>
      <button @click="openCreateForm" class="btn btn-primary">
        <AppIcon icon="plus" :size="18" />
        Add Room
      </button>
    </div>

    <!-- Loading -->
    <div v-if="roomsStore.loading" class="text-center py-12">
      <div class="inline-block w-8 h-8 border-2 border-surface-300 border-t-primary-500 rounded-full animate-spin" />
      <p class="text-surface-500 mt-3">Loading rooms...</p>
    </div>

    <!-- Empty state -->
    <div v-else-if="!roomsStore.rooms.length" class="text-center py-16">
      <div class="w-20 h-20 rounded-2xl bg-surface-100 flex items-center justify-center mx-auto mb-4">
        <AppIcon icon="building" :size="36" class="text-surface-300" />
      </div>
      <h3 class="text-lg font-semibold text-surface-700 mb-1">No rooms yet</h3>
      <p class="text-surface-400 mb-4">Create your first meeting room to get started.</p>
      <button @click="openCreateForm" class="btn btn-primary">
        <AppIcon icon="plus" :size="16" />
        Add Room
      </button>
    </div>

    <!-- Room list -->
    <div v-else class="space-y-3">
      <div
        v-for="room in roomsStore.rooms"
        :key="room.id"
        class="card hover:shadow-soft-lg transition-all"
      >
        <div class="flex flex-col sm:flex-row items-start justify-between gap-4">
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-3 mb-2">
              <h3 class="text-lg font-semibold text-surface-900">{{ room.name }}</h3>
              <span
                :class="[
                  'badge text-[11px]',
                  room.status === 'available'
                    ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                    : 'bg-red-50 text-red-700 border-red-200'
                ]"
              >
                <span :class="room.status === 'available' ? 'status-dot-available' : 'status-dot-unavailable'" />
                {{ room.status }}
              </span>
            </div>
            <p class="text-sm text-surface-500 flex items-center gap-1.5">
              <AppIcon icon="location-marker" :size="13" class="text-surface-400" />
              {{ room.location || 'No location' }}
              <span class="text-surface-300 mx-1">·</span>
              <AppIcon icon="users" :size="13" class="text-surface-400" />
              {{ room.capacity }} people
            </p>
            <p v-if="room.description" class="text-sm text-surface-400 mt-1 line-clamp-1">
              {{ room.description }}
            </p>
          </div>
          <div class="flex gap-2 shrink-0">
            <button @click="openEquipManager(room)" class="btn btn-ghost btn-sm">
              <AppIcon icon="settings" :size="14" />
              <span class="hidden sm:inline">Equipment</span>
            </button>
            <button @click="openEditForm(room)" class="btn btn-secondary btn-sm">
              <AppIcon icon="pencil" :size="14" />
              <span class="hidden sm:inline">Edit</span>
            </button>
            <button @click="handleDelete(room)" class="btn btn-danger btn-sm">
              <AppIcon icon="trash" :size="14" />
              <span class="hidden sm:inline">Delete</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Room create/edit modal -->
    <Teleport to="body">
      <Transition name="modal-backdrop">
        <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center p-4" @click.self="showForm = false">
          <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" />
          <Transition name="modal-content">
            <div v-if="showForm" class="relative bg-white rounded-2xl shadow-soft-xl w-full max-w-lg p-6 max-h-[90vh] overflow-y-auto">
              <div class="flex items-center justify-between mb-5">
                <h2 class="text-xl font-semibold text-surface-900">
                  {{ editingRoom ? 'Edit Room' : 'Add Room' }}
                </h2>
                <button @click="showForm = false" class="p-1.5 rounded-lg hover:bg-surface-100 transition-colors">
                  <AppIcon icon="x-mark" :size="18" class="text-surface-400" />
                </button>
              </div>

              <form @submit.prevent="handleSave" class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-surface-700 mb-1.5">Name <span class="text-red-400">*</span></label>
                  <input v-model="form.name" type="text" class="input-field" required />
                </div>
                <div>
                  <label class="block text-sm font-medium text-surface-700 mb-1.5">Location</label>
                  <input v-model="form.location" type="text" class="input-field" placeholder="e.g., Floor 3, Building A" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-surface-700 mb-1.5">Capacity</label>
                  <input v-model.number="form.capacity" type="number" min="1" class="input-field" />
                </div>
                <div>
                  <label class="block text-sm font-medium text-surface-700 mb-1.5">Description</label>
                  <textarea v-model="form.description" class="input-field" rows="3" placeholder="Brief description of the room..."></textarea>
                </div>
                <div>
                  <label class="block text-sm font-medium text-surface-700 mb-1.5">Status</label>
                  <select v-model="form.status" class="input-field">
                    <option value="available">Available</option>
                    <option value="unavailable">Unavailable</option>
                  </select>
                </div>
                <!-- Room photo upload -->
                <div>
                  <label class="block text-sm font-medium text-surface-700 mb-1.5">Room Photo</label>

                  <!-- Preview -->
                  <div v-if="form.imageUrl" class="relative mb-3 rounded-xl overflow-hidden border border-surface-200 bg-surface-100">
                    <img
                      :src="form.imageUrl"
                      alt="Room preview"
                      class="w-full h-48 object-cover"
                      @error="$event.target.style.display = 'none'"
                    />
                    <button
                      type="button"
                      @click="clearImage"
                      class="absolute top-2 right-2 w-7 h-7 rounded-full bg-black/50 hover:bg-black/70 text-white flex items-center justify-center transition-colors"
                      title="Remove photo"
                    >
                      <AppIcon icon="x-mark" :size="14" />
                    </button>
                  </div>

                  <!-- Upload area -->
                  <div class="flex items-center gap-3">
                    <label
                      :class="[
                        'btn btn-secondary btn-sm cursor-pointer',
                        { 'pointer-events-none opacity-50': uploading }
                      ]"
                    >
                      <span v-if="uploading" class="inline-block w-3.5 h-3.5 border-2 border-surface-400 border-t-primary-500 rounded-full animate-spin" />
                      <AppIcon v-else icon="upload" :size="14" />
                      {{ uploading ? 'Uploading...' : (form.imageUrl ? 'Change Photo' : 'Upload Photo') }}
                      <input
                        ref="fileInput"
                        type="file"
                        accept="image/jpeg,image/png,image/gif,image/webp"
                        class="hidden"
                        @change="handleFileSelect"
                      />
                    </label>
                    <span class="text-xs text-surface-400">JPEG, PNG, GIF or WebP (max 5 MB)</span>
                  </div>

                  <!-- URL fallback -->
                  <details class="mt-3" :open="false">
                    <summary class="text-xs text-surface-400 cursor-pointer hover:text-surface-600 select-none">Or paste an image URL</summary>
                    <input
                      v-model="form.imageUrl"
                      type="url"
                      class="input-field mt-2"
                      placeholder="https://example.com/room.jpg"
                    />
                  </details>
                </div>

                <div v-if="formError" class="flex items-start gap-2 text-sm text-red-700 bg-red-50 rounded-xl p-3 border border-red-200">
                  <AppIcon icon="exclamation-circle" :size="16" class="shrink-0 mt-0.5" />
                  {{ formError }}
                </div>

                <div class="flex gap-3 pt-2">
                  <button type="submit" class="btn btn-primary flex-1" :disabled="formSubmitting">
                    <span v-if="formSubmitting" class="inline-block w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    {{ formSubmitting ? 'Saving...' : 'Save' }}
                  </button>
                  <button type="button" class="btn btn-secondary" @click="showForm = false">Cancel</button>
                </div>
              </form>
            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>

    <!-- Equipment manager modal -->
    <Teleport to="body">
      <Transition name="modal-backdrop">
        <div v-if="showEquipForm" class="fixed inset-0 z-50 flex items-center justify-center p-4" @click.self="showEquipForm = false">
          <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" />
          <Transition name="modal-content">
            <div v-if="showEquipForm" class="relative bg-white rounded-2xl shadow-soft-xl w-full max-w-md p-6">
              <div class="flex items-center justify-between mb-5">
                <h2 class="text-xl font-semibold text-surface-900">Equipment — {{ equipRoomName }}</h2>
                <button @click="showEquipForm = false" class="p-1.5 rounded-lg hover:bg-surface-100 transition-colors">
                  <AppIcon icon="x-mark" :size="18" class="text-surface-400" />
                </button>
              </div>

              <!-- Current equipment -->
              <div v-if="roomsStore.equipment.length" class="space-y-2 mb-4">
                <div
                  v-for="item in roomsStore.equipment"
                  :key="item.id"
                  class="flex items-center justify-between bg-surface-50 rounded-xl p-3.5 border border-surface-100"
                >
                  <div class="flex items-center gap-2.5">
                    <AppIcon icon="check" :size="14" class="text-emerald-500" />
                    <span class="font-medium text-surface-700 text-sm">{{ item.name }}</span>
                    <span class="text-xs text-surface-400 bg-surface-200 px-2 py-0.5 rounded-full">x{{ item.quantity }}</span>
                  </div>
                  <button @click="deleteEquipment(item.id)" class="text-red-500 hover:text-red-700 text-sm font-medium transition-colors">
                    Remove
                  </button>
                </div>
              </div>
              <p v-else class="text-surface-400 text-sm mb-4 text-center py-4">No equipment added yet</p>

              <!-- Add form -->
              <div class="flex gap-2">
                <input v-model="equipForm.name" type="text" class="input-field flex-1" placeholder="Item name" />
                <input v-model.number="equipForm.quantity" type="number" min="1" class="input-field w-20" />
                <button @click="addEquipment" class="btn btn-primary btn-sm">
                  <AppIcon icon="plus" :size="16" />
                </button>
              </div>

              <button @click="showEquipForm = false" class="btn btn-secondary w-full mt-4">Done</button>
            </div>
          </Transition>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>
