package com.chat.infrastructure.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Value;

import com.chat.domain.entity.user.User;
import com.chat.domain.entity.user.UserId;
import com.chat.domain.repository.userReponsitory.IUserRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Repository
public class TextFileUserRepository implements IUserRepository {

    private final File file;
    private final Gson gson;

    public TextFileUserRepository(@Value("${user.storage.path:users.json}") String filePath) {
        this.file = new File(filePath);
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo file", e);
        }
    }

    private List<User> loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<User> users = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if(!line.isBlank()) {
                    User user = gson.fromJson(line, User.class);
                    users.add(user);
                }
            }
            return users;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc file", e);
        }
    }

    private void saveUsers(List<User> users) {
        try (FileWriter writer = new FileWriter(file)) {
            for (User user : users) {
                writer.write(gson.toJson(user) + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Lỗi ghi file", e);
        }
    }
    
    @Override
    public void save(User user) {
        List<User> users = loadUsers();
        // Kiểm tra xem user đã tồn tại chưa
        users.removeIf(u -> u.getId().equals(user.getId()));
        users.add(user);
        saveUsers(users);
    }

    @Override
    public User findById(UserId id) {
        return loadUsers().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}