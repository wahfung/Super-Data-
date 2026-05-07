package com.superdata.controller;

import com.superdata.common.PageResult;
import com.superdata.common.Result;
import com.superdata.dto.PageQuery;
import com.superdata.dto.UserDTO;
import com.superdata.entity.User;
import com.superdata.mapper.UserRoleMapper;
import com.superdata.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Tag(name = "用户管理", description = "用户CRUD、角色分配等接口")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRoleMapper userRoleMapper;

    public UserController(UserService userService, UserRoleMapper userRoleMapper) {
        this.userService = userService;
        this.userRoleMapper = userRoleMapper;
    }

    @Operation(summary = "获取用户分页列表")
    @GetMapping
    @PreAuthorize("hasAuthority('user:list')")
    public Result<PageResult<User>> list(PageQuery query) {
        PageResult<User> result = userService.getPage(query);
        return Result.success(result);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:list')")
    public Result<User> get(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
            List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(id);
            user.setRoles(null);
        }
        return Result.success(user);
    }

    @Operation(summary = "获取用户角色ID列表")
    @GetMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('user:list')")
    public Result<List<Long>> getUserRoles(@PathVariable Long id) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(id);
        return Result.success(roleIds);
    }

    @Operation(summary = "创建用户")
    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    public Result<User> create(@Valid @RequestBody UserDTO dto) {
        User user = userService.createUser(dto);
        return Result.success(user, "用户创建成功");
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    public Result<User> update(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        User user = userService.updateUser(id, dto);
        return Result.success(user, "用户更新成功");
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success(null, "用户删除成功");
    }

    @Operation(summary = "更新用户角色")
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('user:update')")
    public Result<Void> updateRoles(@PathVariable Long id, @RequestBody Map<String, List<Long>> request) {
        List<Long> roleIds = request.get("roleIds");
        userService.updateUserRoles(id, roleIds);
        return Result.success(null, "用户角色更新成功");
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('user:update')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        Integer status = request.get("status");
        userService.updateStatus(id, status);
        return Result.success(null, "用户状态更新成功");
    }
}
