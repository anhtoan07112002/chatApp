package com.chat.infrastructure.persistence.user.entity;

import com.chat.domain.entity.user.User;
import com.chat.domain.entity.user.UserId;
import com.chat.domain.entity.user.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Document(collection = "users")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MongoUserEntity {
    @Id
    private String id;

    private String userName;

    @Indexed(unique = true)
    private String email;

    private String phoneNumber;
    private String password;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime updatedAt;
    private boolean emailVerified;
    private Set<MongoGroupMemberShipEntity> groups;

    public static MongoUserEntity fromDomain(User user) {
        return MongoUserEntity.builder()
                .id(user.getId().getValue() != null ? user.getId().getValue() : null)
                .userName(user.getUserName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .password(user.getPassword())
                .status(user.getStatus())
                .groups(user.getGroups() != null
                        ? user.getGroups().stream()
                        .map(MongoGroupMemberShipEntity::fromDomain)
                        .collect(Collectors.toSet())
                        : new HashSet<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .emailVerified(false)
                .build();
    }

    public User toDomain() {
        return User.builder()
                .id(this.id != null ? UserId.fromString(this.id) : null)  // Sử dụng phương thức static fromString
                .userName(this.userName)
                .email(this.email)
                .phoneNumber(this.phoneNumber)
                .password(this.password)
                .status(this.status)
                .groups(this.groups != null
                        ? this.groups.stream()
                        .map(MongoGroupMemberShipEntity::toDomain)
                        .collect(Collectors.toSet())
                        : new HashSet<>())
                .build();
    }

    public void updateLoginTime() {
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateEmailVerification(boolean verified) {
        this.emailVerified = verified;
        this.updatedAt = LocalDateTime.now();
    }
}
