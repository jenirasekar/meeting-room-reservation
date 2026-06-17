import { defineStore } from 'pinia'
import { ref } from 'vue'
import { roomsAPI, availabilityAPI } from '../api'

export const useRoomsStore = defineStore('rooms', () => {
  const rooms = ref([])
  const currentRoom = ref(null)
  const equipment = ref([])
  const bookedSlots = ref([])
  const loading = ref(false)

  async function fetchRooms(params = {}) {
    try {
      loading.value = true
      const { data } = await roomsAPI.list(params)
      if (data.success) rooms.value = data.data
    } catch (e) {
      console.error('Failed to fetch rooms:', e)
    } finally {
      loading.value = false
    }
  }

  async function fetchRoom(id) {
    try {
      loading.value = true
      const { data } = await roomsAPI.get(id)
      if (data.success) {
        currentRoom.value = data.data.room
        equipment.value = data.data.equipment
      }
      return data
    } catch (e) {
      console.error('Failed to fetch room:', e)
    } finally {
      loading.value = false
    }
  }

  async function createRoom(roomData) {
    const { data } = await roomsAPI.create(roomData)
    if (data.success) await fetchRooms()
    return data
  }

  async function updateRoom(id, roomData) {
    const { data } = await roomsAPI.update(id, roomData)
    if (data.success) await fetchRooms()
    return data
  }

  async function deleteRoom(id) {
    const { data } = await roomsAPI.delete(id)
    if (data.success) await fetchRooms()
    return data
  }

  // AJAX: fetch availability — does NOT touch loading so it won't affect parent pages
  async function fetchAvailability(roomId, date) {
    const { data } = await availabilityAPI.getSlots(roomId, date)
    if (data.success) bookedSlots.value = data.data
    return data
  }

  return {
    rooms, currentRoom, equipment, bookedSlots, loading,
    fetchRooms, fetchRoom, createRoom, updateRoom, deleteRoom, fetchAvailability
  }
})
