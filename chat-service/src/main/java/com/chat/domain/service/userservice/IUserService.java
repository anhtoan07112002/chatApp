package com.chat.domain.service.userservice;

import com.chat.domain.entity.user.User;
import com.chat.domain.entity.user.UserId;

public interface IUserService {
    User createUser(String name, String email, String password); // Create a user
    User getUserById(UserId id); // Get a user
    boolean isOnline(UserId id); // Check if a user is online
}
