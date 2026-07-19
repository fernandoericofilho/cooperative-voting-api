package com.sicredi.voting.controllers;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sicredi.voting.controllers.request.RegisterVoteRequest;
import com.sicredi.voting.enums.VoteOption;
import com.sicredi.voting.models.Vote;
import com.sicredi.voting.services.VoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VoteController.class)
class VoteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VoteService voteService;

    @MockBean
    private com.sicredi.voting.mappers.VoteMapper voteMapper;

    @Test
    void registerVoteReturns201() throws Exception {
        Vote vote = new Vote(1L, "12345678901", VoteOption.YES);
        vote.setId(1L);

        when(voteService.registerVote(1L, "12345678901", VoteOption.YES)).thenReturn(vote);
        when(voteMapper.toVoteDTO(any())).thenReturn(
            new com.sicredi.voting.controllers.response.VoteResponse(
                1L, 1L, "123.***.**-01", "YES", "2026-01-01"
            )
        );

        RegisterVoteRequest request = new RegisterVoteRequest("12345678901", "YES");

        mockMvc.perform(post("/api/v1/agendas/1/votes")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vote", is("YES")))
                .andExpect(jsonPath("$.agendaId", is(1)));
    }

    @Test
    void registerVoteWithInvalidCpfReturns400() throws Exception {
        RegisterVoteRequest request = new RegisterVoteRequest("123", "YES");

        mockMvc.perform(post("/api/v1/agendas/1/votes")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerVoteNoReturns201() throws Exception {
        Vote vote = new Vote(1L, "98765432109", VoteOption.NO);
        vote.setId(2L);

        when(voteService.registerVote(1L, "98765432109", VoteOption.NO)).thenReturn(vote);
        when(voteMapper.toVoteDTO(any())).thenReturn(
            new com.sicredi.voting.controllers.response.VoteResponse(
                2L, 1L, "987.***.**-09", "NO", "2026-01-01"
            )
        );

        RegisterVoteRequest request = new RegisterVoteRequest("98765432109", "NO");

        mockMvc.perform(post("/api/v1/agendas/1/votes")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vote", is("NO")));
    }

    @Test
    void registerVoteWithEmptyCpf() throws Exception {
        RegisterVoteRequest request = new RegisterVoteRequest("", "YES");

        mockMvc.perform(post("/api/v1/agendas/1/votes")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerVoteWithInvalidOption() throws Exception {
        RegisterVoteRequest request = new RegisterVoteRequest("12345678901", "INVALID");

        mockMvc.perform(post("/api/v1/agendas/1/votes")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
