package com.chat.domain.repository.userReponsitory;

import com.chat.domain.entity.user.User;
import com.chat.domain.entity.user.UserId;

public interface IUserRepository {
    void save(User user); // Lưu một tin nhắn
    User findById(UserId id);
}
