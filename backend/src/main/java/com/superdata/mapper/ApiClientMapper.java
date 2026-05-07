package com.superdata.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.superdata.entity.ApiClient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ApiClientMapper extends BaseMapper<ApiClient> {

    @Select("SELECT * FROM sys_api_client WHERE client_id = #{clientId} AND status = 1")
    ApiClient selectByClientId(@Param("clientId") String clientId);
}
