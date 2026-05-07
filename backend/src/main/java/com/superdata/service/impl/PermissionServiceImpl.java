package com.superdata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.superdata.common.PageResult;
import com.superdata.dto.PageQuery;
import com.superdata.dto.PermissionDTO;
import com.superdata.entity.Permission;
import com.superdata.exception.BusinessException;
import com.superdata.mapper.PermissionMapper;
import com.superdata.service.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);

    private final PermissionMapper permissionMapper;

    public PermissionServiceImpl(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    @Override
    public PageResult<Permission> getPage(PageQuery query) {
        Page<Permission> page = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(Permission::getPermissionName, query.getKeyword())
                    .or()
                    .like(Permission::getPermissionCode, query.getKeyword());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Permission::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(Permission::getCreatedAt);

        Page<Permission> result = permissionMapper.selectPage(page, wrapper);
        return PageResult.of(result);
    }

    @Override
    public List<Permission> getAllPermissions() {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getStatus, 1);
        wrapper.orderByAsc(Permission::getId);
        return permissionMapper.selectList(wrapper);
    }

    @Override
    public Permission createPermission(PermissionDTO dto) {
        logger.info("Creating permission: {}", dto.getPermissionCode());

        Permission existing = permissionMapper.selectByPermissionCode(dto.getPermissionCode());
        if (existing != null) {
            throw new BusinessException("权限编码已存在");
        }

        Permission permission = new Permission();
        permission.setPermissionName(dto.getPermissionName());
        permission.setPermissionCode(dto.getPermissionCode());
        permission.setApiPath(dto.getApiPath());
        permission.setMethod(dto.getMethod());
        permission.setDescription(dto.getDescription());
        permission.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        permissionMapper.insert(permission);

        logger.info("Permission created successfully: {}", permission.getId());
        return permission;
    }

    @Override
    public Permission updatePermission(Long id, PermissionDTO dto) {
        logger.info("Updating permission: {}", id);

        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }

        if (!permission.getPermissionCode().equals(dto.getPermissionCode())) {
            Permission existing = permissionMapper.selectByPermissionCode(dto.getPermissionCode());
            if (existing != null) {
                throw new BusinessException("权限编码已存在");
            }
            permission.setPermissionCode(dto.getPermissionCode());
        }

        permission.setPermissionName(dto.getPermissionName());
        permission.setApiPath(dto.getApiPath());
        permission.setMethod(dto.getMethod());
        permission.setDescription(dto.getDescription());
        if (dto.getStatus() != null) {
            permission.setStatus(dto.getStatus());
        }

        permissionMapper.updateById(permission);

        logger.info("Permission updated successfully: {}", id);
        return permission;
    }

    @Override
    public void deletePermission(Long id) {
        logger.info("Deleting permission: {}", id);

        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }

        permissionMapper.deleteById(id);

        logger.info("Permission deleted successfully: {}", id);
    }
}
