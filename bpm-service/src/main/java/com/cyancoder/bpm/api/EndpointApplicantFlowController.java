package com.cyancoder.bpm.api;

import com.cyancoder.bpm.api.dto.*;
import com.cyancoder.bpm.service.ActorContextResolver;
import com.cyancoder.bpm.service.ObjectFlowService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/** Definition-controlled applicant surface; deliberately separate from operator managed-object APIs. */
@RestController
@RequestMapping("/endpoint/bpm/applications")
public class EndpointApplicantFlowController {
    private final ObjectFlowService flows;
    private final ActorContextResolver actors;
    public EndpointApplicantFlowController(ObjectFlowService flows, ActorContextResolver actors) { this.flows = flows; this.actors = actors; }
    @PostMapping
    public ApplicantStatusResponse start(@RequestHeader(value="X-Tenant-Key", required=false) String tenant, @RequestHeader(value="X-Site-Key", required=false) String site,
                                         @RequestBody ApplicantStartRequest request, Authentication auth) {
        return flows.startApplicant(FlowScopeResolver.fromHeaders(tenant, site), request, actors.fromAuthentication(auth));
    }
    @GetMapping("/{objectId}")
    public ApplicantStatusResponse status(@RequestHeader(value="X-Tenant-Key", required=false) String tenant, @RequestHeader(value="X-Site-Key", required=false) String site,
                                           @PathVariable String objectId, Authentication auth) {
        return flows.applicantStatus(FlowScopeResolver.fromHeaders(tenant, site), objectId, actors.fromAuthentication(auth));
    }
    @GetMapping("/{objectId}/active-form")
    public ApplicantActiveFormResponse activeForm(@RequestHeader(value="X-Tenant-Key", required=false) String tenant, @RequestHeader(value="X-Site-Key", required=false) String site,
                                                   @PathVariable String objectId, Authentication auth) {
        return flows.applicantActiveForm(FlowScopeResolver.fromHeaders(tenant, site), objectId, actors.fromAuthentication(auth));
    }
    @PostMapping("/{objectId}/active-form/submissions")
    public ApplicantStatusResponse submit(@RequestHeader(value="X-Tenant-Key", required=false) String tenant, @RequestHeader(value="X-Site-Key", required=false) String site,
                                          @PathVariable String objectId, @RequestBody Map<String,Object> formData, Authentication auth) {
        return flows.submitApplicantForm(FlowScopeResolver.fromHeaders(tenant, site), objectId, formData, actors.fromAuthentication(auth));
    }
}
