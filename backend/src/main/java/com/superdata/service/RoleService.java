package com.superdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.superdata.common.PageResult;
import com.superdata.dto.PageQuery;
import com.superdata.dto.RoleDTO;
import com.superdata.entity.Role;

import java.util.List;

public interface RoleService extends IService<Role> {
    PageResult<Role> getPage(PageQuery query);
    List<Role> getAllRoles();
    Role createRole(RoleDTO dto);
    Role updateRole(Long id, RoleDTO dto);
    void deleteRole(Long id);
    void updateRolePermissions(Long roleId, List<Long> permissionIds);
    Role getRoleWithPermissions(Long id);
}
