package com.cyancoder.ssootp.service;

import com.cyancoder.sso.common.dto.OtpSendRequest;
import com.cyancoder.sso.common.dto.OtpSendResponse;
import com.cyancoder.sso.common.dto.OtpVerifyRequest;
import com.cyancoder.sso.common.dto.OtpVerifyResponse;
import com.cyancoder.ssootp.entity.OtpCodeEntity;
import com.cyancoder.ssootp.repository.OtpCodeRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OtpService {

    private final OtpCodeRepository otpCodeRepository;

    public OtpService(OtpCodeRepository otpCodeRepository) {
        this.otpCodeRepository = otpCodeRepository;
    }

    public OtpSendResponse send(OtpSendRequest request) {
        String key = buildKey(request.username(), request.clientId(), request.purpose());
        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        String codeId = UUID.randomUUID().toString();
        long expiresAt = Instant.now().plusSeconds(300).getEpochSecond();

        OtpCodeEntity entity = new OtpCodeEntity();
        entity.setOtpKey(key);
        entity.setCodeId(codeId);
        entity.setCode(otp);
        entity.setExpiresAtEpochSecond(expiresAt);
        otpCodeRepository.save(entity);
        return new OtpSendResponse(codeId, true, request.username(), otp);
    }

    public OtpVerifyResponse verify(OtpVerifyRequest request) {
        String key = buildKey(request.username(), request.clientId(), request.purpose());
        OtpCodeEntity storedOtp = otpCodeRepository.findById(key).orElse(null);
        if (storedOtp == null) {
            return new OtpVerifyResponse(false, "OTP not found");
        }
        if (storedOtp.getExpiresAtEpochSecond() < Instant.now().getEpochSecond()) {
            otpCodeRepository.deleteById(key);
            return new OtpVerifyResponse(false, "OTP expired");
        }
        if (!storedOtp.getCode().equals(request.code())) {
            return new OtpVerifyResponse(false, "OTP invalid");
        }
        otpCodeRepository.deleteById(key);
        return new OtpVerifyResponse(true, "OTP verified");
    }

    private String buildKey(String username, String clientId, String purpose) {
        return username + "::" + clientId + "::" + purpose;
    }
}
