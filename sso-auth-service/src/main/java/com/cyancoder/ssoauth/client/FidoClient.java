package com.cyancoder.ssoauth.client;

import com.cyancoder.sso.common.dto.FidoChallengeRequest;
import com.cyancoder.sso.common.dto.FidoChallengeResponse;
import com.cyancoder.sso.common.dto.FidoVerifyRequest;
import com.cyancoder.sso.common.dto.FidoVerifyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "sso-fido-service")
public interface FidoClient {

    @PostMapping("/api/sso/fido/challenge")
    FidoChallengeResponse challenge(@RequestBody FidoChallengeRequest request);

    @PostMapping("/api/sso/fido/verify")
    FidoVerifyResponse verify(@RequestBody FidoVerifyRequest request);
}
