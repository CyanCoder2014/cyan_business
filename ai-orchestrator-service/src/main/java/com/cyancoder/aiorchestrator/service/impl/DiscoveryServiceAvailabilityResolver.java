package com.cyancoder.aiorchestrator.service.impl;

import com.cyancoder.aiorchestrator.config.PlatformMetadataProperties;
import com.cyancoder.aiorchestrator.service.ServiceAvailabilityResolver;
import com.cyancoder.aiorchestrator.service.ServiceAvailabilitySnapshot;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;

@Service
public class DiscoveryServiceAvailabilityResolver implements ServiceAvailabilityResolver {
    private static final Map<String, String> ALIASES = aliases();
    private final DiscoveryClient discoveryClient;
    private final PlatformMetadataProperties properties;

    public DiscoveryServiceAvailabilityResolver(DiscoveryClient discoveryClient,
                                                PlatformMetadataProperties properties) {
        this.discoveryClient = discoveryClient;
        this.properties = properties;
    }

    @Override
    public ServiceAvailabilitySnapshot resolve(List<String> requestedServiceKeys) {
        if (requestedServiceKeys != null && !requestedServiceKeys.isEmpty()) {
            return new ServiceAvailabilitySnapshot(normalize(requestedServiceKeys), "REQUEST");
        }
        if ("CONFIGURED".equalsIgnoreCase(properties.getAvailabilityMode())) {
            return new ServiceAvailabilitySnapshot(
                    normalize(properties.getServiceKeys()), "KUBERNETES_CONFIG");
        }
        List<String> discovered = normalize(discoveryClient.getServices());
        if (!discovered.isEmpty()) {
            return new ServiceAvailabilitySnapshot(discovered, "LOCAL_DISCOVERY");
        }
        return new ServiceAvailabilitySnapshot(normalize(properties.getServiceKeys()), "CONFIG_FALLBACK");
    }

    private List<String> normalize(List<String> values) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String value : values == null ? List.<String>of() : values) {
            if (value == null || value.isBlank()) continue;
            String normalized = value.trim().toLowerCase(Locale.ROOT)
                    .replace('_', '-').replace(' ', '-');
            normalized = ALIASES.getOrDefault(normalized, normalized);
            if (!normalized.endsWith("-service")) {
                normalized += "-service";
            }
            result.add(normalized);
        }
        return List.copyOf(result);
    }

    private static Map<String, String> aliases() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("ai", "ai-orchestrator-service");
        values.put("ai-orchestrator", "ai-orchestrator-service");
        values.put("automation", "automation-orchestrator-service");
        values.put("automation-orchestrator", "automation-orchestrator-service");
        values.put("bpm", "bpm-service");
        values.put("notification", "notification-service");
        values.put("report", "report-service");
        values.put("media", "media-service");
        values.put("processor", "processor-service");
        values.put("processoor", "processor-service");
        values.put("batch", "batch-worker-service");
        values.put("batch-worker", "batch-worker-service");
        values.put("sso-auth", "sso-auth-service");
        values.put("sso-user", "sso-user-service");
        values.put("sso-captcha", "sso-captcha-service");
        values.put("ssh-auth", "sso-auth-service");
        values.put("ssh-user", "sso-user-service");
        values.put("ssh-captcha", "sso-captcha-service");
        return Map.copyOf(values);
    }
}
