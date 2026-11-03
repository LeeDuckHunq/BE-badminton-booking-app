package com.example.alopoapi.service;

import com.example.alopoapi.entity.BookingLockEntity;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class BookingLockService {

    private static final Duration LOCK_DURATION = Duration.ofSeconds(30);

    private final MongoTemplate mongoTemplate;

    public BookingLockService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public boolean tryAcquire(String maSan, String owner) {
        Instant now = Instant.now();

        Query query = new Query(new Criteria().andOperator(
                Criteria.where("_id").is(maSan),
                new Criteria().orOperator(
                        Criteria.where("lockedUntil").lte(now),
                        Criteria.where("lockedUntil").exists(false)
                )
        ));

        Update update = new Update()
                .set("owner", owner)
                .set("lockedUntil", now.plus(LOCK_DURATION));

        try {
            BookingLockEntity lock = mongoTemplate.findAndModify(
                    query,
                    update,
                    FindAndModifyOptions.options().upsert(true).returnNew(true),
                    BookingLockEntity.class
            );
            return lock != null && owner.equals(lock.getOwner());
        } catch (DuplicateKeyException exception) {
            return false;
        }
    }

    public void release(String maSan, String owner) {
        Query query = Query.query(
                Criteria.where("_id").is(maSan).and("owner").is(owner)
        );
        Update update = new Update().unset("owner").unset("lockedUntil");
        mongoTemplate.updateFirst(query, update, BookingLockEntity.class);
    }
}
