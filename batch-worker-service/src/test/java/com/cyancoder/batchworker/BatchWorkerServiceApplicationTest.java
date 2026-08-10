package com.cyancoder.batchworker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/batch_worker_test",
        "spring.datasource.username=postgres",
        "spring.datasource.password=postgres",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.batch.jdbc.initialize-schema=always",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.task.scheduling.enabled=false"
})
class BatchWorkerServiceApplicationTest {
    @Test
    void contextLoadsWithBatchRepositoryAndDispatcher() {}
}
