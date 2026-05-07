import request from '@/utils/request'
import type { Result, LoginForm, LoginResponse, UserInfo } from '@/types'

export function login(data: LoginForm) {
  return request.post<Result<LoginResponse>>('/api/auth/login', data)
}

export function logout() {
  return request.post<Result<void>>('/api/auth/logout')
}

export function refreshToken(refreshToken: string) {
  return request.post<Result<LoginResponse>>('/api/auth/refresh', { refreshToken })
}

export function getCurrentUser() {
  return request.get<Result<UserInfo>>('/api/auth/me')
}
