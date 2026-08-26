package com.cyancoder.ssootp.service;

import com.cyancoder.sso.common.dto.OtpSendRequest;
import com.cyancoder.sso.common.dto.OtpSendResponse;
import com.cyancoder.sso.common.dto.OtpVerifyRequest;
import com.cyancoder.sso.common.dto.OtpVerifyResponse;
import com.cyancoder.ssootp.entity.OtpCodeEntity;
import com.cyancoder.ssootp.repository.OtpCodeRepository;
import com.cyancoder.ssootp.sms.KavenegarOtpSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OtpService {

    private final OtpCodeRepository otpCodeRepository;
    private final KavenegarOtpSender otpSender;

    public OtpService(OtpCodeRepository otpCodeRepository, KavenegarOtpSender otpSender) {
        this.otpCodeRepository = otpCodeRepository;
        this.otpSender = otpSender;
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

        boolean delivered = otpSender.send(request.username(), otp);
        // Only echo the code back when delivery didn't actually happen, so a
        // working Kavenegar template stops leaking codes in the API response
        // while local/unconfigured environments stay usable without one.
        String devCode = delivered ? null : otp;
        return new OtpSendResponse(codeId, delivered, request.username(), devCode);
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
