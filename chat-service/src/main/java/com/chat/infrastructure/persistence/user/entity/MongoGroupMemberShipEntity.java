package com.chat.infrastructure.persistence.user.entity;

import com.chat.domain.entity.group.GroupId;
import com.chat.domain.entity.group.GroupRole;
import com.chat.domain.entity.membership.GroupMemberShip;
import com.chat.domain.entity.membership.GroupMemberShipId;
import com.chat.domain.entity.membership.GroupMemberShipStatus;
import com.chat.domain.entity.user.UserId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MongoGroupMemberShipEntity {
    private String id;
    private String groupId;
    private String userId;
    private GroupRole role;
    private LocalDateTime joinedAt;
    private String invitedBy;
    private GroupMemberShipStatus status;
    public static MongoGroupMemberShipEntity fromDomain(GroupMemberShip groupMemberShip) {
        return MongoGroupMemberShipEntity.builder()
                .id(groupMemberShip.getId().id() != null ? groupMemberShip.getId().id().toString() : null)
                .groupId(groupMemberShip.getGroupId().id() != null ? groupMemberShip.getGroupId().id().toString() : null)
                .userId(groupMemberShip.getUserId().getValue() != null ? groupMemberShip.getUserId().getValue() : null)
                .role(groupMemberShip.getRole())
                .joinedAt(groupMemberShip.getJoinedAt())
                .invitedBy(groupMemberShip.getInvitedBy() != null ? String.valueOf(groupMemberShip.getInvitedBy().getValue()) : null)
                .status(groupMemberShip.getStatus())
                .build();
    }

    public static GroupMemberShip toDomain(MongoGroupMemberShipEntity entity) {
        return GroupMemberShip.builder()
                .id(entity.getId() != null ? new GroupMemberShipId(entity.getId()) : null)
                .groupId(entity.getGroupId() != null ? new GroupId(entity.getGroupId()) : null)
                .userId(entity.getUserId() != null ? UserId.fromString(entity.getUserId()) : null)
                .role(entity.getRole())
                .joinedAt(entity.getJoinedAt())
                .invitedBy(entity.getInvitedBy() != null ? UserId.fromString(entity.getInvitedBy()) : null)
                .status(entity.getStatus())
                .build();
    }
}
