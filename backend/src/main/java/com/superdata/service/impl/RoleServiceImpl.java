package com.superdata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.superdata.common.PageResult;
import com.superdata.dto.PageQuery;
import com.superdata.dto.RoleDTO;
import com.superdata.entity.Permission;
import com.superdata.entity.Role;
import com.superdata.entity.RolePermission;
import com.superdata.exception.BusinessException;
import com.superdata.mapper.PermissionMapper;
import com.superdata.mapper.RoleMapper;
import com.superdata.mapper.RolePermissionMapper;
import com.superdata.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    public RoleServiceImpl(RoleMapper roleMapper,
                           RolePermissionMapper rolePermissionMapper,
                           PermissionMapper permissionMapper) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public PageResult<Role> getPage(PageQuery query) {
        Page<Role> page = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(Role::getRoleName, query.getKeyword())
                    .or()
                    .like(Role::getRoleCode, query.getKeyword());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Role::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(Role::getCreatedAt);

        Page<Role> result = roleMapper.selectPage(page, wrapper);

        result.getRecords().forEach(role -> {
            List<Permission> permissions = permissionMapper.selectPermissionsByRoleId(role.getId());
            role.setPermissions(permissions);
        });

        return PageResult.of(result);
    }

    @Override
    public List<Role> getAllRoles() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getStatus, 1);
        wrapper.orderByAsc(Role::getId);
        return roleMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public Role createRole(RoleDTO dto) {
        logger.info("Creating role: {}", dto.getRoleCode());

        Role existing = roleMapper.selectByRoleCode(dto.getRoleCode());
        if (existing != null) {
            throw new BusinessException("角色编码已存在");
        }

        Role role = new Role();
        role.setRoleName(dto.getRoleName());
        role.setRoleCode(dto.getRoleCode());
        role.setDescription(dto.getDescription());
        role.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        roleMapper.insert(role);

        if (dto.getPermissionIds() != null && !dto.getPermissionIds().isEmpty()) {
            updateRolePermissions(role.getId(), dto.getPermissionIds());
        }

        logger.info("Role created successfully: {}", role.getId());
        return role;
    }

    @Override
    @Transactional
    public Role updateRole(Long id, RoleDTO dto) {
        logger.info("Updating role: {}", id);

        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        if (!role.getRoleCode().equals(dto.getRoleCode())) {
            Role existing = roleMapper.selectByRoleCode(dto.getRoleCode());
            if (existing != null) {
                throw new BusinessException("角色编码已存在");
            }
            role.setRoleCode(dto.getRoleCode());
        }

        role.setRoleName(dto.getRoleName());
        role.setDescription(dto.getDescription());
        if (dto.getStatus() != null) {
            role.setStatus(dto.getStatus());
        }

        roleMapper.updateById(role);

        if (dto.getPermissionIds() != null) {
            updateRolePermissions(id, dto.getPermissionIds());
        }

        logger.info("Role updated successfully: {}", id);
        return role;
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        logger.info("Deleting role: {}", id);

        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        if ("ADMIN".equals(role.getRoleCode())) {
            throw new BusinessException("不能删除超级管理员角色");
        }

        rolePermissionMapper.deleteByRoleId(id);
        roleMapper.deleteById(id);

        logger.info("Role deleted successfully: {}", id);
    }

    @Override
    @Transactional
    public void updateRolePermissions(Long roleId, List<Long> permissionIds) {
        rolePermissionMapper.deleteByRoleId(roleId);

        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                rolePermissionMapper.insert(rp);
            }
        }
    }

    @Override
    public Role getRoleWithPermissions(Long id) {
        Role role = roleMapper.selectById(id);
        if (role != null) {
            List<Permission> permissions = permissionMapper.selectPermissionsByRoleId(id);
            role.setPermissions(permissions);
        }
        return role;
    }
}
