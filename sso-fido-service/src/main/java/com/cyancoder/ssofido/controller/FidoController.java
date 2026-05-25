package com.cyancoder.ssofido.controller;

import com.cyancoder.sso.common.dto.FidoChallengeRequest;
import com.cyancoder.sso.common.dto.FidoChallengeResponse;
import com.cyancoder.sso.common.dto.FidoVerifyRequest;
import com.cyancoder.sso.common.dto.FidoVerifyResponse;
import com.cyancoder.ssofido.service.FidoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sso/fido")
public class FidoController {

    private final FidoService fidoService;

    public FidoController(FidoService fidoService) {
        this.fidoService = fidoService;
    }

    @PostMapping("/challenge")
    public FidoChallengeResponse challenge(@RequestBody FidoChallengeRequest request) {
        return fidoService.createChallenge(request);
    }

    @PostMapping("/verify")
    public FidoVerifyResponse verify(@RequestBody FidoVerifyRequest request) {
        return fidoService.verify(request);
    }
}
