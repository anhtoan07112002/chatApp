package com.chat.infrastructure.security;

import com.chat.domain.entity.user.User;
import com.chat.domain.entity.user.UserStatus;
import com.chat.domain.exception.userException.UserNotFoundException;
import com.chat.domain.service.userservice.IAuthService;
import com.chat.domain.service.userservice.IUserService;
import com.chat.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {
    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public User register(String userName, String email, String password, String phoneNumber) {
        // Check if user already exists
        try {
            User existingUser = userService.getUserByEmail(email);
            if (existingUser != null) {
                throw new IllegalArgumentException("Email already registered");
            }
        } catch (UserNotFoundException e) {
            // User does not exist, proceed with registration
        }

        // Create new user
        return userService.createUser(userName, email, password, phoneNumber);
    }

    @Override
    public User authenticate(String email, String password) {
        User user = userService.getUserByEmail(email);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // Update user status to online
        userService.updateUserStatus(user.getId().value().toString(), UserStatus.ONLINE);

        return user;
    }

    @Override
    public String generateToken(User user) {
        return jwtService.generateToken(user);
    }
}
