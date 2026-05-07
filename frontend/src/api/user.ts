import request from '@/utils/request'
import type { Result, PageResult, PageQuery, User, UserForm } from '@/types'

export function getUsers(params: PageQuery) {
  return request.get<Result<PageResult<User>>>('/api/users', { params })
}

export function getUser(id: number) {
  return request.get<Result<User>>(`/api/users/${id}`)
}

export function getUserRoles(id: number) {
  return request.get<Result<number[]>>(`/api/users/${id}/roles`)
}

export function createUser(data: UserForm) {
  return request.post<Result<User>>('/api/users', data)
}

export function updateUser(id: number, data: UserForm) {
  return request.put<Result<User>>(`/api/users/${id}`, data)
}

export function deleteUser(id: number) {
  return request.delete<Result<void>>(`/api/users/${id}`)
}

export function updateUserRoles(id: number, roleIds: number[]) {
  return request.put<Result<void>>(`/api/users/${id}/roles`, { roleIds })
}

export function updateUserStatus(id: number, status: number) {
  return request.put<Result<void>>(`/api/users/${id}/status`, { status })
}
