import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  {
    path: '/',
    redirect: '/rooms'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/Register.vue'),
    meta: { guest: true }
  },
  {
    path: '/rooms',
    name: 'Rooms',
    component: () => import('../views/rooms/Rooms.vue')
  },
  {
    path: '/rooms/:id',
    name: 'RoomDetail',
    component: () => import('../views/rooms/RoomDetail.vue')
  },
  {
    path: '/my-reservations',
    name: 'MyReservations',
    component: () => import('../views/user/MyReservations.vue')
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('../views/admin/AdminDashboard.vue'),
    meta: { admin: true }
  },
  {
    path: '/admin/rooms',
    name: 'AdminRooms',
    component: () => import('../views/admin/AdminRooms.vue'),
    meta: { admin: true }
  },
  {
    path: '/admin/reservations',
    name: 'AdminReservations',
    component: () => import('../views/admin/AdminReservations.vue'),
    meta: { admin: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const auth = useAuthStore()

  // Try to restore session on first load
  if (!auth.user && !auth.loading) {
    await auth.fetchUser()
  }

  // Guest-only routes (login, register)
  if (to.meta.guest && auth.isLoggedIn) {
    return next('/rooms')
  }

  // Admin-only routes
  if (to.meta.admin && !auth.isAdmin) {
    return next('/rooms')
  }

  next()
})

export default router
