package com.sicredi.voting.services.external;

import com.sicredi.voting.enums.VotingStatus;
import com.sicredi.voting.services.util.CpfUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockUserInfoClient implements UserInfoClient {

    @Override
    public VotingStatus check(String cpf) {
        String maskedCpf = CpfUtils.mask(cpf);
        log.info("Mock: Checking member eligibility: {}", maskedCpf);
        log.info("Mock: Member {} is ELIGIBLE", maskedCpf);
        return VotingStatus.ELIGIBLE;
    }
}
