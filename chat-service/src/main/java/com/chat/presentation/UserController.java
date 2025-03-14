package com.chat.presentation;

import com.chat.application.DTO.UserResponseDTO;
import com.chat.domain.entity.user.User;
import com.chat.domain.entity.user.UserStatus;
import com.chat.domain.service.userservice.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

//    @GetMapping("/")
//    // @PreAuthorize("isAuthenticated()")
//    public ResponseEntity<List<User>> getAllUsers() {
//        log.debug("Fetching all users");
//        try {
//            List<User> users = userService.getAllUsers();
//            log.debug("Found {} users", users.size());
//            return ResponseEntity.ok(users);
//        } catch (Exception e) {
//            log.error("Error fetching users", e);
//            throw e;
//        }
//    }
//
//    @GetMapping("/{userId}")
//    // @PreAuthorize("isAuthenticated()")
//    public ResponseEntity<User> getUserProfile(@PathVariable String userId) {
//        User user = userService.getUserById(userId);
//        return ResponseEntity.ok(user);
//    }
//
//    @PutMapping("/{userId}/status")
//    // @PreAuthorize("isAuthenticated()")
//    public ResponseEntity<?> updateUserStatus(
//            @PathVariable String userId,
//            @RequestBody UserStatus status) {
//        userService.updateUserStatus(userId, status);
//        return ResponseEntity.ok().build();
//    }

    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        log.debug("Fetching all users");
        try {
            List<UserResponseDTO> users = userService.getAllUsers()
                    .stream()
                    .map(UserResponseDTO::fromUser)
                    .collect(Collectors.toList());
            log.debug("Found {} users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error fetching users", e);
            throw e;
        }
    }

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDTO> getUserProfile(@PathVariable String userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(UserResponseDTO.fromUser(user));
    }


    @PutMapping("/{userId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable String userId,
            @RequestBody UserStatus status) {
        userService.updateUserStatus(userId, status);
        return ResponseEntity.ok().build();
    }
}
