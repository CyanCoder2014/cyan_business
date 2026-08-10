package com.cyancoder.bpm.api.dto;

import com.cyancoder.bpm.domain.ManagedObject;
import java.util.List;

public record ManagedObjectQueueResponse(List<ManagedObject> content, long totalElements, int page, int size) {}
