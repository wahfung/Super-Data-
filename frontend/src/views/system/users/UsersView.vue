<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="openDialog()" v-if="hasPermission('user:create')">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-form :inline="true" :model="query">
          <el-form-item label="关键词">
            <el-input
              v-model="query.keyword"
              placeholder="用户名/昵称/邮箱"
              clearable
              @keyup.enter="fetchData"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="全部" clearable style="width: 100px">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchData">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetQuery">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :disabled="row.username === 'admin' || !hasPermission('user:update')"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="openDialog(row)"
              v-if="hasPermission('user:update')"
            >
              编辑
            </el-button>
            <el-button
              type="primary"
              link
              @click="openRolesDialog(row)"
              v-if="hasPermission('user:update')"
            >
              角色
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(row)"
              v-if="hasPermission('user:delete')"
              :disabled="row.username === 'admin'"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- User Form Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑用户' : '新增用户'"
      width="500px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :placeholder="form.id ? '不修改请留空' : '请输入密码'"
          />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Roles Assignment Dialog -->
    <el-dialog v-model="rolesDialogVisible" title="分配角色" width="500px">
      <el-checkbox-group v-model="selectedRoleIds">
        <el-checkbox
          v-for="role in allRoles"
          :key="role.id"
          :label="role.id"
          style="display: block; margin: 10px 0"
        >
          {{ role.roleName }} ({{ role.roleCode }})
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="rolesDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleRolesSubmit">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import type { User, UserForm, Role, PageQuery } from '@/types'
import { getUsers, getUserRoles, createUser, updateUser, deleteUser, updateUserStatus, updateUserRoles } from '@/api/user'
import { getAllRoles } from '@/api/role'

const authStore = useAuthStore()
const hasPermission = (p: string) => authStore.hasPermission(p)

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<User[]>([])
const total = ref(0)
const allRoles = ref<Role[]>([])

const query = reactive<PageQuery>({
  page: 1,
  size: 10,
  keyword: '',
  status: undefined
})

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<UserForm>({
  id: undefined,
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  status: 1,
  roleIds: []
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur', validator: (_rule, value, callback) => {
      if (!form.id && !value) {
        callback(new Error('请输入密码'))
      } else if (value && (value.length < 6 || value.length > 50)) {
        callback(new Error('密码长度在 6 到 50 个字符'))
      } else {
        callback()
      }
    }}
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const rolesDialogVisible = ref(false)
const selectedRoleIds = ref<number[]>([])
const currentUserId = ref<number>()

function formatDate(date: string | number[] | null | undefined): string {
  if (!date) return ''
  let parsed: Date
  if (Array.isArray(date)) {
    // Jackson 数组格式: [year, month, day, hour, minute, second]
    const [year, month, day, hour = 0, minute = 0, second = 0] = date
    parsed = new Date(year, month - 1, day, hour, minute, second)
  } else if (typeof date === 'string') {
    parsed = new Date(date.replace(' ', 'T'))
  } else {
    return ''
  }
  return isNaN(parsed.getTime()) ? String(date) : parsed.toLocaleString('zh-CN')
}

async function fetchData() {
  loading.value = true
  try {
    const { data } = await getUsers(query)
    tableData.value = data.data.records
    total.value = data.data.total
  } finally {
    loading.value = false
  }
}

async function fetchRoles() {
  const { data } = await getAllRoles()
  allRoles.value = data.data
}

function resetQuery() {
  query.page = 1
  query.keyword = ''
  query.status = undefined
  fetchData()
}

function openDialog(row?: User) {
  if (row) {
    form.id = row.id
    form.username = row.username
    form.password = ''
    form.nickname = row.nickname
    form.email = row.email
    form.phone = row.phone
    form.status = row.status
  } else {
    form.id = undefined
    form.username = ''
    form.password = ''
    form.nickname = ''
    form.email = ''
    form.phone = ''
    form.status = 1
  }
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
}

async function handleSubmit() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  submitting.value = true
  try {
    if (form.id) {
      await updateUser(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createUser(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: User) {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // cancelled
  }
}

async function handleStatusChange(row: User) {
  try {
    await updateUserStatus(row.id, row.status)
    ElMessage.success('状态更新成功')
  } catch {
    row.status = row.status === 1 ? 0 : 1
  }
}

async function openRolesDialog(row: User) {
  currentUserId.value = row.id
  const { data } = await getUserRoles(row.id)
  selectedRoleIds.value = data.data
  rolesDialogVisible.value = true
}

async function handleRolesSubmit() {
  if (!currentUserId.value) return

  submitting.value = true
  try {
    await updateUserRoles(currentUserId.value, selectedRoleIds.value)
    ElMessage.success('角色分配成功')
    rolesDialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchData()
  fetchRoles()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
