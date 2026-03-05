package com.duo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.duo.dto.LoginDTO;
import com.duo.dto.RegisterDTO;
import com.duo.entity.User;
import com.duo.mapper.UserMapper;
import com.duo.service.UserService;
import com.duo.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void register(RegisterDTO registerDTO) {
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, registerDTO.getUsername()));
        if (count > 0) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : "User_" + System.currentTimeMillis() % 10000);
        // Generate a 6-character random invite code
        user.setInviteCode(generateUniqueInviteCode());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        baseMapper.insert(user);
    }

    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        User user = baseMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, loginDTO.getUsername()));

        if (user == null || !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return result;
    }

    @Override
    public User getUserInfo(Long userId) {
        User user = baseMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null); // Do not return password
        }
        return user;
    }

    private String generateUniqueInviteCode() {
        while (true) {
            // Generates a simple 6 char alphanumeric string
            String code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            Long count = baseMapper.selectCount(new LambdaQueryWrapper<User>()
                    .eq(User::getInviteCode, code));
            if (count == 0) {
                return code;
            }
        }
    }
}
