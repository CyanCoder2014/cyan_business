package com.cyancoder.storefront.controller;

import com.cyancoder.storefront.api.PublishedFormContracts.*;
import com.cyancoder.storefront.service.PublishedFormService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/endpoint/forms")
public class EndpointPublishedFormController {
    private final PublishedFormService service;
    public EndpointPublishedFormController(PublishedFormService service) { this.service = service; }
    @GetMapping public List<PublishedFormSummary> list(@RequestHeader("X-Tenant-Key") String tenant, @RequestHeader(value="X-Site-Key",required=false) String site, Authentication auth) { return service.list(tenant, site, auth.getName()); }
    @PutMapping("/{slug}") public PublishedFormSummary publish(@PathVariable String slug, @RequestHeader("X-Tenant-Key") String tenant, @RequestHeader(value="X-Site-Key",required=false) String site, Authentication auth, @Valid @RequestBody PublishFormRequest request) { return service.publish(tenant, site, auth.getName(), new PublishFormRequest(slug, request.serviceKey(), request.entityKey(), request.title(), request.description(), request.visibility())); }
    @GetMapping("/{slug}") public PublishedFormView get(@PathVariable String slug, @RequestHeader("X-Tenant-Key") String tenant, @RequestHeader(value="X-Site-Key",required=false) String site, Authentication auth) { return service.getForMember(slug, tenant, site, auth.getName()); }
    @PostMapping("/{slug}/submissions") @ResponseStatus(HttpStatus.CREATED) public FormSubmissionResponse submit(@PathVariable String slug, @RequestHeader("X-Tenant-Key") String tenant, @RequestHeader(value="X-Site-Key",required=false) String site, @RequestHeader("Idempotency-Key") String key, Authentication auth, @RequestBody Map<String,Object> data) { return service.submitForMember(slug, tenant, site, auth.getName(), key, data); }
    @DeleteMapping("/{slug}") @ResponseStatus(HttpStatus.NO_CONTENT) public void archive(@PathVariable String slug, @RequestHeader("X-Tenant-Key") String tenant, @RequestHeader(value="X-Site-Key",required=false) String site, Authentication auth) { service.archive(slug, tenant, site, auth.getName()); }
}
