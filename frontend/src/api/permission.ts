import request from '@/utils/request'
import type { Result, PageResult, PageQuery, Permission, PermissionForm } from '@/types'

export function getPermissions(params: PageQuery) {
  return request.get<Result<PageResult<Permission>>>('/api/permissions', { params })
}

export function getAllPermissions() {
  return request.get<Result<Permission[]>>('/api/permissions/all')
}

export function getPermission(id: number) {
  return request.get<Result<Permission>>(`/api/permissions/${id}`)
}

export function createPermission(data: PermissionForm) {
  return request.post<Result<Permission>>('/api/permissions', data)
}

export function updatePermission(id: number, data: PermissionForm) {
  return request.put<Result<Permission>>(`/api/permissions/${id}`, data)
}

export function deletePermission(id: number) {
  return request.delete<Result<void>>(`/api/permissions/${id}`)
}
