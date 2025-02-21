package com.chat.domain.service.userservice;

import com.chat.domain.entity.user.User;

public interface IAuthService {
    User register(String userName, String email, String password, String phoneNumber);
    User authenticate(String email, String password);
    String generateToken(User user);
}
