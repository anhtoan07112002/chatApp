package com.chat.infrastructure.persistence.user;

import com.chat.domain.entity.user.User;
import com.chat.domain.repository.userReponsitory.IUserRepository;
import com.chat.infrastructure.persistence.user.entity.MongoUserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MongoDBUserRepository implements IUserRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public void save(User user) {
        MongoUserEntity entity = MongoUserEntity.fromDomain(user);
        mongoTemplate.save(entity);
    }

    @Override
    public User findById(UUID id) {
        MongoUserEntity entity = mongoTemplate.findById(id.toString(), MongoUserEntity.class);
        return entity != null ? entity.toDomain() : null;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        Query query = new Query();
        query.addCriteria(Criteria.where("email").is(email));
        MongoUserEntity entity = mongoTemplate.findOne(query, MongoUserEntity.class);
        return Optional.ofNullable(entity).map(MongoUserEntity::toDomain);
    }

    @Override
    public List<User> findAll() {
        List<MongoUserEntity> entities = mongoTemplate.findAll(MongoUserEntity.class);
        return entities.stream()
                .map(MongoUserEntity::toDomain)
                .toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        Query query = new Query();
        query.addCriteria(Criteria.where("email").is(email));
        return mongoTemplate.exists(query, MongoUserEntity.class);
    }
}
