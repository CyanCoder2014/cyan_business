package com.cyancoder.bpm.domain;

import java.util.Map;

public record FlowActionConfig(ActionType type, Map<String, Object> params) {
}

