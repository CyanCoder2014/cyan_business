package com.cyancoder.dynamiccore.validation;

import java.util.Map;

public record ValidatorResponse(boolean valid, Map<String, Object> fullInput, String validationMessage) {
}
