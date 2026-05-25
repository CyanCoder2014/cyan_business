package com.cyancoder.aiorchestrator.service;

import com.cyancoder.aiorchestrator.api.dto.FollowUpQuestionDto;
import com.cyancoder.aiorchestrator.domain.AppBlueprint;
import com.cyancoder.aiorchestrator.domain.ClientAppDraft;
import com.cyancoder.aiorchestrator.domain.PlatformAppDslDefinition;

import java.util.List;
import java.util.Map;

public interface FollowUpQuestionService {
    List<FollowUpQuestionDto> resolveForDraft(ClientAppDraft draft);
    List<FollowUpQuestionDto> resolveForBlueprint(AppBlueprint blueprint,
                                                  Map<String, Object> answers,
                                                  PlatformAppDslDefinition dsl,
                                                  String prompt);
}
