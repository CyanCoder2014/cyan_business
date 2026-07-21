package com.cyancoder.automationorchestrator.service;

import com.cyancoder.automationorchestrator.domain.AutomationFlowDefinition;
import com.cyancoder.automationorchestrator.domain.AutomationNode;
import com.cyancoder.automationorchestrator.domain.AutomationNodeType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class AutomationScheduleService {
    private static final Logger log = LoggerFactory.getLogger(AutomationScheduleService.class);
    private final AutomationFlowDefinitionService definitions;
    private final AutomationExecutionService executions;

    public AutomationScheduleService(AutomationFlowDefinitionService definitions, AutomationExecutionService executions) {
        this.definitions = definitions;
        this.executions = executions;
    }

    @Scheduled(fixedDelayString = "${automation.schedule.poll-ms:1000}")
    public void triggerDueFlows() {
        Instant now = Instant.now();
        for (AutomationFlowDefinition definition : definitions.activeScheduledCandidates()) {
            try {
                triggerIfDue(definition, now);
            } catch (RuntimeException failure) {
                log.warn("Unable to evaluate scheduled automation {}", definition.getFlowKey(), failure);
            }
        }
    }

    private void triggerIfDue(AutomationFlowDefinition definition, Instant now) {
            AutomationNode trigger = definition.getNodes().stream()
                    .filter(node -> node.id().equals(definition.getEntryNodeId()))
                    .filter(node -> node.type() == AutomationNodeType.SCHEDULE_TRIGGER)
                    .findFirst().orElse(null);
            if (trigger == null) return;
            if (definition.getNextScheduledAt() == null) {
                definition.setNextScheduledAt(next(trigger.configOrEmpty(), now));
                definitions.saveScheduleState(definition);
                return;
            }
            if (definition.getNextScheduledAt().isAfter(now)) return;
            Instant scheduledFor = definition.getNextScheduledAt();
            definition.setLastScheduledAt(scheduledFor);
            definition.setNextScheduledAt(next(trigger.configOrEmpty(), now));
            definitions.saveScheduleState(definition);
            executions.triggerWebhook(
                    definition.getFlowKey(), definition.getTenantKey(), definition.getSiteKey(),
                    Map.of("scheduledAt", scheduledFor.toString(), "triggeredAt", now.toString()),
                    Map.of("runMode", "PRODUCTION", "triggerType", "SCHEDULE", "environment", definition.getEnvironment()),
                    "schedule:" + definition.getFlowKey() + ":" + scheduledFor
            );
    }

    Instant next(Map<String, Object> config, Instant after) {
        String cron = first(config, "cron", "cronExpression");
        ZoneId zone = ZoneId.of(Objects.toString(config.getOrDefault("timezone", "UTC")));
        if (cron != null) {
            ZonedDateTime next = CronExpression.parse(cron).next(after.atZone(zone));
            if (next == null) throw new IllegalArgumentException("schedule has no next cron occurrence");
            return next.toInstant();
        }
        return after.plus(interval(config));
    }

    private Duration interval(Map<String, Object> config) {
        long seconds = AutomationDataSupport.longValue(config.get("intervalSeconds"), 0);
        if (seconds > 0) return Duration.ofSeconds(seconds);
        List<Object> rules = AutomationDataSupport.list(AutomationDataSupport.map(config.get("rule")).get("interval"));
        if (!rules.isEmpty()) {
            Map<String, Object> rule = AutomationDataSupport.map(rules.getFirst());
            String field = Objects.toString(rule.getOrDefault("field", "minutes")).toLowerCase(Locale.ROOT);
            long value = switch (field) {
                case "seconds" -> AutomationDataSupport.longValue(rule.get("secondsInterval"), 1);
                case "hours" -> AutomationDataSupport.longValue(rule.get("hoursInterval"), 1) * 3600;
                case "days" -> AutomationDataSupport.longValue(rule.get("daysInterval"), 1) * 86400;
                default -> AutomationDataSupport.longValue(rule.get("minutesInterval"), 1) * 60;
            };
            return Duration.ofSeconds(Math.max(1, value));
        }
        throw new IllegalArgumentException("SCHEDULE_TRIGGER requires cron, cronExpression, intervalSeconds, or rule.interval");
    }

    private String first(Map<String, Object> config, String... keys) {
        for (String key : keys) {
            String value = AutomationDataSupport.string(config.get(key));
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }
}
