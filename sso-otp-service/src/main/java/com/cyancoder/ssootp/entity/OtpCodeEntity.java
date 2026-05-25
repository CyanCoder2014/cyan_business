package com.cyancoder.ssootp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sso_otp_codes")
public class OtpCodeEntity {

    @Id
    @Column(name = "otp_key", nullable = false, updatable = false)
    private String otpKey;

    @Column(name = "code_id", nullable = false)
    private String codeId;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "expires_at_epoch_second", nullable = false)
    private long expiresAtEpochSecond;

    public String getOtpKey() {
        return otpKey;
    }

    public void setOtpKey(String otpKey) {
        this.otpKey = otpKey;
    }

    public String getCodeId() {
        return codeId;
    }

    public void setCodeId(String codeId) {
        this.codeId = codeId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public long getExpiresAtEpochSecond() {
        return expiresAtEpochSecond;
    }

    public void setExpiresAtEpochSecond(long expiresAtEpochSecond) {
        this.expiresAtEpochSecond = expiresAtEpochSecond;
    }
}
