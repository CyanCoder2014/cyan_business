package com.cyancoder.bpm.api.dto;

import java.util.List;

public record StateActionStructureResponse(
        String type,
        List<String> aliases,
        String description,
        List<MetadataFieldDescriptor> commonFields,
        List<MetadataFieldDescriptor> params
) {
}
