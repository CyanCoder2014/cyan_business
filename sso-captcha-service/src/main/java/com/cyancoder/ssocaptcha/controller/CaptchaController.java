package com.cyancoder.ssocaptcha.controller;

import com.cyancoder.sso.common.dto.CaptchaChallengeResponse;
import com.cyancoder.sso.common.dto.CaptchaVerifyRequest;
import com.cyancoder.sso.common.dto.CaptchaVerifyResponse;
import com.cyancoder.ssocaptcha.service.CaptchaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sso/captcha")
public class CaptchaController {

    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @PostMapping("/challenges")
    public CaptchaChallengeResponse createChallenge(@RequestParam(required = false) String clientId) {
        return captchaService.createChallenge(clientId);
    }

    @PostMapping("/verify")
    public CaptchaVerifyResponse verify(@RequestBody CaptchaVerifyRequest request) {
        return captchaService.verify(request.challengeId(), request.answer(), request.clientId());
    }
}
