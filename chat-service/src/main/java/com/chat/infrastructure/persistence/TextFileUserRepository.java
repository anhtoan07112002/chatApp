package com.chat.infrastructure.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Value;

import com.chat.domain.entity.user.User;
import com.chat.domain.repository.userReponsitory.IUserRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

@Repository
public class TextFileUserRepository implements IUserRepository {

    private final File file;
    private final Gson gson;

    public TextFileUserRepository(@Value("${user.storage.path:users.json}") String filePath) {
        this.file = new File(filePath);
        this.gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(UUID.class, new TypeAdapter<UUID>() {
            @Override
            public void write(JsonWriter out, UUID value) throws IOException {
                out.value(value.toString());
            }

            @Override
            public UUID read(JsonReader in) throws IOException {
                return UUID.fromString(in.nextString());
            }
        })
        .create();

        try {
            if (!file.exists()) {
                file.createNewFile();
                // Initialize with empty array
                saveUsers(new ArrayList<>());
            }
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo file", e);
        }
    }

    private List<User> loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }
            
            String content = jsonContent.toString();
            if (content.isEmpty()) {
                return new ArrayList<>();
            }
            
            return gson.fromJson(content, new TypeToken<List<User>>(){}.getType());
        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc file", e);
        }
    }

    private void saveUsers(List<User> users) {
        try (FileWriter writer = new FileWriter(file)) {
            // Write entire list as a single JSON array
            writer.write(gson.toJson(users));
        } catch (IOException e) {
            throw new RuntimeException("Lỗi ghi file", e);
        }
    }
    
    @Override
    public void save(User user) {
        List<User> users = loadUsers();
        users.removeIf(u -> u.getId().equals(user.getId()));
        users.add(user);
        saveUsers(users);
    }

    @Override
    public User findById(UUID id) {
        return loadUsers().stream()
                .filter(u -> u.getId().asString().equals(id.toString()))
                .findFirst()
                .orElse(null);
    }
}