package com.cyancoder.ssouser.service;

import com.cyancoder.sso.common.dto.ClientSummary;
import com.cyancoder.sso.common.dto.ClientUpsertRequest;
import com.cyancoder.sso.common.dto.IamClientAccessSummary;
import com.cyancoder.sso.common.dto.IamUserAccessSummary;
import com.cyancoder.sso.common.dto.RealmSummary;
import com.cyancoder.sso.common.dto.RealmUpsertRequest;
import com.cyancoder.sso.common.dto.RoleCatalogSummary;
import com.cyancoder.sso.common.dto.RoleCatalogUpsertRequest;
import com.cyancoder.sso.common.dto.UserClientRoleAssignmentSummary;
import com.cyancoder.sso.common.dto.UserRealmMembershipSummary;
import com.cyancoder.sso.common.dto.UserRealmMembershipUpsertRequest;
import com.cyancoder.sso.common.dto.UserRoleAssignmentRequest;
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
    private final RealmRepository realmRepository;
    private final ClientRepository clientRepository;
    private final RealmRoleRepository realmRoleRepository;
    private final ClientRoleRepository clientRoleRepository;
    private final UserRealmMembershipRepository userRealmMembershipRepository;
    private final UserRealmRoleAssignmentRepository userRealmRoleAssignmentRepository;
    private final UserClientRoleAssignmentRepository userClientRoleAssignmentRepository;
    private final StoredUserRepository storedUserRepository;

    public IamDirectoryService(
            RealmRepository realmRepository,
            ClientRepository clientRepository,
            RealmRoleRepository realmRoleRepository,
            ClientRoleRepository clientRoleRepository,
            UserRealmMembershipRepository userRealmMembershipRepository,
            UserRealmRoleAssignmentRepository userRealmRoleAssignmentRepository,
            UserClientRoleAssignmentRepository userClientRoleAssignmentRepository,
            StoredUserRepository storedUserRepository
    ) {
        this.realmRepository = realmRepository;
        this.clientRepository = clientRepository;
        this.realmRoleRepository = realmRoleRepository;
        this.clientRoleRepository = clientRoleRepository;
        this.userRealmMembershipRepository = userRealmMembershipRepository;
        this.userRealmRoleAssignmentRepository = userRealmRoleAssignmentRepository;
        this.userClientRoleAssignmentRepository = userClientRoleAssignmentRepository;
        this.storedUserRepository = storedUserRepository;
    }

    @PostConstruct
    void seedDefaults() {
        if (realmRepository.count() == 0) {
            upsertRealm(new RealmUpsertRequest("cyan", "Cyan Realm", "Default local realm", true));
        }
        if (clientRepository.count() == 0) {
            upsertClient(new ClientUpsertRequest("cyan-panel", "cyan", "Cyan Panel", "Default panel client", true, true, List.of("http://localhost:3000/*")));
        }
        if (realmRoleRepository.count() == 0) {
            upsertRealmRole(new RoleCatalogUpsertRequest("REALM", "cyan", "realm-admin", "Realm Admin", "Full realm administration", true, List.of("realm:*", "user:*")));
            upsertRealmRole(new RoleCatalogUpsertRequest("REALM", "cyan", "realm-user", "Realm User", "Default realm access", true, List.of("profile:read")));
        }
        if (clientRoleRepository.count() == 0) {
            upsertClientRole(new RoleCatalogUpsertRequest("CLIENT", "cyan-panel", "panel-admin", "Panel Admin", "Full panel access", true, List.of("panel:*", "builder:*", "iam:*")));
            upsertClientRole(new RoleCatalogUpsertRequest("CLIENT", "cyan-panel", "panel-operator", "Panel Operator", "Operational panel access", true, List.of("panel:read", "builder:use")));
        }
        if (storedUserRepository.findById("cyan-admin").isPresent()) {
            upsertMembership(new UserRealmMembershipUpsertRequest("cyan-admin", "cyan", true, true));
            assignRealmRole("cyan", new UserRoleAssignmentRequest("cyan-admin", "realm-admin"));
            assignClientRole("cyan-panel", new UserRoleAssignmentRequest("cyan-admin", "panel-admin"));
        }
        if (storedUserRepository.findById("cyan-user").isPresent()) {
            upsertMembership(new UserRealmMembershipUpsertRequest("cyan-user", "cyan", true, true));
            assignRealmRole("cyan", new UserRoleAssignmentRequest("cyan-user", "realm-user"));
            assignClientRole("cyan-panel", new UserRoleAssignmentRequest("cyan-user", "panel-operator"));
        }
    }

    @Transactional
    public RealmSummary upsertRealm(RealmUpsertRequest request) {
        RealmEntity entity = realmRepository.findById(required(request.realmKey(), "realmKey")).orElseGet(RealmEntity::new);
        entity.setRealmKey(request.realmKey());
        entity.setDisplayName(required(request.displayName(), "displayName"));
        entity.setDescription(request.description());
        entity.setActive(request.active());
        return toSummary(realmRepository.save(entity));
    }

    public List<RealmSummary> listRealms() {
        return realmRepository.findAll().stream().map(this::toSummary).toList();
    }

    @Transactional
    public ClientSummary upsertClient(ClientUpsertRequest request) {
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
        List<ClientEntity> clients = realmKey == null || realmKey.isBlank()
                ? clientRepository.findAll()
                : clientRepository.findByRealmKeyOrderByClientIdAsc(realmKey);
        return clients.stream().map(this::toSummary).toList();
    }

    @Transactional
    public RoleCatalogSummary upsertRealmRole(RoleCatalogUpsertRequest request) {
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
        clientRepository.findById(required(request.scopeKey(), "scopeKey"))
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
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
        return realmRoleRepository.findByRealmKeyOrderByRoleKeyAsc(realmKey).stream().map(this::toSummary).toList();
    }

    public List<RoleCatalogSummary> listClientRoles(String clientId) {
        return clientRoleRepository.findByClientIdOrderByRoleKeyAsc(clientId).stream().map(this::toSummary).toList();
    }

    @Transactional
    public UserRealmMembershipSummary upsertMembership(UserRealmMembershipUpsertRequest request) {
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
        if (username != null && !username.isBlank()) {
            items = userRealmMembershipRepository.findByUsernameOrderByRealmKeyAsc(username);
        } else if (realmKey != null && !realmKey.isBlank()) {
            items = userRealmMembershipRepository.findByRealmKeyOrderByUsernameAsc(realmKey);
        } else {
            items = userRealmMembershipRepository.findAll();
        }
        return items.stream().map(this::toSummary).toList();
    }

    @Transactional
    public IamUserAccessSummary assignRealmRole(String realmKey, UserRoleAssignmentRequest request) {
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
        return resolveAccess(request.username(), clientId);
    }

    public IamUserAccessSummary resolveAccess(String username, String clientId) {
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
        Map<String, List<String>> grouped = new LinkedHashMap<>();
        userClientRoleAssignmentRepository.findByUsernameOrderByClientIdAscRoleKeyAsc(username)
                .forEach(item -> grouped.computeIfAbsent(item.getClientId(), ignored -> new ArrayList<>()).add(item.getRoleKey()));
        return grouped.entrySet().stream()
                .map(entry -> new UserClientRoleAssignmentSummary(username, entry.getKey(), entry.getValue()))
                .toList();
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
}
