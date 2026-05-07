package com.superdata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.superdata.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT u.* FROM sys_user u WHERE u.username = #{username}")
    User selectByUsername(@Param("username") String username);

    @Select("""
        SELECT DISTINCT p.permission_code
        FROM sys_permission p
        JOIN sys_role_permission rp ON p.id = rp.permission_id
        JOIN sys_user_role ur ON rp.role_id = ur.role_id
        WHERE ur.user_id = #{userId} AND p.status = 1
        """)
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    @Select("""
        SELECT r.role_code
        FROM sys_role r
        JOIN sys_user_role ur ON r.id = ur.role_id
        WHERE ur.user_id = #{userId} AND r.status = 1
        """)
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}
