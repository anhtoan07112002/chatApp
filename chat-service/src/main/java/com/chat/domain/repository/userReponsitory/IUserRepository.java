package com.chat.domain.repository.userReponsitory;


import java.util.Optional;
import java.util.UUID;
import com.chat.domain.entity.user.User;

public interface IUserRepository {
    void save(User user);
    User findById(UUID id);
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
