package com.superdata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.superdata.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    @Select("""
        SELECT r.* FROM sys_role r
        JOIN sys_user_role ur ON r.id = ur.role_id
        WHERE ur.user_id = #{userId}
        """)
    List<Role> selectRolesByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode}")
    Role selectByRoleCode(@Param("roleCode") String roleCode);
}
