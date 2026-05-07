<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
            <el-icon size="32"><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.userCount }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
            <el-icon size="32"><UserFilled /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.roleCount }}</div>
            <div class="stat-label">角色总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
            <el-icon size="32"><Key /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.permissionCount }}</div>
            <div class="stat-label">权限总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
            <el-icon size="32"><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.activeUsers }}</div>
            <div class="stat-label">活跃用户</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>快速入口</span>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="8" v-for="item in quickLinks" :key="item.path">
              <div class="quick-link" @click="router.push(item.path)">
                <el-icon :size="40" :color="item.color">
                  <component :is="item.icon" />
                </el-icon>
                <span>{{ item.title }}</span>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>当前用户</span>
            </div>
          </template>
          <div class="user-profile">
            <el-avatar :size="64" :icon="UserFilled" />
            <div class="user-details">
              <h3>{{ userInfo?.nickname || userInfo?.username }}</h3>
              <p>{{ userInfo?.email }}</p>
              <div class="user-roles">
                <el-tag
                  v-for="role in userInfo?.roles"
                  :key="role"
                  size="small"
                  type="primary"
                  style="margin-right: 8px"
                >
                  {{ role }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>系统信息</span>
        </div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="系统名称">Super Data 数据管理系统</el-descriptions-item>
        <el-descriptions-item label="版本号">v1.0.0</el-descriptions-item>
        <el-descriptions-item label="技术栈">SpringBoot + Vue3 + ElementPlus</el-descriptions-item>
        <el-descriptions-item label="数据库">MySQL 8.0</el-descriptions-item>
        <el-descriptions-item label="认证方式">JWT + Basic Auth</el-descriptions-item>
        <el-descriptions-item label="部署方式">Docker Compose</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { User, UserFilled, Key, CircleCheck } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { getUsers } from '@/api/user'
import { getRoles } from '@/api/role'
import { getPermissions } from '@/api/permission'

const router = useRouter()
const authStore = useAuthStore()
const userInfo = computed(() => authStore.userInfo)

const stats = ref({
  userCount: 0,
  roleCount: 0,
  permissionCount: 0,
  activeUsers: 0
})

const quickLinks = [
  { path: '/system/users', title: '用户管理', icon: User, color: '#667eea' },
  { path: '/system/roles', title: '角色管理', icon: UserFilled, color: '#f5576c' },
  { path: '/system/permissions', title: '权限管理', icon: Key, color: '#4facfe' }
]

onMounted(async () => {
  try {
    const [usersRes, rolesRes, permissionsRes] = await Promise.all([
      getUsers({ page: 1, size: 1 }),
      getRoles({ page: 1, size: 1 }),
      getPermissions({ page: 1, size: 1 })
    ])
    stats.value = {
      userCount: usersRes.data.data.total,
      roleCount: rolesRes.data.data.total,
      permissionCount: permissionsRes.data.data.total,
      activeUsers: usersRes.data.data.total
    }
  } catch {
    // Error handled by interceptor
  }
})
</script>

<style scoped>
.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  width: 100%;
  padding: 20px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 16px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
  margin-top: 4px;
}

.quick-link {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px 20px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  background: #f5f7fa;
}

.quick-link:hover {
  background: #e6f7ff;
  transform: translateY(-4px);
}

.quick-link span {
  margin-top: 12px;
  font-size: 14px;
  color: #333;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-details h3 {
  margin: 0 0 8px;
  font-size: 18px;
  color: #333;
}

.user-details p {
  margin: 0 0 12px;
  font-size: 14px;
  color: #999;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
}
</style>
