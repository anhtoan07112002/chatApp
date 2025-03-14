package com.chat.infrastructure.persistence.messsage.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.chat.infrastructure.persistence.messsage.entity.MongoMessageEntity;
import java.util.List;

public interface MongoMessageRepository extends MongoRepository<MongoMessageEntity, String>{
    List<MongoMessageEntity> findBySenderId(String senderId);
    List<MongoMessageEntity> findByReceiverIdAndStatus(String receiverId, String status);
}
