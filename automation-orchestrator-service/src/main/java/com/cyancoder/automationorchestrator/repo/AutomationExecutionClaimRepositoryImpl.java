package com.cyancoder.automationorchestrator.repo;

import com.cyancoder.automationorchestrator.domain.AutomationExecution;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Instant;
import java.util.Optional;

public class AutomationExecutionClaimRepositoryImpl implements AutomationExecutionClaimRepository {
    private final MongoTemplate mongoTemplate;

    public AutomationExecutionClaimRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<AutomationExecution> claimNextRecoverable(
            String workerId,
            Instant now,
            Instant staleBefore,
            Instant leaseUntil
    ) {
        Criteria expiredLease = new Criteria().orOperator(
                Criteria.where("leaseUntil").lte(now),
                new Criteria().andOperator(
                        Criteria.where("leaseUntil").exists(false),
                        new Criteria().orOperator(
                                Criteria.where("updatedAt").lte(staleBefore),
                                Criteria.where("updatedAt").exists(false)
                        )
                )
        );
        Criteria staleRunning = new Criteria().andOperator(
                Criteria.where("status").is("RUNNING"),
                expiredLease
        );
        Criteria dueWait = new Criteria().andOperator(
                Criteria.where("status").in("WAITING", "WAITING_CONCURRENCY"),
                Criteria.where("resumeAt").lte(now)
        );
        Query query = new Query(new Criteria().orOperator(staleRunning, dueWait))
                .with(Sort.by(Sort.Direction.ASC, "updatedAt", "createdAt"));
        Update update = new Update()
                .set("status", "CLAIMED")
                .set("workerId", workerId)
                .set("heartbeatAt", now)
                .set("leaseUntil", leaseUntil)
                .inc("revision", 1);
        AutomationExecution claimed = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(false),
                AutomationExecution.class
        );
        return Optional.ofNullable(claimed);
    }

    @Override
    public long renewLeases(String workerId, Instant heartbeatAt, Instant leaseUntil) {
        Query query = new Query(Criteria.where("workerId").is(workerId).and("status").is("RUNNING"));
        Update update = new Update()
                .set("heartbeatAt", heartbeatAt)
                .set("leaseUntil", leaseUntil);
        UpdateResult result = mongoTemplate.updateMulti(query, update, AutomationExecution.class);
        return result.getModifiedCount();
    }
}
