<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="openDialog()" v-if="hasPermission('role:create')">
            <el-icon><Plus /></el-icon>
            新增角色
          </el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-form :inline="true" :model="query">
          <el-form-item label="关键词">
            <el-input
              v-model="query.keyword"
              placeholder="角色名称/编码"
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
        <el-table-column prop="roleName" label="角色名称" min-width="150" />
        <el-table-column prop="roleCode" label="角色编码" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
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
              v-if="hasPermission('role:update')"
            >
              编辑
            </el-button>
            <el-button
              type="primary"
              link
              @click="openPermissionsDialog(row)"
              v-if="hasPermission('role:update')"
            >
              权限
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(row)"
              v-if="hasPermission('role:delete')"
              :disabled="row.roleCode === 'ADMIN'"
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

    <!-- Role Form Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑角色' : '新增角色'"
      width="500px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" :disabled="!!form.id" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
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

    <!-- Permissions Assignment Dialog -->
    <el-dialog v-model="permissionsDialogVisible" title="分配权限" width="600px">
      <el-checkbox-group v-model="selectedPermissionIds">
        <el-row :gutter="10">
          <el-col :span="12" v-for="permission in allPermissions" :key="permission.id">
            <el-checkbox :label="permission.id" style="margin: 8px 0; display: block">
              {{ permission.permissionName }} ({{ permission.permissionCode }})
            </el-checkbox>
          </el-col>
        </el-row>
      </el-checkbox-group>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="permissionsDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handlePermissionsSubmit">
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
import type { Role, RoleForm, Permission, PageQuery } from '@/types'
import { getRoles, getRolePermissions, createRole, updateRole, deleteRole, updateRolePermissions } from '@/api/role'
import { getAllPermissions } from '@/api/permission'

const authStore = useAuthStore()
const hasPermission = (p: string) => authStore.hasPermission(p)

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<Role[]>([])
const total = ref(0)
const allPermissions = ref<Permission[]>([])

const query = reactive<PageQuery>({
  page: 1,
  size: 10,
  keyword: '',
  status: undefined
})

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<RoleForm>({
  id: undefined,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1,
  permissionIds: []
})

const rules: FormRules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { max: 50, message: '长度不能超过 50 个字符', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { max: 50, message: '长度不能超过 50 个字符', trigger: 'blur' }
  ]
}

const permissionsDialogVisible = ref(false)
const selectedPermissionIds = ref<number[]>([])
const currentRoleId = ref<number>()

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
    const { data } = await getRoles(query)
    tableData.value = data.data.records
    total.value = data.data.total
  } finally {
    loading.value = false
  }
}

async function fetchPermissions() {
  const { data } = await getAllPermissions()
  allPermissions.value = data.data
}

function resetQuery() {
  query.page = 1
  query.keyword = ''
  query.status = undefined
  fetchData()
}

function openDialog(row?: Role) {
  if (row) {
    form.id = row.id
    form.roleName = row.roleName
    form.roleCode = row.roleCode
    form.description = row.description
    form.status = row.status
  } else {
    form.id = undefined
    form.roleName = ''
    form.roleCode = ''
    form.description = ''
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
      await updateRole(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createRole(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: Role) {
  try {
    await ElMessageBox.confirm(`确定要删除角色 "${row.roleName}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // cancelled
  }
}

async function openPermissionsDialog(row: Role) {
  currentRoleId.value = row.id
  const { data } = await getRolePermissions(row.id)
  selectedPermissionIds.value = data.data
  permissionsDialogVisible.value = true
}

async function handlePermissionsSubmit() {
  if (!currentRoleId.value) return

  submitting.value = true
  try {
    await updateRolePermissions(currentRoleId.value, selectedPermissionIds.value)
    ElMessage.success('权限分配成功')
    permissionsDialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchData()
  fetchPermissions()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
