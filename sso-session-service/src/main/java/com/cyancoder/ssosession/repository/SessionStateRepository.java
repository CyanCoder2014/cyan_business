package com.cyancoder.ssosession.repository;

import com.cyancoder.ssosession.entity.SessionStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionStateRepository extends JpaRepository<SessionStateEntity, String> {
}
