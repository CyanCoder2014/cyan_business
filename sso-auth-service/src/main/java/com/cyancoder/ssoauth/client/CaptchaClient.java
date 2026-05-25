package com.cyancoder.ssoauth.client;

import com.cyancoder.sso.common.dto.CaptchaVerifyRequest;
import com.cyancoder.sso.common.dto.CaptchaVerifyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "sso-captcha-service")
public interface CaptchaClient {

    @PostMapping("/api/sso/captcha/verify")
    CaptchaVerifyResponse verify(@RequestBody CaptchaVerifyRequest request);
}
