<script setup>
import { ref, onMounted, computed } from 'vue'
import { adminAPI } from '../../api'
import { useAuthStore } from '../../stores/auth'
import { useToastStore } from '../../stores/toast'
import SkeletonLoader from '../../components/SkeletonLoader.vue'
import AppIcon from '../../components/AppIcon.vue'

const auth = useAuthStore()
const toast = useToastStore()

const users = ref([])
const loading = ref(false)
const searchQuery = ref('')
const editingUserId = ref(null)
const editForm = ref({ email: '', role: 'user' })
const saving = ref(false)
const deleteConfirmId = ref(null)

const currentUserId = computed(() => auth.user?.id)

const filteredUsers = computed(() => {
  if (!searchQuery.value.trim()) return users.value
  const q = searchQuery.value.toLowerCase()
  return users.value.filter(u =>
    u.username.toLowerCase().includes(q) ||
    (u.email && u.email.toLowerCase().includes(q))
  )
})

onMounted(() => { fetchUsers() })

async function fetchUsers() {
  try {
    loading.value = true
    const { data } = await adminAPI.listUsers()
    if (data.success) users.value = data.data
  } catch (e) {
    console.error('Failed to fetch users:', e)
  } finally {
    loading.value = false
  }
}

function startEdit(user) {
  editingUserId.value = user.id
  editForm.value = { email: user.email || '', role: user.role }
}

function cancelEdit() {
  editingUserId.value = null
  deleteConfirmId.value = null
}

async function saveEdit(id) {
  try {
    saving.value = true
    const { data } = await adminAPI.updateUser(id, editForm.value)
    if (data.success) {
      toast.success('User updated')
      editingUserId.value = null
      await fetchUsers()
    } else {
      toast.error(data.message || 'Failed to update')
    }
  } catch (e) {
    toast.error(e.response?.data?.message || 'Failed to update')
  } finally {
    saving.value = false
  }
}

async function handleDelete(id) {
  if (deleteConfirmId.value === id) {
    try {
      const { data } = await adminAPI.deleteUser(id)
      if (data.success) {
        toast.success('User deleted')
        deleteConfirmId.value = null
        await fetchUsers()
      } else {
        toast.error(data.message || 'Failed to delete')
      }
    } catch (e) {
      toast.error(e.response?.data?.message || 'Failed to delete')
    }
  } else {
    deleteConfirmId.value = id
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString([], { year: 'numeric', month: 'short', day: 'numeric' })
}
</script>

<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    <div class="flex items-center justify-between mb-8">
      <div>
        <h1 class="text-2xl font-bold text-surface-900">Manage Users</h1>
        <p class="text-surface-500 mt-1">View, edit roles, or remove user accounts</p>
      </div>
      <button @click="fetchUsers" class="btn btn-secondary">
        <AppIcon icon="refresh" :size="16" />
        Refresh
      </button>
    </div>

    <!-- Search -->
    <div class="relative max-w-sm mb-6">
      <AppIcon icon="search" :size="16" class="absolute left-3 top-1/2 -translate-y-1/2 text-surface-400 z-10 pointer-events-none" />
      <input
        v-model="searchQuery"
        type="text"
        class="w-full pl-10 pr-4 py-2.5 rounded-xl bg-white border border-surface-200 text-sm placeholder-surface-400 focus:outline-none focus:ring-2 focus:ring-primary-500/30 focus:border-primary-400"
        placeholder="Search by username or email..."
      />
    </div>

    <!-- Loading -->
    <div v-if="loading" class="space-y-4">
      <SkeletonLoader variant="list-item" :count="5" />
    </div>

    <!-- Empty -->
    <div v-else-if="!users.length" class="text-center py-16">
      <div class="w-20 h-20 rounded-2xl bg-surface-100 flex items-center justify-center mx-auto mb-4">
        <AppIcon icon="users" :size="36" class="text-surface-300" />
      </div>
      <h3 class="text-lg font-semibold text-surface-700 mb-1">No users found</h3>
      <p class="text-surface-400">User accounts will appear here.</p>
    </div>

    <!-- Search empty -->
    <div v-else-if="!filteredUsers.length" class="text-center py-16">
      <div class="w-20 h-20 rounded-2xl bg-surface-100 flex items-center justify-center mx-auto mb-4">
        <AppIcon icon="search" :size="36" class="text-surface-300" />
      </div>
      <h3 class="text-lg font-semibold text-surface-700 mb-1">No matching users</h3>
      <p class="text-surface-400">Try a different search term.</p>
    </div>

    <!-- User list -->
    <div v-else class="space-y-3">
      <div
        v-for="user in filteredUsers"
        :key="user.id"
        class="card hover:shadow-soft-lg transition-all"
      >
        <div class="flex flex-col sm:flex-row items-start gap-4">
          <!-- Avatar + info -->
          <div class="flex items-center gap-4 flex-1 min-w-0">
            <div
              :class="[
                'w-10 h-10 rounded-xl flex items-center justify-center text-white text-sm font-bold shrink-0',
                user.role === 'admin'
                  ? 'bg-gradient-to-br from-amber-400 to-orange-500'
                  : 'bg-gradient-to-br from-primary-400 to-primary-600'
              ]"
            >
              {{ (user.username || 'U')[0].toUpperCase() }}
            </div>
            <div class="min-w-0">
              <div class="flex items-center gap-2 mb-0.5">
                <span class="font-semibold text-surface-900">{{ user.username }}</span>
                <span
                  :class="[
                    'badge text-[10px]',
                    user.role === 'admin'
                      ? 'bg-amber-50 text-amber-700 border-amber-200'
                      : 'bg-slate-100 text-slate-600 border-slate-200'
                  ]"
                >
                  {{ user.role }}
                </span>
                <span
                  v-if="user.id === currentUserId"
                  class="text-[10px] text-primary-500 font-medium bg-primary-50 px-2 py-0.5 rounded-full"
                >
                  you
                </span>
              </div>
              <p class="text-sm text-surface-400 truncate">{{ user.email || 'No email' }}</p>
              <p class="text-xs text-surface-300 mt-0.5">Joined {{ formatDate(user.createdAt) }}</p>
            </div>
          </div>

          <!-- Actions / Edit form -->
          <div class="flex items-center gap-2 shrink-0 w-full sm:w-auto">
            <template v-if="editingUserId === user.id">
              <input
                v-model="editForm.email"
                type="email"
                class="input-field w-40 sm:w-48 text-sm"
                placeholder="email@example.com"
              />
              <select v-model="editForm.role" class="input-field w-24 text-sm">
                <option value="user">User</option>
                <option value="admin">Admin</option>
              </select>
              <button
                @click="saveEdit(user.id)"
                class="btn btn-primary btn-sm"
                :disabled="saving"
              >
                <span v-if="saving" class="inline-block w-3.5 h-3.5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                <AppIcon v-else icon="check" :size="14" />
              </button>
              <button @click="cancelEdit" class="btn btn-ghost btn-sm">
                <AppIcon icon="x-mark" :size="14" />
              </button>
            </template>
            <template v-else>
              <button
                v-if="user.id !== currentUserId"
                @click="startEdit(user)"
                class="btn btn-secondary btn-sm"
              >
                <AppIcon icon="pencil" :size="14" />
                <span class="hidden sm:inline">Edit</span>
              </button>
              <button
                v-if="user.id !== currentUserId"
                @click="handleDelete(user.id)"
                :class="deleteConfirmId === user.id ? 'btn btn-danger btn-sm animate-shake' : 'btn btn-ghost btn-sm text-red-500 hover:bg-red-50'"
              >
                <AppIcon icon="trash" :size="14" />
                <span class="hidden sm:inline">{{ deleteConfirmId === user.id ? 'Confirm?' : 'Delete' }}</span>
              </button>
              <button
                v-if="deleteConfirmId === user.id"
                @click="deleteConfirmId = null"
                class="btn btn-ghost btn-sm"
              >
                Keep
              </button>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
