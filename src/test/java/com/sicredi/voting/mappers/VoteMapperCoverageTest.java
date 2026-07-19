package com.sicredi.voting.mappers;

import static org.assertj.core.api.Assertions.*;

import com.sicredi.voting.controllers.response.VoteResponse;
import com.sicredi.voting.enums.VoteOption;
import com.sicredi.voting.models.Vote;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

class VoteMapperCoverageTest {

    private final VoteMapper mapper = new VoteMapper();

    @Test
    void mapVoteWithFormattedDateTime() {
        Vote vote = new Vote(1L, "12345678901", VoteOption.YES);
        vote.setId(1L);

        VoteResponse response = mapper.toVoteDTO(vote);

        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getCreatedAt()).contains("T");
    }

    @Test
    void mapVoteAllFields() {
        Vote vote = new Vote(2L, "98765432100", VoteOption.NO);
        vote.setId(2L);

        VoteResponse response = mapper.toVoteDTO(vote);

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getAgendaId()).isEqualTo(2L);
        assertThat(response.getMemberCpf()).isEqualTo("98765432100");
        assertThat(response.getVote()).isEqualTo("NO");
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    void mapVoteDateTimeIsoFormat() {
        Vote vote = new Vote(3L, "11122233344", VoteOption.YES);
        vote.setId(3L);

        VoteResponse response = mapper.toVoteDTO(vote);

        assertThat(response.getCreatedAt()).contains("T");
        assertThat(response.getCreatedAt()).doesNotContain("Z");
    }

    @Test
    void mapVoteYesOption() {
        Vote vote = new Vote(4L, "44455566677", VoteOption.YES);
        vote.setId(4L);

        VoteResponse response = mapper.toVoteDTO(vote);

        assertThat(response.getVote()).isEqualTo("YES");
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    void mapVoteNoOption() {
        Vote vote = new Vote(5L, "77788899900", VoteOption.NO);
        vote.setId(5L);

        VoteResponse response = mapper.toVoteDTO(vote);

        assertThat(response.getVote()).isEqualTo("NO");
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    void mapVoteFormatDateTimeConsistency() {
        Vote vote1 = new Vote(1L, "12345678901", VoteOption.YES);
        vote1.setId(1L);

        Vote vote2 = new Vote(2L, "98765432100", VoteOption.NO);
        vote2.setId(2L);

        VoteResponse response1 = mapper.toVoteDTO(vote1);
        VoteResponse response2 = mapper.toVoteDTO(vote2);

        assertThat(response1.getCreatedAt()).isNotNull();
        assertThat(response2.getCreatedAt()).isNotNull();
        assertThat(response1.getCreatedAt()).contains("T");
        assertThat(response2.getCreatedAt()).contains("T");
    }

    @Test
    void mapVoteDateTimeNotEmpty() {
        Vote vote = new Vote(10L, "55566677788", VoteOption.YES);
        vote.setId(10L);

        VoteResponse response = mapper.toVoteDTO(vote);

        assertThat(response.getCreatedAt()).isNotEmpty();
        assertThat(response.getCreatedAt()).isNotBlank();
    }
}
