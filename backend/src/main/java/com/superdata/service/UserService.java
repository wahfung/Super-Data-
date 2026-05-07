package com.superdata.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.superdata.common.PageResult;
import com.superdata.dto.PageQuery;
import com.superdata.dto.UserDTO;
import com.superdata.entity.User;

public interface UserService extends IService<User> {
    PageResult<User> getPage(PageQuery query);
    User getByUsername(String username);
    User createUser(UserDTO dto);
    User updateUser(Long id, UserDTO dto);
    void deleteUser(Long id);
    void updateUserRoles(Long userId, java.util.List<Long> roleIds);
    void updateStatus(Long id, Integer status);
}
