package com.chat.domain.entity.membership;

import java.time.LocalDateTime;

import com.chat.domain.entity.group.GroupId;
import com.chat.domain.entity.group.GroupRole;
import com.chat.domain.entity.user.UserId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

@Data
@AllArgsConstructor
@Builder
public class GroupMemberShip {
    @Getter
    @NonNull
    private GroupMemberShipId id;
    private GroupId groupId;
    private UserId userId;
    private GroupRole role;
    private LocalDateTime joinedAt;
    private UserId invitedBy;
    private GroupMemberShipStatus status;
}
