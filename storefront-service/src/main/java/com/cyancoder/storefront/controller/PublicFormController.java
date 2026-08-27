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
    @GetMapping("/{slug}/relations/{fieldName}") public Map<String,Object> relationLookup(@PathVariable String slug, @PathVariable String fieldName, @RequestParam(value="q",required=false) String query, @RequestParam(value="page",defaultValue="0") int page, @RequestParam(value="size",defaultValue="20") int size) { return service.publicRelationLookup(slug, fieldName, query, page, size); }
    @PostMapping("/{slug}/submissions") @ResponseStatus(HttpStatus.CREATED) public FormSubmissionResponse submit(@PathVariable String slug, @RequestHeader("Idempotency-Key") String key, @RequestBody Map<String,Object> data) { return service.submitPublic(slug, key, data); }
}
