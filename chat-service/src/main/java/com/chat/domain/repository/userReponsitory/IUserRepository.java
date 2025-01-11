package com.chat.domain.repository.userReponsitory;

import java.util.UUID;

import com.chat.domain.entity.user.User;
// import com.chat.domain.entity.user.UserId;

public interface IUserRepository {
    void save(User user); // Lưu một tin nhắn
    User findById(UUID id);
}
