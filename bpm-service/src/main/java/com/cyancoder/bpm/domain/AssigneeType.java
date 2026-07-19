package com.cyancoder.bpm.domain;

public enum AssigneeType {
    USER,
    ROLE,
    GROUP;

    public static AssigneeType from(Object value) {
        if (value == null || value.toString().isBlank()) {
            return USER;
        }
        for (AssigneeType type : values()) {
            if (type.name().equalsIgnoreCase(value.toString().trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("unsupported assigneeType: " + value);
    }
}
