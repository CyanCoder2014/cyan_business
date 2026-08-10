package com.cyancoder.bpm.service;

import com.cyancoder.bpm.api.dto.AssignmentTargetResponse;
import com.cyancoder.bpm.domain.AssigneeType;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BpmAssignmentDirectoryService {
    private final InternalServiceHttpSupport http;

    public BpmAssignmentDirectoryService(InternalServiceHttpSupport http) { this.http = http; }

    public List<AssignmentTargetResponse> search(String tenantKey, AssigneeType type, String query) {
        if (type == AssigneeType.GROUP) return List.of();
        String path = "/internal/tenants/" + encode(tenantKey) + "/assignment-targets?type=" + type.name()
                + (query == null || query.isBlank() ? "" : "&query=" + encode(query));
        AssignmentTargetResponse[] response = http.get("tenant-service", path, tenantKey, null, AssignmentTargetResponse[].class);
        return response == null ? List.of() : Arrays.asList(response);
    }

    public void requireValid(String tenantKey, AssigneeType type, String key) {
        if (type == AssigneeType.GROUP) {
            if (key == null || key.isBlank()) throw new IllegalArgumentException("group key is required");
            return;
        }
        boolean valid = search(tenantKey, type, key).stream().anyMatch(target -> target.active() && target.key().equals(key));
        if (!valid) throw new IllegalArgumentException("assignee does not exist or is inactive");
    }

    private String encode(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8); }
}
