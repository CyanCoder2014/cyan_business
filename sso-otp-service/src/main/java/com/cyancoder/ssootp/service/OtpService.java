package com.cyancoder.ssootp.service;

import com.cyancoder.sso.common.dto.OtpSendRequest;
import com.cyancoder.sso.common.dto.OtpSendResponse;
import com.cyancoder.sso.common.dto.OtpVerifyRequest;
import com.cyancoder.sso.common.dto.OtpVerifyResponse;
import com.cyancoder.ssootp.entity.OtpCodeEntity;
import com.cyancoder.ssootp.repository.OtpCodeRepository;
import com.cyancoder.ssootp.sms.KavenegarOtpSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OtpService {

    private final OtpCodeRepository otpCodeRepository;
    private final KavenegarOtpSender otpSender;
    private final boolean exposeDevCode;

    public OtpService(
            OtpCodeRepository otpCodeRepository,
            KavenegarOtpSender otpSender,
            @Value("${otp.expose-dev-code:false}") boolean exposeDevCode
    ) {
        this.otpCodeRepository = otpCodeRepository;
        this.otpSender = otpSender;
        this.exposeDevCode = exposeDevCode;
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

        // Deliver to the phone number the caller resolved from the user record,
        // falling back to username only for legacy callers that pass a phone
        // number as the username itself.
        String deliveryTarget = request.deliveryTarget() == null || request.deliveryTarget().isBlank()
                ? request.username()
                : request.deliveryTarget();
        boolean delivered = otpSender.send(deliveryTarget, otp, request.purpose(), request.language());

        // Never echo the code back on a deployed environment. Gating this on an
        // explicit opt-in rather than on delivery failure matters: any delivery
        // failure (unconfigured template, bad receptor, Kavenegar outage) would
        // otherwise silently turn every send response into a code disclosure,
        // which defeats MFA for anyone who already has the password.
        String devCode = exposeDevCode ? otp : null;
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
