-- Super-Data 数据库初始化脚本
CREATE DATABASE IF NOT EXISTS superdata DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE superdata;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_role_code (role_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(50) NOT NULL UNIQUE COMMENT '权限编码',
    api_path VARCHAR(200) COMMENT 'API路径',
    method VARCHAR(10) COMMENT 'HTTP方法',
    description VARCHAR(200) COMMENT '权限描述',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_permission_code (permission_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- API客户端表 (用于Basic Auth)
CREATE TABLE IF NOT EXISTS sys_api_client (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    client_id VARCHAR(50) NOT NULL UNIQUE COMMENT '客户端ID',
    client_secret VARCHAR(100) NOT NULL COMMENT '客户端密钥(BCrypt加密)',
    client_name VARCHAR(100) COMMENT '客户端名称',
    description VARCHAR(200) COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_client_id (client_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API客户端表';

-- ============ 初始化数据 ============

-- 插入角色
INSERT INTO sys_role (id, role_name, role_code, description, status) VALUES
(1, '超级管理员', 'ADMIN', '系统超级管理员，拥有所有权限', 1),
(2, '普通用户', 'USER', '普通用户，拥有基础权限', 1),
(3, 'API客户端', 'API_CLIENT', 'API客户端角色，用于对外接口', 1);

-- 插入权限
INSERT INTO sys_permission (id, permission_name, permission_code, api_path, method, description, status) VALUES
(1, '查看用户列表', 'user:list', '/api/users', 'GET', '查看用户列表权限', 1),
(2, '创建用户', 'user:create', '/api/users', 'POST', '创建用户权限', 1),
(3, '更新用户', 'user:update', '/api/users/*', 'PUT', '更新用户权限', 1),
(4, '删除用户', 'user:delete', '/api/users/*', 'DELETE', '删除用户权限', 1),
(5, '查看角色列表', 'role:list', '/api/roles', 'GET', '查看角色列表权限', 1),
(6, '创建角色', 'role:create', '/api/roles', 'POST', '创建角色权限', 1),
(7, '更新角色', 'role:update', '/api/roles/*', 'PUT', '更新角色权限', 1),
(8, '删除角色', 'role:delete', '/api/roles/*', 'DELETE', '删除角色权限', 1),
(9, '查看权限列表', 'permission:list', '/api/permissions', 'GET', '查看权限列表权限', 1),
(10, '创建权限', 'permission:create', '/api/permissions', 'POST', '创建权限权限', 1),
(11, '更新权限', 'permission:update', '/api/permissions/*', 'PUT', '更新权限权限', 1),
(12, '删除权限', 'permission:delete', '/api/permissions/*', 'DELETE', '删除权限权限', 1);

-- 插入用户 (密码: 123456)
INSERT INTO sys_user (id, username, password, nickname, email, phone, status) VALUES
(1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '系统管理员', 'admin@superdata.com', '13800000001', 1),
(2, 'user1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '普通用户1', 'user1@superdata.com', '13800000002', 1),
(3, 'user2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '普通用户2', 'user2@superdata.com', '13800000003', 1);

-- 插入API客户端 (密钥: 123456)
INSERT INTO sys_api_client (id, client_id, client_secret, client_name, description, status) VALUES
(1, 'open-api-client', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '开放API客户端', '用于第三方系统调用的API客户端', 1);

-- 分配用户角色
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 2);

-- 分配角色权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4),
(1, 5), (1, 6), (1, 7), (1, 8),
(1, 9), (1, 10), (1, 11), (1, 12);

INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(2, 1),
(2, 5),
(2, 9);
