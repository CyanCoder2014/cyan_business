package com.cyancoder.storefront.controller;

import com.cyancoder.storefront.api.PublishedFormContracts.*;
import com.cyancoder.storefront.service.PublishedFormService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/public/forms")
public class PublicFormController {
    private final PublishedFormService service;
    public PublicFormController(PublishedFormService service) { this.service = service; }
    @GetMapping("/{slug}") public PublishedFormView get(@PathVariable String slug) { return service.getPublic(slug); }
    @PostMapping("/{slug}/submissions") @ResponseStatus(HttpStatus.CREATED) public FormSubmissionResponse submit(@PathVariable String slug, @RequestHeader("Idempotency-Key") String key, @RequestBody Map<String,Object> data) { return service.submitPublic(slug, key, data); }
}
