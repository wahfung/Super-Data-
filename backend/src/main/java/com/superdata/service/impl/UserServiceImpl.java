package com.superdata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.superdata.common.PageResult;
import com.superdata.dto.PageQuery;
import com.superdata.dto.UserDTO;
import com.superdata.entity.User;
import com.superdata.entity.UserRole;
import com.superdata.exception.BusinessException;
import com.superdata.mapper.UserMapper;
import com.superdata.mapper.UserRoleMapper;
import com.superdata.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper,
                           UserRoleMapper userRoleMapper,
                           PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageResult<User> getPage(PageQuery query) {
        Page<User> page = new Page<>(query.getPage(), query.getSize());

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(User::getUsername, query.getKeyword())
                    .or()
                    .like(User::getNickname, query.getKeyword())
                    .or()
                    .like(User::getEmail, query.getKeyword());
        }
        if (query.getStatus() != null) {
            wrapper.eq(User::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> result = userMapper.selectPage(page, wrapper);

        result.getRecords().forEach(user -> {
            user.setPassword(null);
            List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(user.getId());
            List<String> roles = userMapper.selectRoleCodesByUserId(user.getId());
            user.setPermissions(userMapper.selectPermissionsByUserId(user.getId()));
        });

        return PageResult.of(result);
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    @Transactional
    public User createUser(UserDTO dto) {
        logger.info("Creating user: {}", dto.getUsername());

        User existing = userMapper.selectByUsername(dto.getUsername());
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setAvatar(dto.getAvatar());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        userMapper.insert(user);

        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            updateUserRoles(user.getId(), dto.getRoleIds());
        }

        user.setPassword(null);
        logger.info("User created successfully: {}", user.getId());
        return user;
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserDTO dto) {
        logger.info("Updating user: {}", id);

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!user.getUsername().equals(dto.getUsername())) {
            User existing = userMapper.selectByUsername(dto.getUsername());
            if (existing != null) {
                throw new BusinessException("用户名已存在");
            }
            user.setUsername(dto.getUsername());
        }

        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setAvatar(dto.getAvatar());
        if (dto.getStatus() != null) {
            user.setStatus(dto.getStatus());
        }

        userMapper.updateById(user);

        if (dto.getRoleIds() != null) {
            updateUserRoles(id, dto.getRoleIds());
        }

        user.setPassword(null);
        logger.info("User updated successfully: {}", id);
        return user;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        logger.info("Deleting user: {}", id);

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("不能删除管理员账户");
        }

        userRoleMapper.deleteByUserId(id);
        userMapper.deleteById(id);

        logger.info("User deleted successfully: {}", id);
    }

    @Override
    @Transactional
    public void updateUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteByUserId(userId);

        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRoleMapper.insert(userRole);
            }
        }
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        logger.info("Updating user status: {} -> {}", id, status);

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if ("admin".equals(user.getUsername()) && status == 0) {
            throw new BusinessException("不能禁用管理员账户");
        }

        user.setStatus(status);
        userMapper.updateById(user);

        logger.info("User status updated successfully: {}", id);
    }
}
