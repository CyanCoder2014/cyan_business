package com.cyancoder.automationorchestrator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.time.Duration;
import java.util.UUID;

@Component
@ConfigurationProperties(prefix = "automation.worker")
public class AutomationWorkerProperties {
    private String id = defaultWorkerId();
    private Duration leaseDuration = Duration.ofMinutes(5);
    private Duration orphanGrace = Duration.ofSeconds(30);
    private int recoveryBatchSize = 20;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Duration getLeaseDuration() { return leaseDuration; }
    public void setLeaseDuration(Duration leaseDuration) { this.leaseDuration = leaseDuration; }
    public Duration getOrphanGrace() { return orphanGrace; }
    public void setOrphanGrace(Duration orphanGrace) { this.orphanGrace = orphanGrace; }
    public int getRecoveryBatchSize() { return recoveryBatchSize; }
    public void setRecoveryBatchSize(int recoveryBatchSize) { this.recoveryBatchSize = recoveryBatchSize; }

    private static String defaultWorkerId() {
        try {
            return InetAddress.getLocalHost().getHostName() + "-" + UUID.randomUUID();
        } catch (Exception ignored) {
            return "automation-worker-" + UUID.randomUUID();
        }
    }
}
