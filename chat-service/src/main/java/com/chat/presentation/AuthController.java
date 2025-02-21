package com.chat.presentation;

import com.chat.application.DTO.AuthRequest;
import com.chat.application.DTO.AuthResponse;
import com.chat.application.DTO.RegisterRequest;
import com.chat.domain.entity.user.User;
import com.chat.domain.service.userservice.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        User user = authService.register(
                request.getUserName(),
                request.getEmail(),
                request.getPassword(),
                request.getPhoneNumber()
        );

        String token = authService.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .userId(user.getId().value().toString())
                .userName(user.getUserName())
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        User user = authService.authenticate(request.getEmail(), request.getPassword());
        String token = authService.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .userId(user.getId().value().toString())
                .userName(user.getUserName())
                .build());
    }
}