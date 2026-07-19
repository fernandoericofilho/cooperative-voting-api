package com.sicredi.voting.dtos;

import com.sicredi.voting.enums.VoteOption;

public interface VoteCount {

    VoteOption getOption();

    Long getTotal();
}
