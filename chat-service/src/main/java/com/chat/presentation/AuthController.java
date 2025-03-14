package com.chat.presentation;

import com.chat.application.DTO.AuthRequest;
import com.chat.application.DTO.AuthResponse;
import com.chat.application.DTO.RegisterRequest;
import com.chat.domain.entity.user.User;
import com.chat.domain.service.userservice.IAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
//public class AuthController {
//    private final IAuthService authService;
//
//    @PostMapping("/register")
//    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
//        User user = authService.register(
//                request.getUserName(),
//                request.getEmail(),
//                request.getPassword(),
//                request.getPhoneNumber()
//        );
//
//        String token = authService.generateToken(user);
//
//        return ResponseEntity.ok(AuthResponse.builder()
//                .token(token)
//                .userId(user.getId().value().toString())
//                .userName(user.getUserName())
//                .build());
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
//        User user = authService.authenticate(request.getEmail(), request.getPassword());
//        String token = authService.generateToken(user);
//
//        return ResponseEntity.ok(AuthResponse.builder()
//                .token(token)
//                .userId(user.getId().value().toString())
//                .userName(user.getUserName())
//                .build());
//    }
//}
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        log.debug("Received register request: {}", request);
        User user = authService.register(
                request.getUserName(),
                request.getEmail(),
                request.getPassword(),
                request.getPhoneNumber()
        );

        String token = authService.generateToken(user);
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .userId(user.getId().getValue())
                .userName(user.getUserName())
                .build();

        log.debug("Register response: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        log.debug("Received login request for email: {}", request.getEmail());
        User user = authService.authenticate(request.getEmail(), request.getPassword());
        String token = authService.generateToken(user);

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .userId(user.getId().getValue())
                .userName(user.getUserName())
                .build();

        log.debug("Login response: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String token,
            Authentication authentication) {
        log.debug("Processing logout request");

        try {
            // Lấy thông tin user từ authentication
            String email = authentication.getName();
            authService.logout(email, token.substring(7)); // Bỏ "Bearer " prefix

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error during logout", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}