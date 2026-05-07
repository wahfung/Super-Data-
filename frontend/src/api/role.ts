import request from '@/utils/request'
import type { Result, PageResult, PageQuery, Role, RoleForm } from '@/types'

export function getRoles(params: PageQuery) {
  return request.get<Result<PageResult<Role>>>('/api/roles', { params })
}

export function getAllRoles() {
  return request.get<Result<Role[]>>('/api/roles/all')
}

export function getRole(id: number) {
  return request.get<Result<Role>>(`/api/roles/${id}`)
}

export function getRolePermissions(id: number) {
  return request.get<Result<number[]>>(`/api/roles/${id}/permissions`)
}

export function createRole(data: RoleForm) {
  return request.post<Result<Role>>('/api/roles', data)
}

export function updateRole(id: number, data: RoleForm) {
  return request.put<Result<Role>>(`/api/roles/${id}`, data)
}

export function deleteRole(id: number) {
  return request.delete<Result<void>>(`/api/roles/${id}`)
}

export function updateRolePermissions(id: number, permissionIds: number[]) {
  return request.put<Result<void>>(`/api/roles/${id}/permissions`, { permissionIds })
}
