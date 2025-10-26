package com.telconova.suportsuite.security;

import com.telconova.suportsuite.DTO.AuthResponse;
import com.telconova.suportsuite.DTO.LoginRequest;

public interface IAuthService {
    AuthResponse login(LoginRequest request, String ipAddress);
}
