import { defineStore } from 'pinia'
import { ref } from 'vue'
import { reservationsAPI, checkinAPI, adminAPI } from '../api'

export const useReservationsStore = defineStore('reservations', () => {
  const reservations = ref([])
  const currentReservation = ref(null)
  const stats = ref(null)
  const loading = ref(false)

  async function fetchReservations(params = {}) {
    try {
      loading.value = true
      const { data } = await reservationsAPI.list(params)
      if (data.success) reservations.value = data.data
    } catch (e) {
      console.error('Failed to fetch reservations:', e)
    } finally {
      loading.value = false
    }
  }

  async function fetchReservation(id) {
    try {
      loading.value = true
      const { data } = await reservationsAPI.get(id)
      if (data.success) currentReservation.value = data.data
      return data
    } catch (e) {
      console.error('Failed to fetch reservation:', e)
    } finally {
      loading.value = false
    }
  }

  async function createReservation(resData) {
    const { data } = await reservationsAPI.create(resData)
    return data
  }

  async function approveReservation(id, note = '') {
    const { data } = await reservationsAPI.approve(id, note)
    if (data.success) await fetchReservations()
    return data
  }

  async function rejectReservation(id, note = '') {
    const { data } = await reservationsAPI.reject(id, note)
    if (data.success) await fetchReservations()
    return data
  }

  async function cancelReservation(id) {
    const { data } = await reservationsAPI.cancel(id)
    if (data.success) await fetchReservations()
    return data
  }

  async function deleteReservation(id) {
    const { data } = await reservationsAPI.delete(id)
    if (data.success) await fetchReservations()
    return data
  }

  async function checkin(reservationId) {
    const { data } = await checkinAPI.checkin(reservationId)
    return data
  }

  async function fetchStats() {
    try {
      loading.value = true
      const { data } = await adminAPI.stats()
      if (data.success) stats.value = data.data
    } catch (e) {
      console.error('Failed to fetch stats:', e)
    } finally {
      loading.value = false
    }
  }

  return {
    reservations, currentReservation, stats, loading,
    fetchReservations, fetchReservation, createReservation,
    approveReservation, rejectReservation, cancelReservation, deleteReservation,
    checkin, fetchStats
  }
})
