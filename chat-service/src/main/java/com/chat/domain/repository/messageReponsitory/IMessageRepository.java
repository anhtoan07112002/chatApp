package com.chat.domain.repository.messageReponsitory;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.chat.domain.entity.messages.Message;
// import com.chat.domain.entity.messages.MessageId;
import com.chat.domain.entity.user.UserId;

@Repository
public interface IMessageRepository {
    void save(Message message); // Lưu một tin nhắn
    List<Message> findBySenderId(UserId senderId); // Tìm tin nhắn theo ID người gửi
    List<Message> findPendingMessagesByReceiverId(UserId receiverId); // Tìm tin nhắn đang chờ
    // List<Message> findByReceiverId(UserId receiverId); // Tìm tin nhắn theo ID người nhận
    // Message findById(MessageId messageId); // Tìm tin nhắn theo ID
    // void deleteById(MessageId messageId); // Xóa tin nhắn theo ID
}
