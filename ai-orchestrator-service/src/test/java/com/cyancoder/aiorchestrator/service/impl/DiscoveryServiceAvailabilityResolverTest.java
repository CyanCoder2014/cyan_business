package com.cyancoder.aiorchestrator.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.cyancoder.aiorchestrator.config.PlatformMetadataProperties;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.discovery.DiscoveryClient;

class DiscoveryServiceAvailabilityResolverTest {
    private final DiscoveryClient discovery = mock(DiscoveryClient.class);
    private final PlatformMetadataProperties properties = new PlatformMetadataProperties();
    private final DiscoveryServiceAvailabilityResolver resolver =
            new DiscoveryServiceAvailabilityResolver(discovery, properties);

    @Test
    void requestInventoryIsAuthoritativeAndNormalizesClientAliases() {
        var result = resolver.resolve(List.of(
                "AI", "Notification", "bpm", "automation", "report",
                "ssh-user", "ssh-captcha", "media", "processoor"));

        assertThat(result.source()).isEqualTo("REQUEST");
        assertThat(result.availableServiceKeys()).containsExactly(
                "ai-orchestrator-service",
                "notification-service",
                "bpm-service",
                "automation-orchestrator-service",
                "report-service",
                "sso-user-service",
                "sso-captcha-service",
                "media-service",
                "processor-service");
    }

    @Test
    void discoveryIsUsedOnlyWhenRequestDoesNotProvideInventory() {
        when(discovery.getServices()).thenReturn(List.of("BPM-SERVICE", "PROCESSOR-SERVICE"));

        var result = resolver.resolve(List.of());

        assertThat(result.source()).isEqualTo("LOCAL_DISCOVERY");
        assertThat(result.availableServiceKeys()).containsExactly("bpm-service", "processor-service");
    }

    @Test
    void configuredProductionModeDoesNotConsultLocalDiscovery() {
        DiscoveryClient productionDiscovery = mock(DiscoveryClient.class);
        PlatformMetadataProperties productionProperties = new PlatformMetadataProperties();
        productionProperties.setAvailabilityMode("CONFIGURED");
        productionProperties.setServiceKeys(List.of("AI", "bpm"));
        DiscoveryServiceAvailabilityResolver productionResolver =
                new DiscoveryServiceAvailabilityResolver(productionDiscovery, productionProperties);

        var result = productionResolver.resolve(null);

        assertThat(result.source()).isEqualTo("KUBERNETES_CONFIG");
        assertThat(result.availableServiceKeys()).containsExactly(
                "ai-orchestrator-service", "bpm-service");
        verifyNoInteractions(productionDiscovery);
    }
}
