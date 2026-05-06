package com.cyancoder.bpm.domain;

import java.util.Set;

public record FlowAccessRule(Set<String> canRead, Set<String> canEdit, Set<String> canApprove) {
}

