package com.cyancoder.ssootp.controller;

import com.cyancoder.sso.common.dto.OtpSendRequest;
import com.cyancoder.sso.common.dto.OtpSendResponse;
import com.cyancoder.sso.common.dto.OtpVerifyRequest;
import com.cyancoder.sso.common.dto.OtpVerifyResponse;
import com.cyancoder.ssootp.service.OtpService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sso/otp")
public class OtpController {

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/send")
    public OtpSendResponse send(@RequestBody OtpSendRequest request) {
        return otpService.send(request);
    }

    @PostMapping("/verify")
    public OtpVerifyResponse verify(@RequestBody OtpVerifyRequest request) {
        return otpService.verify(request);
    }
}
