package com.duo.controller;

import com.duo.common.R;
import com.duo.dto.LoginDTO;
import com.duo.dto.RegisterDTO;
import com.duo.entity.User;
import com.duo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public R<String> register(@RequestBody RegisterDTO registerDTO) {
        try {
            userService.register(registerDTO);
            return R.success("Registered successfully");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        try {
            Map<String, Object> result = userService.login(loginDTO);
            return R.success(result);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @GetMapping("/info/{id}")
    public R<User> getInfo(@PathVariable Long id) {
        try {
            User user = userService.getUserInfo(id);
            if (user != null) {
                return R.success(user);
            }
            return R.error("User not found");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @PostMapping("/updateAvatar")
    public R<String> updateAvatar(@RequestParam Long userId, @RequestParam String avatarUrl) {
        try {
            User user = userService.getById(userId);
            if (user != null) {
                user.setAvatar(avatarUrl);
                userService.updateById(user);
                return R.success("Upload avatar successfully");
            }
            return R.error("User not found");
        } catch (Exception e) {
            return R.error("Update avatar failed: " + e.getMessage());
        }
    }

    // TODO: A real application will get user ID from JWT Token, e.g., via SecurityContextHolder
    // We provide a quick way for now
}
