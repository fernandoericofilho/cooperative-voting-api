package com.sicredi.votacao.dtos;

import com.sicredi.votacao.enums.OpcaoVoto;

public interface ContagemVoto {

    OpcaoVoto getOpcao();

    Long getTotal();
}
