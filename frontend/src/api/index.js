import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Response interceptor — handle auth errors
// Bug 7 fix: use dynamic import() instead of require() in ES module context
api.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      const { useAuthStore } = await import('../stores/auth')
      // Only call if Pinia is already active (i.e. inside a component context)
      try {
        const auth = useAuthStore()
        auth.logout()
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

// ---- Admin ----
export const adminAPI = {
  stats() { return api.get('/admin/stats') }
}

export default api
