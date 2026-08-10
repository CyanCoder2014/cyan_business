package com.cyancoder.botadapter.api;

import java.util.Map;

public record BotProcessBindingRequest(
        String bindingKey,
        String triggerType,
        String commandPrefix,
        String targetType,
        String targetKey,
        Map<String, Object> inputTemplate,
        Boolean enabled
) {}
