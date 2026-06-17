<script setup>
import { ref, onMounted } from 'vue'
import { useRoomsStore } from '../../stores/rooms'
import { equipmentAPI } from '../../api'

const roomsStore = useRoomsStore()

const showForm = ref(false)
const editingRoom = ref(null)
const form = ref({
  name: '', location: '', capacity: 10, description: '', status: 'available', imageUrl: ''
})
const formError = ref('')
const formSubmitting = ref(false)

// Equipment management
const showEquipForm = ref(false)
const equipRoomId = ref(0)
const equipForm = ref({ name: '', quantity: 1 })

onMounted(() => {
  roomsStore.fetchRooms()
})

function openCreateForm() {
  editingRoom.value = null
  form.value = { name: '', location: '', capacity: 10, description: '', status: 'available', imageUrl: '' }
  formError.value = ''
  showForm.value = true
}

function openEditForm(room) {
  editingRoom.value = room
  form.value = {
    name: room.name,
    location: room.location || '',
    capacity: room.capacity,
    description: room.description || '',
    status: room.status,
    imageUrl: room.imageUrl || ''
  }
  formError.value = ''
  showForm.value = true
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
  if (!confirm(`Delete room "${room.name}"?`)) return
  const result = await roomsStore.deleteRoom(room.id)
  if (!result.success) {
    alert(result.message || 'Failed to delete')
  }
}

function openEquipManager(room) {
  equipRoomId.value = room.id
  showEquipForm.value = true
  roomsStore.fetchRoom(room.id)
}

async function addEquipment() {
  if (!equipForm.value.name.trim()) return
  await equipmentAPI.add(equipRoomId.value, equipForm.value)
  equipForm.value = { name: '', quantity: 1 }
  await roomsStore.fetchRoom(equipRoomId.value)
}

async function deleteEquipment(id) {
  if (!confirm('Delete this equipment?')) return
  await equipmentAPI.delete(id)
  await roomsStore.fetchRoom(equipRoomId.value)
}
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="flex items-center justify-between mb-8">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Manage Rooms</h1>
        <p class="text-gray-500 mt-1">Add, edit, or remove meeting rooms</p>
      </div>
      <button @click="openCreateForm" class="btn-primary">+ Add Room</button>
    </div>

    <!-- Room list -->
    <div v-if="roomsStore.loading" class="text-center py-12">
      <p class="text-gray-500">Loading...</p>
    </div>

    <div v-else class="space-y-4">
      <div v-for="room in roomsStore.rooms" :key="room.id" class="card">
        <div class="flex flex-col sm:flex-row items-start justify-between gap-4">
          <div class="flex-1">
            <div class="flex items-center gap-3 mb-2">
              <h3 class="text-lg font-semibold">{{ room.name }}</h3>
              <span :class="room.status === 'available' ? 'badge bg-green-100 text-green-700' : 'badge bg-red-100 text-red-700'">
                {{ room.status }}
              </span>
            </div>
            <p class="text-sm text-gray-500">
              📍 {{ room.location || 'N/A' }} · 👥 {{ room.capacity }} people
            </p>
            <p class="text-sm text-gray-400 mt-1">{{ room.description || 'No description' }}</p>
          </div>
          <div class="flex gap-2 shrink-0">
            <button @click="openEquipManager(room)" class="btn-secondary btn-sm">Equipment</button>
            <button @click="openEditForm(room)" class="btn-secondary btn-sm">Edit</button>
            <button @click="handleDelete(room)" class="btn-danger btn-sm">Delete</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Room create/edit modal -->
    <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-lg p-6 max-h-[90vh] overflow-y-auto">
        <h2 class="text-xl font-semibold mb-4">
          {{ editingRoom ? 'Edit Room' : 'Add Room' }}
        </h2>
        <form @submit.prevent="handleSave" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Name *</label>
            <input v-model="form.name" type="text" class="input-field" required />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Location</label>
            <input v-model="form.location" type="text" class="input-field" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Capacity</label>
            <input v-model.number="form.capacity" type="number" min="1" class="input-field" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
            <textarea v-model="form.description" class="input-field" rows="3"></textarea>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Status</label>
            <select v-model="form.status" class="input-field">
              <option value="available">Available</option>
              <option value="unavailable">Unavailable</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Image URL</label>
            <input v-model="form.imageUrl" type="url" class="input-field" placeholder="https://..." />
          </div>

          <div v-if="formError" class="text-sm text-red-600 bg-red-50 rounded-lg p-3">
            {{ formError }}
          </div>

          <div class="flex gap-3 pt-2">
            <button type="submit" class="btn-primary flex-1" :disabled="formSubmitting">
              {{ formSubmitting ? 'Saving...' : 'Save' }}
            </button>
            <button type="button" class="btn-secondary" @click="showForm = false">Cancel</button>
          </div>
        </form>
      </div>
    </div>

    <!-- Equipment manager modal -->
    <div v-if="showEquipForm" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-md p-6">
        <h2 class="text-xl font-semibold mb-4">Manage Equipment</h2>

        <!-- Current equipment -->
        <div v-if="roomsStore.equipment.length" class="space-y-2 mb-4">
          <div v-for="item in roomsStore.equipment" :key="item.id"
               class="flex items-center justify-between bg-gray-50 rounded-lg p-3">
            <div>
              <span class="font-medium">{{ item.name }}</span>
              <span class="text-gray-500 ml-2">x{{ item.quantity }}</span>
            </div>
            <button @click="deleteEquipment(item.id)" class="text-red-500 hover:text-red-700 text-sm">
              Remove
            </button>
          </div>
        </div>
        <p v-else class="text-gray-500 text-sm mb-4">No equipment added yet</p>

        <!-- Add form -->
        <div class="flex gap-2">
          <input v-model="equipForm.name" type="text" class="input-field flex-1" placeholder="Item name" />
          <input v-model.number="equipForm.quantity" type="number" min="1" class="input-field w-20" />
          <button @click="addEquipment" class="btn-primary btn-sm">Add</button>
        </div>

        <button @click="showEquipForm = false" class="btn-secondary w-full mt-4">Close</button>
      </div>
    </div>
  </div>
</template>
