package com.superdata.controller;

import com.superdata.common.PageResult;
import com.superdata.common.Result;
import com.superdata.dto.PageQuery;
import com.superdata.dto.UserDTO;
import com.superdata.entity.User;
import com.superdata.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "对外开放API", description = "使用Basic Auth认证的对外接口")
@RestController
@RequestMapping("/open-api/v1")
public class OpenApiController {

    private final UserService userService;

    public OpenApiController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "健康检查")
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", System.currentTimeMillis());
        data.put("service", "super-data");
        return Result.success(data);
    }

    @Operation(summary = "获取用户列表")
    @GetMapping("/users")
    public Result<PageResult<User>> listUsers(PageQuery query) {
        PageResult<User> result = userService.getPage(query);
        return Result.success(result);
    }

    @Operation(summary = "创建用户")
    @PostMapping("/users")
    public Result<User> createUser(@Valid @RequestBody UserDTO dto) {
        User user = userService.createUser(dto);
        return Result.success(user, "用户创建成功");
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/users/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }
}
