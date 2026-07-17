package com.sicredi.votacao.dtos;

import com.sicredi.votacao.models.OpcaoVoto;

public interface ContagemVoto {

    OpcaoVoto getOpcao();

    Long getTotal();
}
