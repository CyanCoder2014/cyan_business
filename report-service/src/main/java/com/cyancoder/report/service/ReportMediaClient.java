package com.cyancoder.report.service;

import com.cyancoder.platform.internalhttp.InternalServiceCredentialsResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Map;
import java.util.Objects;

@Component
public class ReportMediaClient {
    private final RestClient client;
    private final String authorization;

    public ReportMediaClient(RestClient.Builder builder,
                             @Value("${media-service.base-url:http://localhost:9116}") String url,
                             InternalServiceCredentialsResolver credentialsResolver) {
        this.client = builder.baseUrl(url).build();
        this.authorization = credentialsResolver.authorizationHeader("media-service");
    }

    public Map<?, ?> upload(String tenant, String site, String fileName, String mime, byte[] bytes, String exportId) {
        Map<?, ?> result = client.post()
                .uri("/internal/media/assets/generated")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .header("X-Tenant-Key", tenant)
                .headers(headers -> {
                    if (site != null && !site.isBlank()) headers.set("X-Site-Key", site);
                })
                .body(Map.of(
                        "fileName", fileName,
                        "mimeType", mime,
                        "base64", Base64.getEncoder().encodeToString(bytes),
                        "generatedBy", "report-export:" + exportId,
                        "retentionDays", 30))
                .retrieve()
                .body(Map.class);
        String asset = Objects.toString(result == null ? null : result.get("assetKey"), null);
        if (asset != null) {
            client.put()
                    .uri("/internal/media/assets/{asset}/references", asset)
                    .header(HttpHeaders.AUTHORIZATION, authorization)
                    .header("X-Tenant-Key", tenant)
                    .headers(headers -> {
                        if (site != null && !site.isBlank()) headers.set("X-Site-Key", site);
                    })
                    .body(Map.of(
                            "ownerService", "report-service",
                            "ownerType", "REPORT_EXPORT",
                            "ownerKey", exportId,
                            "fieldPath", "assetKey"))
                    .retrieve()
                    .toBodilessEntity();
        }
        return result;
    }
}
