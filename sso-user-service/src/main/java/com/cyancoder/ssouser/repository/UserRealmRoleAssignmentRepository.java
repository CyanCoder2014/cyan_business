package com.cyancoder.ssouser.repository;

import com.cyancoder.ssouser.entity.UserRealmRoleAssignmentEntity;
import com.cyancoder.ssouser.entity.UserRealmRoleAssignmentEntity.UserRealmRoleAssignmentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRealmRoleAssignmentRepository extends JpaRepository<UserRealmRoleAssignmentEntity, UserRealmRoleAssignmentId> {
    List<UserRealmRoleAssignmentEntity> findByUsernameAndRealmKeyOrderByRoleKeyAsc(String username, String realmKey);
    List<UserRealmRoleAssignmentEntity> findByUsernameAndRealmKeyAndActiveTrueOrderByRoleKeyAsc(String username, String realmKey);
    java.util.Optional<UserRealmRoleAssignmentEntity> findByUsernameAndRealmKeyAndRoleKey(String username, String realmKey, String roleKey);
}
