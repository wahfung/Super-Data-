import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/components/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '仪表盘' }
      },
      {
        path: 'system/users',
        name: 'Users',
        component: () => import('@/views/system/users/UsersView.vue'),
        meta: { title: '用户管理', permission: 'user:list' }
      },
      {
        path: 'system/roles',
        name: 'Roles',
        component: () => import('@/views/system/roles/RolesView.vue'),
        meta: { title: '角色管理', permission: 'role:list' }
      },
      {
        path: 'system/permissions',
        name: 'Permissions',
        component: () => import('@/views/system/permissions/PermissionsView.vue'),
        meta: { title: '权限管理', permission: 'permission:list' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth !== false) {
    if (!authStore.isLoggedIn) {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }

    if (!authStore.userInfo) {
      await authStore.fetchUserInfo()
    }

    const permission = to.meta.permission as string | undefined
    if (permission && !authStore.hasPermission(permission)) {
      next({ path: '/' })
      return
    }
  }

  next()
})

export default router
