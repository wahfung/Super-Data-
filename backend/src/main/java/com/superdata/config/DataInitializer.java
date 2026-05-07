package com.superdata.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.superdata.entity.ApiClient;
import com.superdata.entity.User;
import com.superdata.mapper.ApiClientMapper;
import com.superdata.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器 - 确保用户密码正确加密
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private static final String DEFAULT_PASSWORD = "123456";

    private final UserMapper userMapper;
    private final ApiClientMapper apiClientMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserMapper userMapper,
                           ApiClientMapper apiClientMapper,
                           PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.apiClientMapper = apiClientMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        logger.info("开始检查并初始化用户密码...");

        // 更新所有用户密码
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userMapper.selectList(userWrapper).forEach(user -> {
            // 检查密码是否能匹配，如果不能则更新
            if (!passwordEncoder.matches(DEFAULT_PASSWORD, user.getPassword())) {
                String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);
                user.setPassword(encodedPassword);
                userMapper.updateById(user);
                logger.info("已更新用户 {} 的密码", user.getUsername());
            }
        });

        // 更新 API 客户端密码
        LambdaQueryWrapper<ApiClient> clientWrapper = new LambdaQueryWrapper<>();
        apiClientMapper.selectList(clientWrapper).forEach(client -> {
            if (!passwordEncoder.matches(DEFAULT_PASSWORD, client.getClientSecret())) {
                String encodedSecret = passwordEncoder.encode(DEFAULT_PASSWORD);
                client.setClientSecret(encodedSecret);
                apiClientMapper.updateById(client);
                logger.info("已更新API客户端 {} 的密钥", client.getClientId());
            }
        });

        logger.info("密码初始化检查完成");
    }
}
