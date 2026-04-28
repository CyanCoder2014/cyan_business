package com.cyancoder.ssootp.repository;

import com.cyancoder.ssootp.entity.OtpCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpCodeRepository extends JpaRepository<OtpCodeEntity, String> {
}
