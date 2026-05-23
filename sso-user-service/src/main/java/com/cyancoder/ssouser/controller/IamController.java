package com.cyancoder.ssouser.controller;

import com.cyancoder.sso.common.dto.ClientSummary;
import com.cyancoder.sso.common.dto.ClientUpsertRequest;
import com.cyancoder.sso.common.dto.IamUserAccessSummary;
import com.cyancoder.sso.common.dto.RealmSummary;
import com.cyancoder.sso.common.dto.RealmUpsertRequest;
import com.cyancoder.sso.common.dto.RoleCatalogSummary;
import com.cyancoder.sso.common.dto.RoleCatalogUpsertRequest;
import com.cyancoder.sso.common.dto.UserClientRoleAssignmentSummary;
import com.cyancoder.sso.common.dto.UserRealmMembershipSummary;
import com.cyancoder.sso.common.dto.UserRealmMembershipUpsertRequest;
import com.cyancoder.sso.common.dto.UserRoleAssignmentRequest;
import com.cyancoder.ssouser.service.IamDirectoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sso/iam")
public class IamController {
    private final IamDirectoryService iamDirectoryService;

    public IamController(IamDirectoryService iamDirectoryService) {
        this.iamDirectoryService = iamDirectoryService;
    }

    @PostMapping("/realms")
    @ResponseStatus(HttpStatus.CREATED)
    public RealmSummary upsertRealm(@RequestBody RealmUpsertRequest request) {
        return iamDirectoryService.upsertRealm(request);
    }

    @GetMapping("/realms")
    public List<RealmSummary> listRealms() {
        return iamDirectoryService.listRealms();
    }

    @PostMapping("/clients")
    @ResponseStatus(HttpStatus.CREATED)
    public ClientSummary upsertClient(@RequestBody ClientUpsertRequest request) {
        return iamDirectoryService.upsertClient(request);
    }

    @GetMapping("/clients")
    public List<ClientSummary> listClients(@RequestParam(required = false) String realmKey) {
        return iamDirectoryService.listClients(realmKey);
    }

    @PostMapping("/realm-roles")
    @ResponseStatus(HttpStatus.CREATED)
    public RoleCatalogSummary upsertRealmRole(@RequestBody RoleCatalogUpsertRequest request) {
        return iamDirectoryService.upsertRealmRole(request);
    }

    @GetMapping("/realm-roles")
    public List<RoleCatalogSummary> listRealmRoles(@RequestParam String realmKey) {
        return iamDirectoryService.listRealmRoles(realmKey);
    }

    @PostMapping("/client-roles")
    @ResponseStatus(HttpStatus.CREATED)
    public RoleCatalogSummary upsertClientRole(@RequestBody RoleCatalogUpsertRequest request) {
        return iamDirectoryService.upsertClientRole(request);
    }

    @GetMapping("/client-roles")
    public List<RoleCatalogSummary> listClientRoles(@RequestParam String clientId) {
        return iamDirectoryService.listClientRoles(clientId);
    }

    @PostMapping("/memberships")
    @ResponseStatus(HttpStatus.CREATED)
    public UserRealmMembershipSummary upsertMembership(@RequestBody UserRealmMembershipUpsertRequest request) {
        return iamDirectoryService.upsertMembership(request);
    }

    @GetMapping("/memberships")
    public List<UserRealmMembershipSummary> listMemberships(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realmKey
    ) {
        return iamDirectoryService.listMemberships(username, realmKey);
    }

    @PostMapping("/realms/{realmKey}/assign-role")
    public IamUserAccessSummary assignRealmRole(@PathVariable String realmKey, @RequestBody UserRoleAssignmentRequest request) {
        return iamDirectoryService.assignRealmRole(realmKey, request);
    }

    @PostMapping("/clients/{clientId}/assign-role")
    public IamUserAccessSummary assignClientRole(@PathVariable String clientId, @RequestBody UserRoleAssignmentRequest request) {
        return iamDirectoryService.assignClientRole(clientId, request);
    }

    @GetMapping("/users/{username}/access")
    public IamUserAccessSummary resolveAccess(@PathVariable String username, @RequestParam(required = false) String clientId) {
        return iamDirectoryService.resolveAccess(username, clientId);
    }

    @GetMapping("/users/{username}/client-assignments")
    public List<UserClientRoleAssignmentSummary> listClientAssignments(@PathVariable String username) {
        return iamDirectoryService.listClientAssignments(username);
    }
}
