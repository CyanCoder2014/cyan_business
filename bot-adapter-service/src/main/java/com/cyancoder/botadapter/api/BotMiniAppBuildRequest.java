package com.cyancoder.botadapter.api;

import java.util.Map;

public record BotMiniAppBuildRequest(
        String channel,
        String integrationKey,
        String buildKey,
        String title,
        String launchUrl,
        Map<String, Object> manifest
) {
}
