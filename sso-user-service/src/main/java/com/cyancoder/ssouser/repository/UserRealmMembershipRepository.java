package com.cyancoder.ssouser.repository;

import com.cyancoder.ssouser.entity.UserRealmMembershipEntity;
import com.cyancoder.ssouser.entity.UserRealmMembershipEntity.UserRealmMembershipId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRealmMembershipRepository extends JpaRepository<UserRealmMembershipEntity, UserRealmMembershipId> {
    List<UserRealmMembershipEntity> findByUsernameOrderByRealmKeyAsc(String username);
    List<UserRealmMembershipEntity> findByRealmKeyOrderByUsernameAsc(String realmKey);
    Optional<UserRealmMembershipEntity> findByUsernameAndRealmKey(String username, String realmKey);
}
