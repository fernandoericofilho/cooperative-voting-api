package com.sicredi.votacao.services.external;

import com.sicredi.votacao.enums.StatusVotacao;

public interface UserInfoClient {

    StatusVotacao consultar(String cpf);
}
