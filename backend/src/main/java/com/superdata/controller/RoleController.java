package com.superdata.controller;

import com.superdata.common.PageResult;
import com.superdata.common.Result;
import com.superdata.dto.PageQuery;
import com.superdata.dto.RoleDTO;
import com.superdata.entity.Role;
import com.superdata.mapper.RolePermissionMapper;
import com.superdata.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Tag(name = "角色管理", description = "角色CRUD、权限分配等接口")
@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;
    private final RolePermissionMapper rolePermissionMapper;

    public RoleController(RoleService roleService, RolePermissionMapper rolePermissionMapper) {
        this.roleService = roleService;
        this.rolePermissionMapper = rolePermissionMapper;
    }

    @Operation(summary = "获取角色分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('role:list')")
    public Result<PageResult<Role>> list(PageQuery query) {
        PageResult<Role> result = roleService.getPage(query);
        return Result.success(result);
    }

    @Operation(summary = "获取所有角色（下拉选择用）")
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('role:list')")
    public Result<List<Role>> listAll() {
        List<Role> roles = roleService.getAllRoles();
        return Result.success(roles);
    }

    @Operation(summary = "获取角色详情（含权限）")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role:list')")
    public Result<Role> get(@PathVariable Long id) {
        Role role = roleService.getRoleWithPermissions(id);
        return Result.success(role);
    }

    @Operation(summary = "获取角色权限ID列表")
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('role:list')")
    public Result<List<Long>> getRolePermissions(@PathVariable Long id) {
        List<Long> permissionIds = rolePermissionMapper.selectPermissionIdsByRoleId(id);
        return Result.success(permissionIds);
    }

    @Operation(summary = "创建角色")
    @PostMapping
    @PreAuthorize("hasAuthority('role:create')")
    public Result<Role> create(@Valid @RequestBody RoleDTO dto) {
        Role role = roleService.createRole(dto);
        return Result.success(role, "角色创建成功");
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:update')")
    public Result<Role> update(@PathVariable Long id, @Valid @RequestBody RoleDTO dto) {
        Role role = roleService.updateRole(id, dto);
        return Result.success(role, "角色更新成功");
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success(null, "角色删除成功");
    }

    @Operation(summary = "更新角色权限")
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('role:update')")
    public Result<Void> updatePermissions(@PathVariable Long id, @RequestBody Map<String, List<Long>> request) {
        List<Long> permissionIds = request.get("permissionIds");
        roleService.updateRolePermissions(id, permissionIds);
        return Result.success(null, "角色权限更新成功");
    }
}
