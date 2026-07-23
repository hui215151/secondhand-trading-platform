package com.secondhand.service;

import com.secondhand.dto.LoginDTO;
import com.secondhand.entity.User;

public interface UserService {
    String login(LoginDTO loginDTO);
    void register(User user);
    User getUserInfo(Long userId);
}