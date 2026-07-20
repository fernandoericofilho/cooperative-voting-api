package com.sicredi.voting.services.external;

import com.sicredi.voting.enums.VotingStatus;
import com.sicredi.voting.services.util.CpfUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Mock implementation of UserInfoClient for development/testing.
 *
 * IMPORTANT: The external service at https://user-info.herokuapp.com is OFFLINE
 * (Heroku deprecated free dynos in November 2022).
 *
 * This mock implementation always returns ELIGIBLE status for all CPFs
 * to allow testing the voting workflow without external dependencies.
 *
 * To use the real external service when it becomes available:
 * 1. Set app.mock.enabled=false in application.yml
 * 2. The configuration will automatically switch to WebClientUserInfoClient
 * 3. Update app.external.user-info-url to the correct endpoint if needed
 */
@Slf4j
public class MockUserInfoClient implements UserInfoClient {

    @Override
    public VotingStatus check(String cpf) {
        String maskedCpf = CpfUtils.mask(cpf);
        log.info("[MOCK] Checking member eligibility: {} - returning ELIGIBLE", maskedCpf);
        log.warn("[MOCK] External service https://user-info.herokuapp.com is OFFLINE - using mock implementation");
        return VotingStatus.ELIGIBLE;
    }
}
