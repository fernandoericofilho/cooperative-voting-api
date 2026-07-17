package com.sicredi.votacao.services.external;

import com.sicredi.votacao.dtos.StatusVotacao;

public interface UserInfoClient {

    StatusVotacao consultar(String cpf);
}
