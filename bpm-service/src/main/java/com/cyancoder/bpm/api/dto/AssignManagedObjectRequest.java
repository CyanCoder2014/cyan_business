package com.cyancoder.bpm.api.dto;

import com.cyancoder.bpm.domain.AssigneeType;
import jakarta.validation.constraints.NotBlank;

public record AssignManagedObjectRequest(@NotBlank String assignee, AssigneeType assigneeType) {}
