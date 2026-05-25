package com.cyancoder.ssoauth.client;

import com.cyancoder.sso.common.dto.OtpSendRequest;
import com.cyancoder.sso.common.dto.OtpSendResponse;
import com.cyancoder.sso.common.dto.OtpVerifyRequest;
import com.cyancoder.sso.common.dto.OtpVerifyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "sso-otp-service")
public interface OtpClient {

    @PostMapping("/api/sso/otp/send")
    OtpSendResponse send(@RequestBody OtpSendRequest request);

    @PostMapping("/api/sso/otp/verify")
    OtpVerifyResponse verify(@RequestBody OtpVerifyRequest request);
}
