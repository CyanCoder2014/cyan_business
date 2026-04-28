package com.cyancoder.ssofido.service;

import com.cyancoder.sso.common.dto.FidoChallengeRequest;
import com.cyancoder.sso.common.dto.FidoChallengeResponse;
import com.cyancoder.sso.common.dto.FidoVerifyRequest;
import com.cyancoder.sso.common.dto.FidoVerifyResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FidoService {

    private final Map<String, ChallengeState> challenges = new ConcurrentHashMap<>();

    public FidoChallengeResponse createChallenge(FidoChallengeRequest request) {
        String challengeId = UUID.randomUUID().toString();
        String challenge = UUID.randomUUID().toString().replace("-", "");
        long expiresAt = Instant.now().plusSeconds(300).getEpochSecond();
        challenges.put(challengeId, new ChallengeState(request.username(), request.clientId(), challenge, expiresAt));
        return new FidoChallengeResponse(challengeId, challenge, expiresAt);
    }

    public FidoVerifyResponse verify(FidoVerifyRequest request) {
        ChallengeState state = challenges.remove(request.challengeId());
        if (state == null) {
            return new FidoVerifyResponse(false, "Challenge not found");
        }
        if (state.expiresAtEpochSecond() < Instant.now().getEpochSecond()) {
            return new FidoVerifyResponse(false, "Challenge expired");
        }
        if (!state.username().equals(request.username()) || !state.clientId().equals(request.clientId())) {
            return new FidoVerifyResponse(false, "Challenge identity mismatch");
        }
        if (!state.challenge().equals(request.signedChallenge())) {
            return new FidoVerifyResponse(false, "Challenge verification failed");
        }
        return new FidoVerifyResponse(true, "FIDO challenge verified");
    }

    private record ChallengeState(String username, String clientId, String challenge, long expiresAtEpochSecond) {
    }
}
