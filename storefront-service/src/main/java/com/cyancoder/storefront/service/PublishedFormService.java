package com.cyancoder.storefront.service;

import com.cyancoder.storefront.api.PublishedFormContracts.*;
import com.cyancoder.storefront.model.PublishedFormEntity;
import com.cyancoder.storefront.repository.PublishedFormRepository;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PublishedFormService {
    private final PublishedFormRepository forms;
    private final TenantMembershipClient memberships;
    private final InternalServiceHttpSupport internal;

    public PublishedFormService(PublishedFormRepository forms, TenantMembershipClient memberships, InternalServiceHttpSupport internal) {
        this.forms = forms; this.memberships = memberships; this.internal = internal;
    }

    public List<PublishedFormSummary> list(String tenantKey, String siteKey, String actor) {
        requireScope(tenantKey, actor);
        return forms.findAllByTenantKeyAndSiteKeyOrderByUpdatedAtDesc(tenantKey, normalizeSite(siteKey)).stream().map(this::summary).toList();
    }

    @Transactional
    public PublishedFormSummary publish(String tenantKey, String siteKey, String actor, PublishFormRequest request) {
        requireScope(tenantKey, actor); memberships.requirePermission(tenantKey, actor, "definition.manage");
        Map<String, Object> definition = loadDefinition(request.serviceKey(), request.entityKey(), tenantKey, normalizeSite(siteKey));
        if (!Boolean.TRUE.equals(definition.get("active"))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Publish the entity definition before sharing its form");
        }
        String slug = normalizeSlug(request.slug());
        PublishedFormEntity form = forms.findBySlug(slug).map(existing -> {
            if (!existing.getTenantKey().equals(tenantKey) || !existing.getSiteKey().equals(normalizeSite(siteKey))
                    || !existing.getServiceKey().equals(request.serviceKey()) || !existing.getEntityKey().equals(request.entityKey())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Form link is already in use");
            }
            existing.republish(request.title().trim(), clean(request.description()), request.visibility());
            return existing;
        }).orElseGet(() -> new PublishedFormEntity(slug, tenantKey, normalizeSite(siteKey), request.serviceKey(), request.entityKey(), request.title().trim(), clean(request.description()), request.visibility(), actor));
        return summary(forms.save(form));
    }

    public PublishedFormView getForMember(String slug, String tenantKey, String siteKey, String actor) {
        requireScope(tenantKey, actor);
        PublishedFormEntity form = active(slug);
        if (!form.getTenantKey().equals(tenantKey) || !form.getSiteKey().equals(normalizeSite(siteKey))) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Form not found");
        return view(form);
    }

    public PublishedFormView getPublic(String slug) {
        PublishedFormEntity form = active(slug);
        if (!"PUBLIC".equals(form.getVisibility())) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Form not found");
        return view(form);
    }

    public FormSubmissionResponse submitForMember(String slug, String tenantKey, String siteKey, String actor, String idempotencyKey, Map<String, Object> data) {
        requireScope(tenantKey, actor);
        PublishedFormEntity form = active(slug);
        if (!form.getTenantKey().equals(tenantKey) || !form.getSiteKey().equals(normalizeSite(siteKey))) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Form not found");
        return submit(form, idempotencyKey, data);
    }

    public FormSubmissionResponse submitPublic(String slug, String idempotencyKey, Map<String, Object> data) {
        PublishedFormEntity form = active(slug);
        if (!"PUBLIC".equals(form.getVisibility())) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Form not found");
        return submit(form, idempotencyKey, data);
    }

    @Transactional
    public void archive(String slug, String tenantKey, String siteKey, String actor) {
        requireScope(tenantKey, actor); memberships.requirePermission(tenantKey, actor, "definition.manage");
        PublishedFormEntity form = forms.findBySlug(normalizeSlug(slug)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Form not found"));
        if (!form.getTenantKey().equals(tenantKey) || !form.getSiteKey().equals(normalizeSite(siteKey))) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Form not found");
        form.archive(); forms.save(form);
    }

    private FormSubmissionResponse submit(PublishedFormEntity form, String idempotencyKey, Map<String, Object> data) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 180) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key is required");
        if (data == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Form data is required");
        String submissionKey = "form-" + UUID.nameUUIDFromBytes((form.getId() + ":" + idempotencyKey).getBytes(StandardCharsets.UTF_8));
        internal.post(form.getServiceKey(), "/internal/entities/submit/" + form.getEntityKey() + "?recordKey=" + submissionKey,
                form.getTenantKey(), form.getSiteKey(), data, Map.class);
        return new FormSubmissionResponse(submissionKey, "ACCEPTED");
    }

    private PublishedFormView view(PublishedFormEntity form) {
        Map<String, Object> response = loadDefinition(form.getServiceKey(), form.getEntityKey(), form.getTenantKey(), form.getSiteKey());
        Object model = response.get("definition");
        Map<String, Object> definition = model instanceof Map<?, ?> map ? cast(map) : Map.of();
        return new PublishedFormView(form.getSlug(), form.getTitle(), form.getDescription(), form.getVisibility(), form.getServiceKey(), form.getEntityKey(), definition);
    }

    private Map<String, Object> loadDefinition(String service, String entity, String tenant, String site) {
        Map<String, Object> result = internal.get(service, "/internal/entities/definitions/" + entity, tenant, site, Map.class);
        if (result == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity definition not found");
        return result;
    }

    private PublishedFormEntity active(String slug) {
        PublishedFormEntity form = forms.findBySlug(normalizeSlug(slug)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Form not found"));
        if (!"PUBLISHED".equals(form.getStatus())) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Form not found");
        return form;
    }
    private void requireScope(String tenant, String actor) { if (tenant == null || tenant.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-Tenant-Key is required"); memberships.requireMembership(tenant, actor); }
    private String normalizeSlug(String value) { String slug = value == null ? "" : value.trim().toLowerCase(Locale.ROOT); if (!slug.matches("[a-z0-9][a-z0-9-]{2,119}")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid form link"); return slug; }
    private String normalizeSite(String value) { return value == null ? "" : value.trim(); }
    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private PublishedFormSummary summary(PublishedFormEntity form) { return new PublishedFormSummary(form.getSlug(), form.getTenantKey(), form.getSiteKey(), form.getServiceKey(), form.getEntityKey(), form.getTitle(), form.getDescription(), form.getVisibility(), form.getStatus(), form.getCreatedAt(), form.getUpdatedAt()); }
    @SuppressWarnings("unchecked") private Map<String, Object> cast(Map<?, ?> value) { return (Map<String, Object>) value; }
}
