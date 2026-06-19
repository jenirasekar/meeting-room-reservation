import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Response interceptor — handle auth errors
api.interceptors.response.use(
  response => response,
  async error => {
    const requestUrl = error.config?.url || ''
    const isAuthEndpoint = requestUrl.includes('/auth/logout') || requestUrl.includes('/auth/login') || requestUrl.includes('/auth/me')

    // Never trigger logout/redirect logic from a failed auth-endpoint request itself —
    // that's what was causing the infinite /auth/logout loop.
    if (error.response?.status === 401 && !isAuthEndpoint) {
      const { useAuthStore } = await import('../stores/auth')
      try {
        const auth = useAuthStore()
        auth.user = null // clear local state directly, skip calling logout() API again
      } catch (e) {
        // Pinia not ready yet (e.g. during initial fetchUser), ignore
      }
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

// ---- Auth ----
export const authAPI = {
  register(data) { return api.post('/auth/register', data) },
  login(data) { return api.post('/auth/login', data) },
  logout() { return api.post('/auth/logout') },
  me() { return api.get('/auth/me') }
}

// ---- Rooms ----
export const roomsAPI = {
  list(params) { return api.get('/rooms', { params }) },
  get(id) { return api.get(`/rooms/${id}`) },
  create(data) { return api.post('/rooms', data) },
  update(id, data) { return api.put(`/rooms/${id}`, data) },
  delete(id) { return api.delete(`/rooms/${id}`) }
}

// ---- Equipment ----
export const equipmentAPI = {
  add(roomId, data) { return api.post(`/rooms/${roomId}/equipment`, data) },
  update(id, data) { return api.put(`/equipment/${id}`, data) },
  delete(id) { return api.delete(`/equipment/${id}`) }
}

// ---- Availability (AJAX) ----
export const availabilityAPI = {
  getSlots(roomId, date) {
    return api.get(`/rooms/${roomId}/availability`, { params: { date } })
  }
}

// ---- Reservations ----
export const reservationsAPI = {
  list(params) { return api.get('/reservations', { params }) },
  get(id) { return api.get(`/reservations/${id}`) },
  create(data) { return api.post('/reservations', data) },
  approve(id, note) { return api.put(`/reservations/${id}/approve`, { note }) },
  reject(id, note) { return api.put(`/reservations/${id}/reject`, { note }) },
  cancel(id) { return api.put(`/reservations/${id}/cancel`) }
}

// ---- Check-in ----
export const checkinAPI = {
  checkin(reservationId) { return api.post(`/checkin/${reservationId}`) }
}

// ---- Upload ----
export const uploadAPI = {
  roomImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/upload/room-image', formData, {
      headers: { 'Content-Type': undefined }
    })
  }
}

// ---- Admin ----
export const adminAPI = {
  stats() { return api.get('/admin/stats') },
  // User management
  listUsers() { return api.get('/admin/users') },
  getUser(id) { return api.get(`/admin/users/${id}`) },
  updateUser(id, data) { return api.put(`/admin/users/${id}`, data) },
  deleteUser(id) { return api.delete(`/admin/users/${id}`) }
}

export default api
