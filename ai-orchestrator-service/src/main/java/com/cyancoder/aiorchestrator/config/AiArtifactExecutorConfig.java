package com.cyancoder.aiorchestrator.config;
import java.util.concurrent.Executor;import org.springframework.context.annotation.Bean;import org.springframework.context.annotation.Configuration;import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
@Configuration public class AiArtifactExecutorConfig{@Bean("aiArtifactExecutor")public Executor executor(){ThreadPoolTaskExecutor e=new ThreadPoolTaskExecutor();e.setCorePoolSize(2);e.setMaxPoolSize(4);e.setQueueCapacity(100);e.setThreadNamePrefix("ai-artifact-");e.initialize();return e;}}
