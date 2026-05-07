package com.superdata.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class PermissionDTO {

    private Long id;

    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称长度不能超过50个字符")
    private String permissionName;

    @NotBlank(message = "权限编码不能为空")
    @Size(max = 50, message = "权限编码长度不能超过50个字符")
    private String permissionCode;

    @Size(max = 200, message = "API路径长度不能超过200个字符")
    private String apiPath;

    @Size(max = 10, message = "HTTP方法长度不能超过10个字符")
    private String method;

    @Size(max = 200, message = "权限描述长度不能超过200个字符")
    private String description;

    private Integer status;
}
