package com.chat.infrastructure.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Value;

import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageStatus;
import com.chat.domain.entity.user.UserId;
import com.chat.domain.repository.messageReponsitory.IMessageRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Repository
public class TextFileRepository implements IMessageRepository {

    private final File file;
    private final Gson gson;

    public TextFileRepository(@Value("${message.storage.path:messages.json}") String filePath) {
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

    private List<Message> loadMessages() {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<Message> messages = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if(!line.isBlank()) {
                    Message message = gson.fromJson(line, Message.class);
                    messages.add(message);
                }
            }
            return messages;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc file", e);
        }
    }
    
    @Override
    public void save(Message message) {
        List<Message> messages = loadMessages();
        messages.add(message);
    }

    @Override
    public List<Message> findBySenderId(UserId senderId) {
        return loadMessages().stream().filter(m -> m.getSenderId().equals(senderId)).collect(Collectors.toList());
    }

    @Override
    public List<Message> findPendingMessagesByReceiverId(UserId receiverId) {
        return loadMessages().stream().filter(m -> m.getReceiverId().equals(receiverId) && m.getStatus() == MessageStatus.PENDING).collect(Collectors.toList());
    }
}
