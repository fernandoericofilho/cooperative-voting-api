package com.sicredi.voting.mappers;

import static org.assertj.core.api.Assertions.*;

import com.sicredi.voting.controllers.response.VoteResponse;
import com.sicredi.voting.enums.VoteOption;
import com.sicredi.voting.models.Vote;
import org.junit.jupiter.api.Test;

class VoteMapperTest {

    private final VoteMapper mapper = new VoteMapper();

    @Test
    void mapVoteToVoteResponse() {
        Vote vote = new Vote(1L, "12345678901", VoteOption.YES);

        VoteResponse response = mapper.toVoteDTO(vote);

        assertThat(response.getAgendaId()).isEqualTo(1L);
        assertThat(response.getMemberCpf()).isEqualTo("12345678901");
        assertThat(response.getVote()).isEqualTo("YES");
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    void voteNo() {
        Vote vote = new Vote(1L, "98765432109", VoteOption.NO);

        VoteResponse response = mapper.toVoteDTO(vote);

        assertThat(response.getVote()).isEqualTo("NO");
    }

}
