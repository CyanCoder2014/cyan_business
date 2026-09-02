package com.cyancoder.ssouser.repository;

import com.cyancoder.ssouser.entity.UserClientRoleAssignmentEntity;
import com.cyancoder.ssouser.entity.UserClientRoleAssignmentEntity.UserClientRoleAssignmentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserClientRoleAssignmentRepository extends JpaRepository<UserClientRoleAssignmentEntity, UserClientRoleAssignmentId> {
    List<UserClientRoleAssignmentEntity> findByUsernameAndClientIdOrderByRoleKeyAsc(String username, String clientId);
    List<UserClientRoleAssignmentEntity> findByUsernameOrderByClientIdAscRoleKeyAsc(String username);
    List<UserClientRoleAssignmentEntity> findByUsernameAndClientIdAndActiveTrueOrderByRoleKeyAsc(String username, String clientId);
    java.util.Optional<UserClientRoleAssignmentEntity> findByUsernameAndClientIdAndRoleKey(String username, String clientId, String roleKey);
    List<UserClientRoleAssignmentEntity> findByUsernameAndActiveTrueOrderByClientIdAscRoleKeyAsc(String username);
}
