package com.cyancoder.batchworker.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "batch.worker")
public class BatchWorkerProperties {
    private int chunkSize = 200;
    private int retryLimit = 5;
    private int skipLimit = 100;
    private Duration leaseDuration = Duration.ofMinutes(2);
    private Duration orphanTimeout = Duration.ofMinutes(5);

    public int getChunkSize() { return chunkSize; }
    public void setChunkSize(int chunkSize) { this.chunkSize = chunkSize; }
    public int getRetryLimit() { return retryLimit; }
    public void setRetryLimit(int retryLimit) { this.retryLimit = retryLimit; }
    public int getSkipLimit() { return skipLimit; }
    public void setSkipLimit(int skipLimit) { this.skipLimit = skipLimit; }
    public Duration getLeaseDuration() { return leaseDuration; }
    public void setLeaseDuration(Duration leaseDuration) { this.leaseDuration = leaseDuration; }
    public Duration getOrphanTimeout() { return orphanTimeout; }
    public void setOrphanTimeout(Duration orphanTimeout) { this.orphanTimeout = orphanTimeout; }
}
