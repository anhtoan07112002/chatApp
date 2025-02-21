package com.chat.domain.entity.user;

// import java.time.LocalDateTime;

import com.chat.domain.entity.membership.GroupMemberShip;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @NonNull
    private UserId id;
    private String userName;
    private String email;
    private String phoneNumber;
    private String password;
    private UserStatus status;
    private Set<GroupMemberShip> groups;

    // Factory method for creating new users
    public static User create(String userName, String email, String phoneNumber, String password) {
        return User.builder()
                .id(UserId.generate())
                .userName(userName)
                .email(email)
                .phoneNumber(phoneNumber)
                .password(password)
                .status(UserStatus.OFFLINE)
                .groups(new HashSet<>())
                .build();
    }

    // Domain methods
    public void updateProfile(String newUsername, String newEmail, String newPhoneNumber) {
        if (newUsername != null && !newUsername.trim().isEmpty()) {
            this.userName = newUsername;
        }
        if (newEmail != null && !newEmail.trim().isEmpty()) {
            this.email = newEmail;
        }
        if (newPhoneNumber != null && !newPhoneNumber.trim().isEmpty()) {
            this.phoneNumber = newPhoneNumber;
        }
    }

    public void joinGroup(GroupMemberShip membership) {
        if (this.groups == null) {
            this.groups = new HashSet<>();
        }
        this.groups.add(membership);
    }

    public void leaveGroup(String groupId) {
        if (this.groups != null) {
            this.groups.removeIf(membership ->
                    membership.getGroupId().id().toString().equals(groupId));
        }
    }

    // Status management methods
    public void markAsOnline() {
        this.status = UserStatus.ONLINE;
    }

    public void markAsOffline() {
        this.status = UserStatus.OFFLINE;
    }

    public void markAsAway() {
        this.status = UserStatus.AWAY;
    }

    public void markAsDoNotDisturb() {
        this.status = UserStatus.DO_NOT_DISTURB;
    }

    // Validation methods
    public boolean isValidEmail() {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public boolean isValidPhoneNumber() {
        return phoneNumber != null && phoneNumber.matches("^\\+?[1-9]\\d{1,14}$");
    }

    public boolean isValidUsername() {
        return userName != null && userName.length() >= 3 && userName.length() <= 50;
    }
}
