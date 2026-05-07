package com.superdata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.superdata.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    @Select("""
        SELECT p.* FROM sys_permission p
        JOIN sys_role_permission rp ON p.id = rp.permission_id
        WHERE rp.role_id = #{roleId}
        """)
    List<Permission> selectPermissionsByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT * FROM sys_permission WHERE permission_code = #{permissionCode}")
    Permission selectByPermissionCode(@Param("permissionCode") String permissionCode);
}
