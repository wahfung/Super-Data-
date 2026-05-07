package com.superdata.service;

import com.superdata.dto.LoginRequest;
import com.superdata.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse refreshToken(String refreshToken);
    LoginResponse.UserInfo getCurrentUser(String username);
}
