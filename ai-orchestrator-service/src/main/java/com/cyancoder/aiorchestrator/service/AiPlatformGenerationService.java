package com.cyancoder.aiorchestrator.service;

import com.cyancoder.aiorchestrator.api.dto.GeneratePlatformAppRequest;
import com.cyancoder.aiorchestrator.api.dto.GeneratePlatformAppResponse;

public interface AiPlatformGenerationService {
    GeneratePlatformAppResponse generate(GeneratePlatformAppRequest request);
}

