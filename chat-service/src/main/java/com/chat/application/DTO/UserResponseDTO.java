package com.chat.application.DTO;

import com.chat.domain.entity.user.User;
import com.chat.domain.entity.user.UserStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDTO {
    private String id;
    private String userName;
    private String email;
    private String phoneNumber;
    private UserStatus status;
    private boolean emailVerified;

    public static UserResponseDTO fromUser(User user) {
        return UserResponseDTO.builder()
                .id(user.getId().getValue())
                .userName(user.getUserName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .build();
    }
}