package com.cyancoder.ssouser.service;

import com.cyancoder.sso.common.dto.ClientSummary;
import com.cyancoder.sso.common.dto.ClientUpsertRequest;
import com.cyancoder.sso.common.dto.IamClientAccessSummary;
import com.cyancoder.sso.common.dto.IamUserAccessSummary;
import com.cyancoder.sso.common.dto.ManagedUserProvisionRequest;
import com.cyancoder.sso.common.dto.RealmSummary;
import com.cyancoder.sso.common.dto.RealmUpsertRequest;
import com.cyancoder.sso.common.dto.RoleCatalogSummary;
import com.cyancoder.sso.common.dto.RoleCatalogUpsertRequest;
import com.cyancoder.sso.common.dto.UserRegistrationRequest;
import com.cyancoder.sso.common.dto.UserClientRoleAssignmentSummary;
import com.cyancoder.sso.common.dto.UserRealmMembershipSummary;
import com.cyancoder.sso.common.dto.UserRealmMembershipUpsertRequest;
import com.cyancoder.sso.common.dto.UserRoleAssignmentRequest;
import com.cyancoder.sso.common.dto.UserSummary;
import com.cyancoder.ssouser.entity.ClientEntity;
import com.cyancoder.ssouser.entity.ClientRoleEntity;
import com.cyancoder.ssouser.entity.RealmEntity;
import com.cyancoder.ssouser.entity.RealmRoleEntity;
import com.cyancoder.ssouser.entity.StoredUserEntity;
import com.cyancoder.ssouser.entity.UserClientRoleAssignmentEntity;
import com.cyancoder.ssouser.entity.UserRealmMembershipEntity;
import com.cyancoder.ssouser.entity.UserRealmRoleAssignmentEntity;
import com.cyancoder.ssouser.repository.ClientRepository;
import com.cyancoder.ssouser.repository.ClientRoleRepository;
import com.cyancoder.ssouser.repository.RealmRepository;
import com.cyancoder.ssouser.repository.RealmRoleRepository;
import com.cyancoder.ssouser.repository.StoredUserRepository;
import com.cyancoder.ssouser.repository.UserClientRoleAssignmentRepository;
import com.cyancoder.ssouser.repository.UserRealmMembershipRepository;
import com.cyancoder.ssouser.repository.UserRealmRoleAssignmentRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
public class IamDirectoryService {
    private static final String DEFAULT_REALM_KEY = "cyan";
    private static final String DEFAULT_CLIENT_ID = "cyan-panel";
    private static final String DEFAULT_PUBLIC_REALM_ROLE = "realm-user";
    private static final String DEFAULT_PUBLIC_CLIENT_ROLE = "client-owner";

    private final RealmRepository realmRepository;
    private final ClientRepository clientRepository;
    private final RealmRoleRepository realmRoleRepository;
    private final ClientRoleRepository clientRoleRepository;
    private final UserRealmMembershipRepository userRealmMembershipRepository;
    private final UserRealmRoleAssignmentRepository userRealmRoleAssignmentRepository;
    private final UserClientRoleAssignmentRepository userClientRoleAssignmentRepository;
    private final StoredUserRepository storedUserRepository;
    private final UserDirectoryService userDirectoryService;
    private final IamSecurityService iamSecurityService;

    public IamDirectoryService(
            RealmRepository realmRepository,
            ClientRepository clientRepository,
            RealmRoleRepository realmRoleRepository,
            ClientRoleRepository clientRoleRepository,
            UserRealmMembershipRepository userRealmMembershipRepository,
            UserRealmRoleAssignmentRepository userRealmRoleAssignmentRepository,
            UserClientRoleAssignmentRepository userClientRoleAssignmentRepository,
            StoredUserRepository storedUserRepository,
            UserDirectoryService userDirectoryService,
            IamSecurityService iamSecurityService
    ) {
        this.realmRepository = realmRepository;
        this.clientRepository = clientRepository;
        this.realmRoleRepository = realmRoleRepository;
        this.clientRoleRepository = clientRoleRepository;
        this.userRealmMembershipRepository = userRealmMembershipRepository;
        this.userRealmRoleAssignmentRepository = userRealmRoleAssignmentRepository;
        this.userClientRoleAssignmentRepository = userClientRoleAssignmentRepository;
        this.storedUserRepository = storedUserRepository;
        this.userDirectoryService = userDirectoryService;
        this.iamSecurityService = iamSecurityService;
    }

    @PostConstruct
    void seedDefaults() {
        if (realmRepository.count() == 0) {
            RealmEntity realm = new RealmEntity();
            realm.setRealmKey("cyan");
            realm.setDisplayName("Cyan Realm");
            realm.setDescription("Default local realm");
            realm.setActive(true);
            realmRepository.save(realm);
        }
        if (clientRepository.count() == 0) {
            ClientEntity client = new ClientEntity();
            client.setClientId("cyan-panel");
            client.setRealmKey("cyan");
            client.setDisplayName("Cyan Panel");
            client.setDescription("Default panel client");
            client.setActive(true);
            client.setPublicClient(true);
            client.setRedirectUris(List.of("http://localhost:3000/*"));
            clientRepository.save(client);
        }
        if (realmRoleRepository.count() == 0) {
            saveSeedRealmRole("cyan", "super-admin", "Super Admin", "Full platform administration", List.of("*"));
            saveSeedRealmRole("cyan", "reseller-admin", "Reseller Admin", "Can create and manage clients and their users", List.of("client:create", "client:manage", "user:create", "user:manage", "client-role:catalog", "client-role:assign", "realm:read"));
            saveSeedRealmRole("cyan", "realm-admin", "Realm Admin", "Full realm administration", List.of("realm:manage", "client:create", "client:manage", "user:create", "user:manage", "realm-role:catalog", "realm-role:assign", "client-role:catalog", "client-role:assign"));
            saveSeedRealmRole("cyan", "realm-user", "Realm User", "Default realm access", List.of("profile:read"));
        }
        if (clientRoleRepository.count() == 0) {
            saveSeedClientRole("cyan-panel", "client-owner", "Client Owner", "Owns the client workspace and can manage sub users", List.of("client:manage", "user:create", "user:manage", "client-role:assign", "builder:*", "operations:*", "commerce:*", "panel:*"));
            saveSeedClientRole("cyan-panel", "client-admin", "Client Admin", "Can manage users and operate builders for a client", List.of("user:create", "user:manage", "client-role:assign", "builder:*", "operations:*", "commerce:*", "panel:read"));
            saveSeedClientRole("cyan-panel", "builder", "Builder", "Can build web, app, bot, and flows", List.of("builder:*", "panel:read"));
            saveSeedClientRole("cyan-panel", "operator", "Operator", "Can run daily operations and builders", List.of("builder:use", "operations:*", "commerce:*", "panel:read"));
            saveSeedClientRole("cyan-panel", "viewer", "Viewer", "Read-only panel access", List.of("panel:read"));
        }
        if (storedUserRepository.findById("cyan-admin").isPresent()) {
            saveSeedMembership("cyan-admin", "cyan", true, true);
            saveSeedRealmAssignment("cyan-admin", "cyan", "super-admin");
            saveSeedClientAssignment("cyan-admin", "cyan-panel", "client-owner");
        }
        if (storedUserRepository.findById("cyan-user").isPresent()) {
            saveSeedMembership("cyan-user", "cyan", true, true);
            saveSeedRealmAssignment("cyan-user", "cyan", "realm-user");
            saveSeedClientAssignment("cyan-user", "cyan-panel", "builder");
        }
    }

    @Transactional
    public RealmSummary upsertRealm(RealmUpsertRequest request) {
        iamSecurityService.requirePlatformAdmin();
        RealmEntity entity = realmRepository.findById(required(request.realmKey(), "realmKey")).orElseGet(RealmEntity::new);
        entity.setRealmKey(request.realmKey());
        entity.setDisplayName(required(request.displayName(), "displayName"));
        entity.setDescription(request.description());
        entity.setActive(request.active());
        return toSummary(realmRepository.save(entity));
    }

    public List<RealmSummary> listRealms() {
        if (iamSecurityService.isSuperAdmin()) {
            return realmRepository.findAll().stream().map(this::toSummary).toList();
        }
        String realmKey = iamSecurityService.currentRealmKey();
        return realmRepository.findById(realmKey).stream().map(this::toSummary).toList();
    }

    @Transactional
    public ClientSummary upsertClient(ClientUpsertRequest request) {
        if (realmRepository.existsById(required(request.realmKey(), "realmKey"))) {
            iamSecurityService.requireRealmManager(request.realmKey());
        } else {
            iamSecurityService.requirePlatformAdmin();
        }
        RealmEntity realm = realmRepository.findById(required(request.realmKey(), "realmKey"))
                .orElseThrow(() -> new IllegalArgumentException("Realm not found"));
        if (!realm.isActive()) {
            throw new IllegalArgumentException("Realm is inactive");
        }
        ClientEntity entity = clientRepository.findById(required(request.clientId(), "clientId")).orElseGet(ClientEntity::new);
        entity.setClientId(request.clientId());
        entity.setRealmKey(request.realmKey());
        entity.setDisplayName(required(request.displayName(), "displayName"));
        entity.setDescription(request.description());
        entity.setActive(request.active());
        entity.setPublicClient(request.publicClient());
        entity.setRedirectUris(request.redirectUris() == null ? List.of() : request.redirectUris().stream().filter(item -> item != null && !item.isBlank()).toList());
        return toSummary(clientRepository.save(entity));
    }

    public List<ClientSummary> listClients(String realmKey) {
        String resolvedRealmKey = realmKey;
        if (!iamSecurityService.isSuperAdmin()) {
            resolvedRealmKey = iamSecurityService.currentRealmKey();
        }
        List<ClientEntity> clients = resolvedRealmKey == null || resolvedRealmKey.isBlank()
                ? clientRepository.findAll()
                : clientRepository.findByRealmKeyOrderByClientIdAsc(resolvedRealmKey);
        return clients.stream().map(this::toSummary).toList();
    }

    @Transactional
    public RoleCatalogSummary upsertRealmRole(RoleCatalogUpsertRequest request) {
        iamSecurityService.requireRealmManager(request.scopeKey());
        realmRepository.findById(required(request.scopeKey(), "scopeKey"))
                .orElseThrow(() -> new IllegalArgumentException("Realm not found"));
        RealmRoleEntity entity = realmRoleRepository.findByRealmKeyOrderByRoleKeyAsc(request.scopeKey()).stream()
                .filter(item -> item.getRoleKey().equals(request.roleKey()))
                .findFirst()
                .orElseGet(RealmRoleEntity::new);
        entity.setRealmKey(request.scopeKey());
        entity.setRoleKey(required(request.roleKey(), "roleKey"));
        entity.setDisplayName(required(request.displayName(), "displayName"));
        entity.setDescription(request.description());
        entity.setActive(request.active());
        entity.setPermissions(normalizeList(request.permissions()));
        return toSummary(realmRoleRepository.save(entity));
    }

    @Transactional
    public RoleCatalogSummary upsertClientRole(RoleCatalogUpsertRequest request) {
        ClientEntity client = clientRepository.findById(required(request.scopeKey(), "scopeKey"))
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        iamSecurityService.requireClientManager(client.getRealmKey(), client.getClientId());
        ClientRoleEntity entity = clientRoleRepository.findByClientIdOrderByRoleKeyAsc(request.scopeKey()).stream()
                .filter(item -> item.getRoleKey().equals(request.roleKey()))
                .findFirst()
                .orElseGet(ClientRoleEntity::new);
        entity.setClientId(request.scopeKey());
        entity.setRoleKey(required(request.roleKey(), "roleKey"));
        entity.setDisplayName(required(request.displayName(), "displayName"));
        entity.setDescription(request.description());
        entity.setActive(request.active());
        entity.setPermissions(normalizeList(request.permissions()));
        return toSummary(clientRoleRepository.save(entity));
    }

    public List<RoleCatalogSummary> listRealmRoles(String realmKey) {
        if (!iamSecurityService.isSuperAdmin() && !iamSecurityService.currentRealmKey().equals(realmKey)) {
            throw new IllegalArgumentException("Cross-realm role catalog access is not allowed");
        }
        return realmRoleRepository.findByRealmKeyOrderByRoleKeyAsc(realmKey).stream().map(this::toSummary).toList();
    }

    public List<RoleCatalogSummary> listClientRoles(String clientId) {
        ClientEntity client = clientRepository.findById(clientId).orElseThrow(() -> new IllegalArgumentException("Client not found"));
        if (!iamSecurityService.isSuperAdmin() && !iamSecurityService.currentRealmKey().equals(client.getRealmKey())) {
            throw new IllegalArgumentException("Cross-realm client role access is not allowed");
        }
        return clientRoleRepository.findByClientIdOrderByRoleKeyAsc(clientId).stream().map(this::toSummary).toList();
    }

    @Transactional
    public UserRealmMembershipSummary upsertMembership(UserRealmMembershipUpsertRequest request) {
        iamSecurityService.requireRealmManager(request.realmKey());
        StoredUserEntity user = storedUserRepository.findById(required(request.username(), "username"))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!user.isActive()) {
            throw new IllegalArgumentException("User is inactive");
        }
        realmRepository.findById(required(request.realmKey(), "realmKey"))
                .orElseThrow(() -> new IllegalArgumentException("Realm not found"));
        UserRealmMembershipEntity entity = userRealmMembershipRepository.findByUsernameAndRealmKey(request.username(), request.realmKey())
                .orElseGet(UserRealmMembershipEntity::new);
        entity.setUsername(request.username());
        entity.setRealmKey(request.realmKey());
        entity.setActive(request.active());
        entity.setDefaultRealm(request.defaultRealm());
        if (request.defaultRealm()) {
            userRealmMembershipRepository.findByUsernameOrderByRealmKeyAsc(request.username())
                    .forEach(item -> {
                        if (!item.getRealmKey().equals(request.realmKey()) && item.isDefaultRealm()) {
                            item.setDefaultRealm(false);
                            userRealmMembershipRepository.save(item);
                        }
                    });
        }
        return toSummary(userRealmMembershipRepository.save(entity));
    }

    public List<UserRealmMembershipSummary> listMemberships(String username, String realmKey) {
        List<UserRealmMembershipEntity> items;
        if (iamSecurityService.isSuperAdmin()) {
            if (username != null && !username.isBlank()) {
                items = userRealmMembershipRepository.findByUsernameOrderByRealmKeyAsc(username);
            } else if (realmKey != null && !realmKey.isBlank()) {
                items = userRealmMembershipRepository.findByRealmKeyOrderByUsernameAsc(realmKey);
            } else {
                items = userRealmMembershipRepository.findAll();
            }
        } else if (username != null && !username.isBlank()) {
            String targetRealm = realmKey == null || realmKey.isBlank() ? iamSecurityService.currentRealmKey() : realmKey;
            if (!iamSecurityService.currentUsername().equals(username)) {
                iamSecurityService.requireRealmManager(targetRealm);
            }
            items = userRealmMembershipRepository.findByUsernameOrderByRealmKeyAsc(username);
        } else {
            items = userRealmMembershipRepository.findByRealmKeyOrderByUsernameAsc(iamSecurityService.currentRealmKey());
        }
        return items.stream().map(this::toSummary).toList();
    }

    @Transactional
    public IamUserAccessSummary assignRealmRole(String realmKey, UserRoleAssignmentRequest request) {
        iamSecurityService.requireRealmManager(realmKey);
        userRealmMembershipRepository.findByUsernameAndRealmKey(required(request.username(), "username"), required(realmKey, "realmKey"))
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of the realm"));
        realmRoleRepository.findByRealmKeyOrderByRoleKeyAsc(realmKey).stream()
                .filter(item -> item.getRoleKey().equals(required(request.roleKey(), "roleKey")))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Realm role not found"));
        UserRealmRoleAssignmentEntity entity = new UserRealmRoleAssignmentEntity();
        entity.setUsername(request.username());
        entity.setRealmKey(realmKey);
        entity.setRoleKey(request.roleKey());
        userRealmRoleAssignmentRepository.save(entity);
        return resolveAccess(request.username(), null);
    }

    @Transactional
    public IamUserAccessSummary assignClientRole(String clientId, UserRoleAssignmentRequest request) {
        ClientEntity client = clientRepository.findById(required(clientId, "clientId"))
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        iamSecurityService.requireClientManager(client.getRealmKey(), clientId);
        userRealmMembershipRepository.findByUsernameAndRealmKey(required(request.username(), "username"), client.getRealmKey())
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of the client realm"));
        clientRoleRepository.findByClientIdOrderByRoleKeyAsc(clientId).stream()
                .filter(item -> item.getRoleKey().equals(required(request.roleKey(), "roleKey")))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Client role not found"));
        UserClientRoleAssignmentEntity entity = new UserClientRoleAssignmentEntity();
        entity.setUsername(request.username());
        entity.setClientId(clientId);
        entity.setRoleKey(request.roleKey());
        userClientRoleAssignmentRepository.save(entity);
        return resolveAccessInternal(request.username(), clientId);
    }

    public IamUserAccessSummary resolveAccess(String username, String clientId) {
        if (!iamSecurityService.currentUsername().equals(username) && !iamSecurityService.hasRealmRole("super-admin")) {
            String targetRealm = resolveRealmKeyForSubject(username, clientId);
            if (clientId != null && !clientId.isBlank()) {
                iamSecurityService.requireClientManager(targetRealm, clientId);
            } else {
                iamSecurityService.requireRealmManager(targetRealm);
            }
        }
        return resolveAccessInternal(username, clientId);
    }

    public IamUserAccessSummary resolveAccessInternal(String username, String clientId) {
        StoredUserEntity user = storedUserRepository.findById(required(username, "username"))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String resolvedRealmKey = resolveRealmKey(user, clientId);
        List<String> realmRoles = userRealmRoleAssignmentRepository.findByUsernameAndRealmKeyOrderByRoleKeyAsc(username, resolvedRealmKey)
                .stream().map(UserRealmRoleAssignmentEntity::getRoleKey).toList();
        List<String> realmPermissions = permissionsForRealm(resolvedRealmKey, realmRoles);
        List<IamClientAccessSummary> clients = new ArrayList<>();
        List<ClientEntity> realmClients = clientRepository.findByRealmKeyOrderByClientIdAsc(resolvedRealmKey);
        for (ClientEntity client : realmClients) {
            if (clientId != null && !clientId.isBlank() && !client.getClientId().equals(clientId)) {
                continue;
            }
            List<String> clientRoles = userClientRoleAssignmentRepository.findByUsernameAndClientIdOrderByRoleKeyAsc(username, client.getClientId())
                    .stream().map(UserClientRoleAssignmentEntity::getRoleKey).toList();
            clients.add(new IamClientAccessSummary(
                    client.getClientId(),
                    client.getRealmKey(),
                    clientRoles,
                    permissionsForClient(client.getClientId(), clientRoles)
            ));
        }
        return new IamUserAccessSummary(username, resolvedRealmKey, realmRoles, realmPermissions, clients);
    }

    public List<UserClientRoleAssignmentSummary> listClientAssignments(String username) {
        if (!iamSecurityService.isSuperAdmin() && !iamSecurityService.currentUsername().equals(username)) {
            String targetRealm = resolveRealmKeyForSubject(username, null);
            iamSecurityService.requireRealmManager(targetRealm);
        }
        Map<String, List<String>> grouped = new LinkedHashMap<>();
        userClientRoleAssignmentRepository.findByUsernameOrderByClientIdAscRoleKeyAsc(username)
                .forEach(item -> grouped.computeIfAbsent(item.getClientId(), ignored -> new ArrayList<>()).add(item.getRoleKey()));
        return grouped.entrySet().stream()
                .map(entry -> new UserClientRoleAssignmentSummary(username, entry.getKey(), entry.getValue()))
                .toList();
    }

    @Transactional
    public UserSummary registerPublicUser(UserRegistrationRequest request) {
        String username = request.username();
        if (username == null || username.isBlank()) {
            username = request.email();
        }

        realmRepository.findById(DEFAULT_REALM_KEY)
                .orElseThrow(() -> new IllegalStateException("Default realm is not configured"));
        ClientEntity client = clientRepository.findById(DEFAULT_CLIENT_ID)
                .orElseThrow(() -> new IllegalStateException("Default panel client is not configured"));
        if (!DEFAULT_REALM_KEY.equals(client.getRealmKey())) {
            throw new IllegalStateException("Default panel client does not belong to the default realm");
        }

        UserSummary user = userDirectoryService.register(new UserRegistrationRequest(
                required(username, "username"),
                required(request.password(), "password"),
                request.email(),
                request.phoneNumber(),
                false,
                List.of("user")
        ));

        saveSeedMembership(user.username(), DEFAULT_REALM_KEY, true, true);
        saveSeedRealmAssignment(user.username(), DEFAULT_REALM_KEY, DEFAULT_PUBLIC_REALM_ROLE);
        saveSeedClientAssignment(user.username(), DEFAULT_CLIENT_ID, DEFAULT_PUBLIC_CLIENT_ROLE);
        return user;
    }

    @Transactional
    public IamUserAccessSummary provisionManagedUser(ManagedUserProvisionRequest request) {
        String realmKey = required(request.realmKey(), "realmKey");
        String clientId = request.clientId();
        iamSecurityService.requireClientScopedUserProvision(realmKey, clientId);

        if (clientId != null && !clientId.isBlank()) {
            ClientEntity client = clientRepository.findById(clientId).orElseThrow(() -> new IllegalArgumentException("Client not found"));
            if (!client.getRealmKey().equals(realmKey)) {
                throw new IllegalArgumentException("Client does not belong to the requested realm");
            }
        }

        userDirectoryService.register(new com.cyancoder.sso.common.dto.UserRegistrationRequest(
                required(request.username(), "username"),
                required(request.password(), "password"),
                request.email(),
                request.phoneNumber(),
                request.mfaEnabled(),
                List.of("user")
        ));
        UserRealmMembershipEntity membership = new UserRealmMembershipEntity();
        membership.setUsername(request.username());
        membership.setRealmKey(realmKey);
        membership.setActive(true);
        membership.setDefaultRealm(false);
        userRealmMembershipRepository.save(membership);

        if (request.realmRoles() != null) {
            for (String role : request.realmRoles()) {
                if (role != null && !role.isBlank()) {
                    assignRealmRole(realmKey, new com.cyancoder.sso.common.dto.UserRoleAssignmentRequest(request.username(), role));
                }
            }
        }
        if (clientId != null && !clientId.isBlank() && request.clientRoles() != null) {
            for (String role : request.clientRoles()) {
                if (role != null && !role.isBlank()) {
                    assignClientRole(clientId, new com.cyancoder.sso.common.dto.UserRoleAssignmentRequest(request.username(), role));
                }
            }
        }
        return resolveAccessInternal(request.username(), clientId);
    }

    private String resolveRealmKeyForSubject(String username, String clientId) {
        StoredUserEntity user = storedUserRepository.findById(required(username, "username"))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return resolveRealmKey(user, clientId);
    }

    private String resolveRealmKey(StoredUserEntity user, String clientId) {
        if (clientId != null && !clientId.isBlank()) {
            ClientEntity client = clientRepository.findById(clientId).orElseThrow(() -> new IllegalArgumentException("Client not found"));
            UserRealmMembershipEntity membership = userRealmMembershipRepository.findByUsernameAndRealmKey(user.getUsername(), client.getRealmKey())
                    .orElseThrow(() -> new IllegalArgumentException("User is not a member of the client realm"));
            if (!membership.isActive()) {
                throw new IllegalArgumentException("User realm membership is inactive");
            }
            return client.getRealmKey();
        }
        return userRealmMembershipRepository.findByUsernameOrderByRealmKeyAsc(user.getUsername()).stream()
                .filter(UserRealmMembershipEntity::isActive)
                .sorted((left, right) -> Boolean.compare(right.isDefaultRealm(), left.isDefaultRealm()))
                .map(UserRealmMembershipEntity::getRealmKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User has no active realm membership"));
    }

    private List<String> permissionsForRealm(String realmKey, List<String> roles) {
        LinkedHashSet<String> permissions = new LinkedHashSet<>();
        for (RealmRoleEntity entity : realmRoleRepository.findByRealmKeyOrderByRoleKeyAsc(realmKey)) {
            if (roles.contains(entity.getRoleKey())) {
                permissions.addAll(normalizeList(entity.getPermissions()));
            }
        }
        return List.copyOf(permissions);
    }

    private List<String> permissionsForClient(String clientId, List<String> roles) {
        LinkedHashSet<String> permissions = new LinkedHashSet<>();
        for (ClientRoleEntity entity : clientRoleRepository.findByClientIdOrderByRoleKeyAsc(clientId)) {
            if (roles.contains(entity.getRoleKey())) {
                permissions.addAll(normalizeList(entity.getPermissions()));
            }
        }
        return List.copyOf(permissions);
    }

    private RealmSummary toSummary(RealmEntity entity) {
        return new RealmSummary(entity.getRealmKey(), entity.getDisplayName(), entity.getDescription(), entity.isActive());
    }

    private ClientSummary toSummary(ClientEntity entity) {
        return new ClientSummary(entity.getClientId(), entity.getRealmKey(), entity.getDisplayName(), entity.getDescription(), entity.isActive(), entity.isPublicClient(), normalizeList(entity.getRedirectUris()));
    }

    private RoleCatalogSummary toSummary(RealmRoleEntity entity) {
        return new RoleCatalogSummary("REALM", entity.getRealmKey(), entity.getRoleKey(), entity.getDisplayName(), entity.getDescription(), entity.isActive(), normalizeList(entity.getPermissions()));
    }

    private RoleCatalogSummary toSummary(ClientRoleEntity entity) {
        return new RoleCatalogSummary("CLIENT", entity.getClientId(), entity.getRoleKey(), entity.getDisplayName(), entity.getDescription(), entity.isActive(), normalizeList(entity.getPermissions()));
    }

    private UserRealmMembershipSummary toSummary(UserRealmMembershipEntity entity) {
        return new UserRealmMembershipSummary(entity.getUsername(), entity.getRealmKey(), entity.isActive(), entity.isDefaultRealm());
    }

    private List<String> normalizeList(List<String> values) {
        return values == null ? List.of() : values.stream().filter(item -> item != null && !item.isBlank()).distinct().toList();
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private void saveSeedRealmRole(String realmKey, String roleKey, String displayName, String description, List<String> permissions) {
        RealmRoleEntity entity = new RealmRoleEntity();
        entity.setRealmKey(realmKey);
        entity.setRoleKey(roleKey);
        entity.setDisplayName(displayName);
        entity.setDescription(description);
        entity.setActive(true);
        entity.setPermissions(permissions);
        realmRoleRepository.save(entity);
    }

    private void saveSeedClientRole(String clientId, String roleKey, String displayName, String description, List<String> permissions) {
        ClientRoleEntity entity = new ClientRoleEntity();
        entity.setClientId(clientId);
        entity.setRoleKey(roleKey);
        entity.setDisplayName(displayName);
        entity.setDescription(description);
        entity.setActive(true);
        entity.setPermissions(permissions);
        clientRoleRepository.save(entity);
    }

    private void saveSeedMembership(String username, String realmKey, boolean active, boolean defaultRealm) {
        UserRealmMembershipEntity entity = new UserRealmMembershipEntity();
        entity.setUsername(username);
        entity.setRealmKey(realmKey);
        entity.setActive(active);
        entity.setDefaultRealm(defaultRealm);
        userRealmMembershipRepository.save(entity);
    }

    private void saveSeedRealmAssignment(String username, String realmKey, String roleKey) {
        UserRealmRoleAssignmentEntity entity = new UserRealmRoleAssignmentEntity();
        entity.setUsername(username);
        entity.setRealmKey(realmKey);
        entity.setRoleKey(roleKey);
        userRealmRoleAssignmentRepository.save(entity);
    }

    private void saveSeedClientAssignment(String username, String clientId, String roleKey) {
        UserClientRoleAssignmentEntity entity = new UserClientRoleAssignmentEntity();
        entity.setUsername(username);
        entity.setClientId(clientId);
        entity.setRoleKey(roleKey);
        userClientRoleAssignmentRepository.save(entity);
    }
}
