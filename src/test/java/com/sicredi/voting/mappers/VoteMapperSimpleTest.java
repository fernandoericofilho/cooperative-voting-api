package com.sicredi.voting.mappers;

import static org.assertj.core.api.Assertions.*;

import com.sicredi.voting.controllers.response.VoteResponse;
import com.sicredi.voting.enums.VoteOption;
import com.sicredi.voting.models.Vote;
import org.junit.jupiter.api.Test;

class VoteMapperSimpleTest {

    private final VoteMapper mapper = new VoteMapper();

    @Test
    void mapVoteYes() {
        Vote vote = new Vote(1L, "12345678901", VoteOption.YES);

        VoteResponse response = mapper.toVoteDTO(vote);

        assertThat(response.getAgendaId()).isEqualTo(1L);
        assertThat(response.getVote()).isEqualTo("YES");
        // CPF can be masked or not, depending on implementation
        assertThat(response.getMemberCpf()).isNotNull();
    }

    @Test
    void mapVoteNo() {
        Vote vote = new Vote(2L, "98765432109", VoteOption.NO);

        VoteResponse response = mapper.toVoteDTO(vote);

        assertThat(response.getAgendaId()).isEqualTo(2L);
        assertThat(response.getVote()).isEqualTo("NO");
    }

    @Test
    void mapVoteCreatedAtNotNull() {
        Vote vote = new Vote(1L, "12345678901", VoteOption.YES);

        VoteResponse response = mapper.toVoteDTO(vote);

        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    void mapVoteDifferentCpfs() {
        Vote v1 = new Vote(1L, "11111111111", VoteOption.YES);
        Vote v2 = new Vote(1L, "22222222222", VoteOption.YES);

        VoteResponse r1 = mapper.toVoteDTO(v1);
        VoteResponse r2 = mapper.toVoteDTO(v2);

        assertThat(r1.getMemberCpf()).isNotEqualTo(r2.getMemberCpf());
    }

    @Test
    void mapVoteIdNull() {
        Vote vote = new Vote(1L, "12345678901", VoteOption.YES);

        VoteResponse response = mapper.toVoteDTO(vote);

        assertThat(response.getId()).isNull();
    }
}
