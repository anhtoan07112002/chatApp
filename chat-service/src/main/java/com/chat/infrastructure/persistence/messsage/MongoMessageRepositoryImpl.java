package com.chat.infrastructure.persistence.messsage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.chat.domain.repository.messageReponsitory.IMessageRepository;
import com.chat.domain.entity.messages.Message;
import com.chat.domain.entity.messages.MessageId;
import com.chat.domain.entity.user.UserId;
import com.chat.infrastructure.persistence.messsage.entity.MongoMessageEntity;
import com.chat.infrastructure.persistence.messsage.repository.MongoMessageRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Primary
@Slf4j
public class MongoMessageRepositoryImpl implements IMessageRepository {
    private final ObjectProvider<MongoMessageRepository> mongoRepositoryProvider;

    public MongoMessageRepositoryImpl(ObjectProvider<MongoMessageRepository> mongoRepositoryProvider) {
        this.mongoRepositoryProvider = mongoRepositoryProvider;
    }

    private MongoMessageRepository getRepository() {
        MongoMessageRepository repository = mongoRepositoryProvider.getIfAvailable();
        if (repository == null) {
            log.error("MongoDB repository not available");
            throw new IllegalStateException("MongoDB repository not available");
        }
        return repository;
    }

    @Override
    public void save(Message message) {
        MongoRepository repository = getRepository();
        MongoMessageEntity entity = MongoMessageEntity.fromDomain(message);

        // Tìm message hiện có
        Optional<MongoMessageEntity> existingMessage = repository.findById(entity.getId());

        if (existingMessage.isPresent()) {
            // Cập nhật thông tin cho tin nhắn hiện có
            MongoMessageEntity existing = existingMessage.get();
            existing.setStatus(message.getStatus().name());
            existing.setSentAt(message.getSentAt());
            existing.setReceivedAt(message.getReceivedAt());
            existing.setReadAt(message.getReadAt());
            repository.save(existing);
        } else {
            // Tạo tin nhắn mới nếu chưa tồn tại
            repository.save(entity);
        }
    }

    @Override
    public List<Message> findBySenderId(UserId senderId) {
        return getRepository().findBySenderId(senderId.toString())
                .stream()
                .map(MongoMessageEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> findPendingMessagesByReceiverId(UserId receiverId) {
        return getRepository().findByReceiverIdAndStatus(
                        receiverId.toString(),
                        "PENDING"
                )
                .stream()
                .map(MongoMessageEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Message findById(MessageId id) {
        return getRepository().findById(id.vaUuid().toString())
                .map(MongoMessageEntity::toDomain)
                .orElse(null);
    }
}
