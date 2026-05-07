export interface Result<T> {
  success: boolean
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  pages: number
  size: number
}

export interface PageQuery {
  page: number
  size: number
  keyword?: string
  status?: number
}

export interface User {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  status: number
  createdAt: string
  updatedAt: string
  roles?: Role[]
  permissions?: string[]
}

export interface UserForm {
  id?: number
  username: string
  password?: string
  nickname: string
  email: string
  phone: string
  avatar?: string
  status: number
  roleIds: number[]
}

export interface Role {
  id: number
  roleName: string
  roleCode: string
  description: string
  status: number
  createdAt: string
  updatedAt: string
  permissions?: Permission[]
}

export interface RoleForm {
  id?: number
  roleName: string
  roleCode: string
  description: string
  status: number
  permissionIds: number[]
}

export interface Permission {
  id: number
  permissionName: string
  permissionCode: string
  apiPath: string
  method: string
  description: string
  status: number
  createdAt: string
  updatedAt: string
}

export interface PermissionForm {
  id?: number
  permissionName: string
  permissionCode: string
  apiPath: string
  method: string
  description: string
  status: number
}

export interface LoginForm {
  username: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  userInfo: UserInfo
}

export interface UserInfo {
  id: number
  username: string
  nickname: string
  email: string
  avatar: string
  roles: string[]
  permissions: string[]
}
