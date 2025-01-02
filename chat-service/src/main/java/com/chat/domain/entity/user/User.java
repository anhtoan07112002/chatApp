package com.chat.domain.entity.user;

import com.chat.domain.entity.group.set;
import com.chat.domain.entity.membership.GroupMemberShip;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
/**
 * The type User.
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @NonNull
    @Getter
    private UserId id;
    private String userName;
    private String email;
    private String phoneNumber;
    private UserStatus status;
    private set<GroupMemberShip> groups;

    /**
     * Update profie.
     *
     * @param newUsername    the new username
     * @param newEmail       the new email
     * @param newPhoneNumber the new phone number
     */
    public void UpdateProfie(String newUsername, String newEmail, String newPhoneNumber) {
        if (newUsername != null) {
            this.setUserName(newUsername);
        }
        if (newEmail != null) {
            this.setUserName(newEmail);
        }
        if (newPhoneNumber != null) {
            this.setUserName(newPhoneNumber);
        }
    }

    /**
     * Block user.
     *
     * @param userId the user id
     */
    public void markAsOnline() {
        this.setStatus(UserStatus.ONLINE);
    }

    public void markAsOffline() {
        this.setStatus(UserStatus.OFFLINE);
    }

    public void markAsAway() {
        this.setStatus(UserStatus.AWAY);
    }

    public void markAsDisturnb() {
        this.setStatus(UserStatus.DO_NOT_DISTURB);
    }
}
