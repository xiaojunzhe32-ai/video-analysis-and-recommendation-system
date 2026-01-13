package com.video.server.service.impl;

import com.video.server.dto.LoginRequest;
import com.video.server.dto.LoginResponse;
import com.video.server.entity.User;
import com.video.server.exception.BusinessException;
import com.video.server.mapper.UserMapper;
import com.video.server.service.AuthService;
import com.video.server.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 认证服务实现类
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    
    // 默认密码（用于测试，实际应该加密存储）
    private static final String DEFAULT_PASSWORD = "123456";
    // Token过期时间：7天
    private static final long TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;
    
    @Override
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        User user = userMapper.selectByUsername(request.getUsername());
        
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被冻结");
        }
        
        // 验证密码（简化版：实际应该使用加密盐值）
        // 这里为了简化，直接比较明文密码，实际应该使用加密后的密码比较
        if (!DEFAULT_PASSWORD.equals(request.getPassword()) && 
            !user.getPassword().equals(encryptPassword(request.getPassword(), user.getSalt()))) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        
        // 判断用户类型
        String userType = "admin".equalsIgnoreCase(request.getUsername()) ? "admin" : "user";
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), TOKEN_EXPIRE_TIME);
        
        return new LoginResponse(token, user.getId(), user.getUsername(), userType);
    }
    
    /**
     * 加密密码（简化版）
     */
    private String encryptPassword(String password, String salt) {
        if (salt == null) {
            salt = UUID.randomUUID().toString().replace("-", "");
        }
        String saltedPassword = password + salt;
        return DigestUtils.md5DigestAsHex(saltedPassword.getBytes(StandardCharsets.UTF_8));
    }
}
