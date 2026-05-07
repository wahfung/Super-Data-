package com.superdata.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_api_client")
public class ApiClient {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String clientId;

    private String clientSecret;

    private String clientName;

    private String description;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
