package com.sicredi.voting.services.external;

import com.sicredi.voting.enums.VotingStatus;

public interface UserInfoClient {

    VotingStatus check(String cpf);
}
