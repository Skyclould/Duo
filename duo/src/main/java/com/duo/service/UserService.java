package com.duo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.duo.dto.LoginDTO;
import com.duo.dto.RegisterDTO;
import com.duo.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {

    void register(RegisterDTO registerDTO);

    Map<String, Object> login(LoginDTO loginDTO);

    User getUserInfo(Long userId);
}
