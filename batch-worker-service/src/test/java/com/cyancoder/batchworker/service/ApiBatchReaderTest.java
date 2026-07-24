package com.cyancoder.batchworker.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.cyancoder.batchworker.api.BatchDefinitionSpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.ExecutionContext;

class ApiBatchReaderTest {
    private HttpServer server;

    @AfterEach
    void stop() {
        if (server != null) server.stop(0);
    }

    @Test
    void resumesAtLastCommittedItemWithinPage() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/customers", exchange -> {
            byte[] body = "{\"data\":[{\"id\":\"1\"},{\"id\":\"2\"},{\"id\":\"3\"}]}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        BatchDefinitionSpec.Source source = new BatchDefinitionSpec.Source(
                "http://localhost:" + server.getAddress().getPort() + "/customers",
                "data", "page", "size", 3, Map.of(), null);
        ExecutionContext checkpoint = new ExecutionContext();
        ApiBatchReader first = new ApiBatchReader(source, new ObjectMapper());
        first.open(checkpoint);
        assertThat(first.read().get("id")).isEqualTo("1");
        assertThat(first.read().get("id")).isEqualTo("2");
        first.update(checkpoint);

        ApiBatchReader restarted = new ApiBatchReader(source, new ObjectMapper());
        restarted.open(checkpoint);
        assertThat(restarted.read().get("id")).isEqualTo("3");
    }
}
