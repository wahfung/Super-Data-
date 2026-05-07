package com.superdata.controller;

import com.superdata.common.PageResult;
import com.superdata.common.Result;
import com.superdata.dto.PageQuery;
import com.superdata.dto.PermissionDTO;
import com.superdata.entity.Permission;
import com.superdata.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "权限管理", description = "权限CRUD接口")
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Operation(summary = "获取权限分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('permission:list')")
    public Result<PageResult<Permission>> list(PageQuery query) {
        PageResult<Permission> result = permissionService.getPage(query);
        return Result.success(result);
    }

    @Operation(summary = "获取所有权限（下拉选择用）")
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('permission:list')")
    public Result<List<Permission>> listAll() {
        List<Permission> permissions = permissionService.getAllPermissions();
        return Result.success(permissions);
    }

    @Operation(summary = "获取权限详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:list')")
    public Result<Permission> get(@PathVariable Long id) {
        Permission permission = permissionService.getById(id);
        return Result.success(permission);
    }

    @Operation(summary = "创建权限")
    @PostMapping
    @PreAuthorize("hasAuthority('permission:create')")
    public Result<Permission> create(@Valid @RequestBody PermissionDTO dto) {
        Permission permission = permissionService.createPermission(dto);
        return Result.success(permission, "权限创建成功");
    }

    @Operation(summary = "更新权限")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:update')")
    public Result<Permission> update(@PathVariable Long id, @Valid @RequestBody PermissionDTO dto) {
        Permission permission = permissionService.updatePermission(id, dto);
        return Result.success(permission, "权限更新成功");
    }

    @Operation(summary = "删除权限")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return Result.success(null, "权限删除成功");
    }
}
