<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>权限管理</span>
          <el-button type="primary" @click="openDialog()" v-if="hasPermission('permission:create')">
            <el-icon><Plus /></el-icon>
            新增权限
          </el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-form :inline="true" :model="query">
          <el-form-item label="关键词">
            <el-input
              v-model="query.keyword"
              placeholder="权限名称/编码"
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
        <el-table-column prop="permissionName" label="权限名称" min-width="150" />
        <el-table-column prop="permissionCode" label="权限编码" min-width="150" />
        <el-table-column prop="apiPath" label="API路径" min-width="200" />
        <el-table-column prop="method" label="请求方法" width="100">
          <template #default="{ row }">
            <el-tag :type="getMethodType(row.method)" v-if="row.method">
              {{ row.method }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="150" />
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
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="openDialog(row)"
              v-if="hasPermission('permission:update')"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              link
              @click="handleDelete(row)"
              v-if="hasPermission('permission:delete')"
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

    <!-- Permission Form Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑权限' : '新增权限'"
      width="500px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="权限名称" prop="permissionName">
          <el-input v-model="form.permissionName" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限编码" prop="permissionCode">
          <el-input v-model="form.permissionCode" placeholder="如: user:create" />
        </el-form-item>
        <el-form-item label="API路径" prop="apiPath">
          <el-input v-model="form.apiPath" placeholder="如: /api/users" />
        </el-form-item>
        <el-form-item label="请求方法" prop="method">
          <el-select v-model="form.method" placeholder="请选择" clearable style="width: 100%">
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import type { Permission, PermissionForm, PageQuery } from '@/types'
import { getPermissions, createPermission, updatePermission, deletePermission } from '@/api/permission'

const authStore = useAuthStore()
const hasPermission = (p: string) => authStore.hasPermission(p)

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<Permission[]>([])
const total = ref(0)

const query = reactive<PageQuery>({
  page: 1,
  size: 10,
  keyword: '',
  status: undefined
})

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<PermissionForm>({
  id: undefined,
  permissionName: '',
  permissionCode: '',
  apiPath: '',
  method: '',
  description: '',
  status: 1
})

const rules: FormRules = {
  permissionName: [
    { required: true, message: '请输入权限名称', trigger: 'blur' },
    { max: 50, message: '长度不能超过 50 个字符', trigger: 'blur' }
  ],
  permissionCode: [
    { required: true, message: '请输入权限编码', trigger: 'blur' },
    { max: 50, message: '长度不能超过 50 个字符', trigger: 'blur' }
  ]
}

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

function getMethodType(method: string): string {
  const types: Record<string, string> = {
    GET: 'success',
    POST: 'primary',
    PUT: 'warning',
    DELETE: 'danger'
  }
  return types[method] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const { data } = await getPermissions(query)
    tableData.value = data.data.records
    total.value = data.data.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.page = 1
  query.keyword = ''
  query.status = undefined
  fetchData()
}

function openDialog(row?: Permission) {
  if (row) {
    form.id = row.id
    form.permissionName = row.permissionName
    form.permissionCode = row.permissionCode
    form.apiPath = row.apiPath
    form.method = row.method
    form.description = row.description
    form.status = row.status
  } else {
    form.id = undefined
    form.permissionName = ''
    form.permissionCode = ''
    form.apiPath = ''
    form.method = ''
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
      await updatePermission(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createPermission(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row: Permission) {
  try {
    await ElMessageBox.confirm(`确定要删除权限 "${row.permissionName}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deletePermission(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {
    // cancelled
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
