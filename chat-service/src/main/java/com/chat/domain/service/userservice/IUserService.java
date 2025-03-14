package com.chat.domain.service.userservice;

import com.chat.domain.entity.user.User;
import com.chat.domain.entity.user.UserStatus;

import java.util.List;
// import com.chat.domain.entity.user.UserId;

public interface IUserService {
    User createUser(String name, String email, String password, String phoneNumber); // Create a user
    User getUserById(String id); // Get a user
    boolean isOnline(String id); // Check if a user is online
    void updateUserStatus(String userId, UserStatus status);

    User getUserByEmail(String email);
    List<User> getAllUsers();

//    void savaUser(User user);
}
