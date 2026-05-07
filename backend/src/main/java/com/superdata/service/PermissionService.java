package com.superdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.superdata.common.PageResult;
import com.superdata.dto.PageQuery;
import com.superdata.dto.PermissionDTO;
import com.superdata.entity.Permission;

import java.util.List;

public interface PermissionService extends IService<Permission> {
    PageResult<Permission> getPage(PageQuery query);
    List<Permission> getAllPermissions();
    Permission createPermission(PermissionDTO dto);
    Permission updatePermission(Long id, PermissionDTO dto);
    void deletePermission(Long id);
}
