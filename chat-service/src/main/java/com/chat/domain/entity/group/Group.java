package com.chat.domain.entity.group;
import com.chat.domain.entity.membership.GroupMemberShip;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Group {
    @Getter
    @NonNull
    private GroupId id;
    @NonNull
    private String name;
    private String description;
    private String avatarUrl;
    private GroupType status;
    private set<GroupMemberShip> members;

    public void updateGroup(String name, String description, String avatarUrl) {
        if (name != null) {
            this.setName(name);
        }
        if (description != null) {
            this.setDescription(description);
        }
        if (avatarUrl != null) {
            this.setAvatarUrl(avatarUrl);
        }
    }

    public void markpublic() {
        this.setStatus(GroupType.PUBLIC);
    }

    public void markPrivate() {
        this.setStatus(GroupType.PRIVATE);
    }
}
