package com.cyancoder.batchworker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:batchworker;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
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
