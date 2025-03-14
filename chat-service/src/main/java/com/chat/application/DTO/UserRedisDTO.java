package com.chat.application.DTO;

import com.chat.domain.entity.user.User;
import com.chat.domain.entity.user.UserId;
import com.chat.domain.entity.user.UserStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRedisDTO {
    private String id;
    private String userName;
    private String email;
    private String phoneNumber;
    private String password;
    private UserStatus status;

    public static UserRedisDTO fromDomain(User user) {
        return UserRedisDTO.builder()
                .id(user.getId().getValue())
                .userName(user.getUserName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .password(user.getPassword())
                .status(user.getStatus())
                .build();
    }

    public User toDomain() {
        return User.builder()
                .id(UserId.fromString(this.id))
                .userName(this.userName)
                .email(this.email)
                .phoneNumber(this.phoneNumber)
                .password(this.password)
                .status(this.status)
                .build();
    }
}


