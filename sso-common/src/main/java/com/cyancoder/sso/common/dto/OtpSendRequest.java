package com.cyancoder.sso.common.dto;

public record OtpSendRequest(
        String username,
        String clientId,
        String purpose,
        /*
         * Phone number to deliver the code to. Callers must resolve this from
         * the stored user record — never from client-supplied input, or an
         * attacker could redirect a victim's OTP to their own phone.
         */
        String deliveryTarget,
        /** BCP-47-ish language tag ("fa", "en") selecting the SMS template. */
        String language
) {
    public OtpSendRequest(String username, String clientId, String purpose) {
        this(username, clientId, purpose, null, null);
    }

    public OtpSendRequest(String username, String clientId, String purpose, String deliveryTarget) {
        this(username, clientId, purpose, deliveryTarget, null);
    }
}
