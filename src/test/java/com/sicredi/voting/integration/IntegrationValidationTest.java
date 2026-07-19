package com.sicredi.voting.integration;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sicredi.voting.controllers.request.OpenSessionRequest;
import com.sicredi.voting.controllers.request.CreateAgendaRequest;
import com.sicredi.voting.controllers.request.RegisterVoteRequest;
import com.sicredi.voting.enums.VotingStatus;
import com.sicredi.voting.services.external.UserInfoClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Complete integration test of Cooperative Voting API.
 * Validates ALL endpoints and business flows with real data.
 *
 * Flow tested:
 * 1. Create agenda (POST /api/v1/agendas) - Status 201
 * 2. List agendas (GET /api/v1/agendas) - Status 200
 * 3. Open session (POST /api/v1/agendas/{id}/sessions) - Status 200
 * 4. Register multiple votes (POST /api/v1/agendas/{id}/votes) - Status 201
 * 5. Tally result (GET /api/v1/agendas/{id}/result) - Status 200
 * 6. Test all error scenarios (400, 404, 409, 422)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IntegrationValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserInfoClient userInfoClient;

    private static final String AGENDA_BASE_URL = "/api/v1/agendas";
    private static final String CPF_ELIGIBLE_1 = "12345678901";
    private static final String CPF_ELIGIBLE_2 = "12345678902";
    private static final String CPF_ELIGIBLE_3 = "12345678903";
    private static final String CPF_ELIGIBLE_4 = "12345678904";
    private static final String CPF_ELIGIBLE_5 = "12345678905";
    private static final String CPF_NOT_ELIGIBLE = "99999999999";
    private static final String CPF_INVALID = "11111111111";

    @BeforeEach
    void setup() {
        // Mock UserInfoClient returning ELIGIBLE by default
        when(userInfoClient.check(anyString()))
            .thenAnswer(invocation -> {
                String cpf = invocation.getArgument(0);
                if (cpf.equals(CPF_NOT_ELIGIBLE)) {
                    return VotingStatus.NOT_ELIGIBLE;
                }
                return VotingStatus.ELIGIBLE;
            });
    }

    /**
     * SCENARIO 1: Complete voting flow
     * Tests: POST create, GET list, POST open session, POST vote, GET result
     */
    @Test
    void testCompleteVotingFlow() throws Exception {
        // Step 1: Create agenda - Status 201 CREATED
        String agendaTitle = "Statutory Reform 2026";
        String agendaDescription = "Discussion about changes in the cooperative bylaws";

        MvcResult createAgendaResult = mockMvc.perform(
            post(AGENDA_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CreateAgendaRequest(agendaTitle, agendaDescription)
                ))
        )
            .andDo(print())
            .andExpect(status().isCreated())  // ✓ Status 201
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.title", is(agendaTitle)))
            .andExpect(jsonPath("$.description", is(agendaDescription)))
            .andExpect(jsonPath("$.createdAt", notNullValue()))
            .andExpect(jsonPath("$.status", is("NOT_STARTED")))
            .andReturn();

        Long agendaId = extractAgendaIdFromResponse(createAgendaResult);

        // Step 2: List agendas - Status 200 OK
        mockMvc.perform(get(AGENDA_BASE_URL)
                .queryParam("page", "0")
                .queryParam("size", "10")
                .queryParam("sortBy", "id")
                .queryParam("sortDirection", "DESC")
        )
            .andDo(print())
            .andExpect(status().isOk())  // ✓ Status 200
            .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.totalPages", greaterThanOrEqualTo(1)))
            .andExpect(jsonPath("$.currentPage", is(0)))
            .andExpect(jsonPath("$.pageSize", is(10)))
            .andExpect(jsonPath("$.hasNext", notNullValue()))
            .andExpect(jsonPath("$.hasPrevious", is(false)));

        // Step 3: Open session with 60 seconds - Status 200 OK
        mockMvc.perform(
            post(String.format("%s/%d/sessions", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new OpenSessionRequest(60L)
                ))
        )
            .andDo(print())
            .andExpect(status().isOk())  // ✓ Status 200
            .andExpect(jsonPath("$.id", is(agendaId.intValue())))
            .andExpect(jsonPath("$.sessionOpenedAt", notNullValue()))
            .andExpect(jsonPath("$.sessionClosesAt", notNullValue()))
            .andExpect(jsonPath("$.status", is("OPEN")));

        // Step 4: Register YES votes - Status 201 CREATED
        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest(CPF_ELIGIBLE_1, "YES")
                ))
        )
            .andDo(print())
            .andExpect(status().isCreated())  // ✓ Status 201
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.agendaId", is(agendaId.intValue())))
            .andExpect(jsonPath("$.memberCpf", is(CPF_ELIGIBLE_1)))
            .andExpect(jsonPath("$.vote", is("YES")))
            .andExpect(jsonPath("$.createdAt", notNullValue()));

        // Second YES vote
        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest(CPF_ELIGIBLE_2, "YES")
                ))
        )
            .andExpect(status().isCreated())  // ✓ Status 201
            .andExpect(jsonPath("$.vote", is("YES")));

        // Third YES vote
        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest(CPF_ELIGIBLE_3, "YES")
                ))
        )
            .andExpect(status().isCreated())  // ✓ Status 201
            .andExpect(jsonPath("$.vote", is("YES")));

        // Step 5: Register NO votes - Status 201 CREATED
        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest(CPF_ELIGIBLE_4, "NO")
                ))
        )
            .andDo(print())
            .andExpect(status().isCreated())  // ✓ Status 201
            .andExpect(jsonPath("$.vote", is("NO")));

        // Second NO vote
        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest(CPF_ELIGIBLE_5, "NO")
                ))
        )
            .andExpect(status().isCreated())  // ✓ Status 201
            .andExpect(jsonPath("$.vote", is("NO")));

        // Step 6: Tally result - Status 200 OK
        mockMvc.perform(get(String.format("%s/%d/result", AGENDA_BASE_URL, agendaId)))
            .andDo(print())
            .andExpect(status().isOk())  // ✓ Status 200
            .andExpect(jsonPath("$.agendaId", is(agendaId.intValue())))
            .andExpect(jsonPath("$.yesCount", is(3)))
            .andExpect(jsonPath("$.noCount", is(2)))
            .andExpect(jsonPath("$.result", is("APPROVED")))  // 3 > 2 = APPROVED
            .andExpect(jsonPath("$.status", is("OPEN")));  // Session still open (60 seconds)

        // Validate agenda was retrieved successfully
        mockMvc.perform(get(String.format("%s/%d", AGENDA_BASE_URL, agendaId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(agendaId.intValue())))
            .andExpect(jsonPath("$.title", is(agendaTitle)));
    }

    /**
     * SCENARIO 2: Error 404 - Agenda not found
     * Tests: GET, POST session, POST vote, GET result with nonexistent ID
     */
    @Test
    void testError404AgendaNotFound() throws Exception {
        Long nonexistentAgendaId = 99999L;

        // Get nonexistent agenda
        mockMvc.perform(get(String.format("%s/%d", AGENDA_BASE_URL, nonexistentAgendaId)))
            .andDo(print())
            .andExpect(status().isNotFound())  // ✓ Status 404
            .andExpect(jsonPath("$.status", is(404)))
            .andExpect(jsonPath("$.message", containsString("not found")));

        // Open session on nonexistent agenda
        mockMvc.perform(
            post(String.format("%s/%d/sessions", AGENDA_BASE_URL, nonexistentAgendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new OpenSessionRequest(60L)
                ))
        )
            .andExpect(status().isNotFound())  // ✓ Status 404
            .andExpect(jsonPath("$.status", is(404)));

        // Register vote on nonexistent agenda
        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, nonexistentAgendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest(CPF_ELIGIBLE_1, "YES")
                ))
        )
            .andExpect(status().isNotFound())  // ✓ Status 404
            .andExpect(jsonPath("$.status", is(404)));

        // Tally result of nonexistent agenda
        mockMvc.perform(get(String.format("%s/%d/result", AGENDA_BASE_URL, nonexistentAgendaId)))
            .andExpect(status().isNotFound())  // ✓ Status 404
            .andExpect(jsonPath("$.status", is(404)));
    }

    /**
     * SCENARIO 3: Error 422 - Session not open
     * Tests: Try to vote without opening session
     */
    @Test
    void testError422SessionNotOpen() throws Exception {
        // Create agenda
        MvcResult createResult = mockMvc.perform(
            post(AGENDA_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CreateAgendaRequest("Test Session Agenda", "Description")
                ))
        )
            .andExpect(status().isCreated())
            .andReturn();

        Long agendaId = extractAgendaIdFromResponse(createResult);

        // Try to vote without opening session
        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest(CPF_ELIGIBLE_1, "YES")
                ))
        )
            .andDo(print())
            .andExpect(status().isUnprocessableEntity())  // ✓ Status 422
            .andExpect(jsonPath("$.status", is(422)))
            .andExpect(jsonPath("$.message", containsString("not open")));
    }

    /**
     * SCENARIO 4: Error 422 - Session closed
     * Tests: Try to vote after session ends
     */
    @Test
    void testError422SessionClosed() throws Exception {
        // Create agenda
        MvcResult createResult = mockMvc.perform(
            post(AGENDA_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CreateAgendaRequest("Short Session Agenda", "Description")
                ))
        )
            .andExpect(status().isCreated())
            .andReturn();

        Long agendaId = extractAgendaIdFromResponse(createResult);

        // Open session with 1 second
        mockMvc.perform(
            post(String.format("%s/%d/sessions", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new OpenSessionRequest(1L)
                ))
        )
            .andExpect(status().isOk());

        // Wait for session to end
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Try to vote in closed session
        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest(CPF_ELIGIBLE_1, "YES")
                ))
        )
            .andDo(print())
            .andExpect(status().isUnprocessableEntity())  // ✓ Status 422
            .andExpect(jsonPath("$.status", is(422)))
            .andExpect(jsonPath("$.message", containsString("closed")));
    }

    /**
     * SCENARIO 5: Error 409 - Session already open
     * Tests: Try to open session already open
     */
    @Test
    void testError409SessionAlreadyOpen() throws Exception {
        // Create agenda
        MvcResult createResult = mockMvc.perform(
            post(AGENDA_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CreateAgendaRequest("Double Session Agenda", "Description")
                ))
        )
            .andExpect(status().isCreated())
            .andReturn();

        Long agendaId = extractAgendaIdFromResponse(createResult);

        // Open session
        mockMvc.perform(
            post(String.format("%s/%d/sessions", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new OpenSessionRequest(60L)
                ))
        )
            .andExpect(status().isOk());

        // Try to open session again
        mockMvc.perform(
            post(String.format("%s/%d/sessions", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new OpenSessionRequest(60L)
                ))
        )
            .andDo(print())
            .andExpect(status().isConflict())  // ✓ Status 409
            .andExpect(jsonPath("$.status", is(409)))
            .andExpect(jsonPath("$.message", containsString("already open")));
    }

    /**
     * SCENARIO 6: Error 409 - Duplicate vote
     * Tests: Try to vote twice on same agenda with same CPF
     */
    @Test
    void testError409DuplicateVote() throws Exception {
        // Create agenda and open session
        MvcResult createResult = mockMvc.perform(
            post(AGENDA_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CreateAgendaRequest("Double Vote Agenda", "Description")
                ))
        )
            .andExpect(status().isCreated())
            .andReturn();

        Long agendaId = extractAgendaIdFromResponse(createResult);

        mockMvc.perform(
            post(String.format("%s/%d/sessions", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new OpenSessionRequest(60L)
                ))
        )
            .andExpect(status().isOk());

        // First vote
        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest(CPF_ELIGIBLE_1, "YES")
                ))
        )
            .andExpect(status().isCreated());

        // Try to vote again with same CPF
        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest(CPF_ELIGIBLE_1, "NO")
                ))
        )
            .andDo(print())
            .andExpect(status().isConflict())  // ✓ Status 409
            .andExpect(jsonPath("$.status", is(409)))
            .andExpect(jsonPath("$.message", containsString("already voted")));
    }

    /**
     * SCENARIO 7: Error 422 - Member not eligible
     * Tests: Try to vote with ineligible CPF
     */
    @Test
    void testError422MemberNotEligible() throws Exception {
        // Create agenda and open session
        MvcResult createResult = mockMvc.perform(
            post(AGENDA_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CreateAgendaRequest("Not Eligible Agenda", "Description")
                ))
        )
            .andExpect(status().isCreated())
            .andReturn();

        Long agendaId = extractAgendaIdFromResponse(createResult);

        mockMvc.perform(
            post(String.format("%s/%d/sessions", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new OpenSessionRequest(60L)
                ))
        )
            .andExpect(status().isOk());

        // Try to vote with ineligible CPF
        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest(CPF_NOT_ELIGIBLE, "YES")
                ))
        )
            .andDo(print())
            .andExpect(status().isUnprocessableEntity())  // ✓ Status 422
            .andExpect(jsonPath("$.status", is(422)))
            .andExpect(jsonPath("$.message", containsString("not eligible")));
    }

    /**
     * SCENARIO 8: Error 400 - Request validation
     * Tests: Send invalid data in requests
     */
    @Test
    void testError400RequestValidation() throws Exception {
        // Blank title
        mockMvc.perform(
            post(AGENDA_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CreateAgendaRequest("", "Description")
                ))
        )
            .andDo(print())
            .andExpect(status().isBadRequest())  // ✓ Status 400
            .andExpect(jsonPath("$.status", is(400)))
            .andExpect(jsonPath("$.message", containsString("Validation")));

        // CPF with invalid format
        MvcResult createResult = mockMvc.perform(
            post(AGENDA_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CreateAgendaRequest("Validation Agenda", "Description")
                ))
        )
            .andExpect(status().isCreated())
            .andReturn();

        Long agendaId = extractAgendaIdFromResponse(createResult);

        mockMvc.perform(
            post(String.format("%s/%d/sessions", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new OpenSessionRequest(60L)
                ))
        )
            .andExpect(status().isOk());

        // CPF with less than 11 digits
        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest("123", "YES")
                ))
        )
            .andDo(print())
            .andExpect(status().isBadRequest())  // ✓ Status 400
            .andExpect(jsonPath("$.status", is(400)));

        // Invalid vote option
        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest(CPF_ELIGIBLE_1, "MAYBE")
                ))
        )
            .andExpect(status().isBadRequest())  // ✓ Status 400
            .andExpect(jsonPath("$.status", is(400)));

        // Blank vote
        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest(CPF_ELIGIBLE_1, "")
                ))
        )
            .andExpect(status().isBadRequest())  // ✓ Status 400
            .andExpect(jsonPath("$.status", is(400)));
    }

    /**
     * SCENARIO 9: Result with tie
     * Tests: Tally when total YES == total NO
     */
    @Test
    void testResultTie() throws Exception {
        // Create agenda and open session
        MvcResult createResult = mockMvc.perform(
            post(AGENDA_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CreateAgendaRequest("Tie Agenda", "Test tie scenario")
                ))
        )
            .andExpect(status().isCreated())
            .andReturn();

        Long agendaId = extractAgendaIdFromResponse(createResult);

        mockMvc.perform(
            post(String.format("%s/%d/sessions", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new OpenSessionRequest(60L)
                ))
        )
            .andExpect(status().isOk());

        // 2 YES votes
        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest(CPF_ELIGIBLE_1, "YES")
                ))
        )
            .andExpect(status().isCreated());

        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest(CPF_ELIGIBLE_2, "YES")
                ))
        )
            .andExpect(status().isCreated());

        // 2 NO votes
        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest(CPF_ELIGIBLE_3, "NO")
                ))
        )
            .andExpect(status().isCreated());

        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest(CPF_ELIGIBLE_4, "NO")
                ))
        )
            .andExpect(status().isCreated());

        // Tally result - should be TIED
        mockMvc.perform(get(String.format("%s/%d/result", AGENDA_BASE_URL, agendaId)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.yesCount", is(2)))
            .andExpect(jsonPath("$.noCount", is(2)))
            .andExpect(jsonPath("$.result", is("TIED")));
    }

    /**
     * SCENARIO 10: Result rejected
     * Tests: Tally when total NO > total YES
     */
    @Test
    void testResultRejected() throws Exception {
        // Create agenda and open session
        MvcResult createResult = mockMvc.perform(
            post(AGENDA_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CreateAgendaRequest("Rejected Agenda", "Test rejection")
                ))
        )
            .andExpect(status().isCreated())
            .andReturn();

        Long agendaId = extractAgendaIdFromResponse(createResult);

        mockMvc.perform(
            post(String.format("%s/%d/sessions", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new OpenSessionRequest(60L)
                ))
        )
            .andExpect(status().isOk());

        // 1 YES vote
        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest(CPF_ELIGIBLE_1, "YES")
                ))
        )
            .andExpect(status().isCreated());

        // 3 NO votes
        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest(CPF_ELIGIBLE_2, "NO")
                ))
        )
            .andExpect(status().isCreated());

        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest(CPF_ELIGIBLE_3, "NO")
                ))
        )
            .andExpect(status().isCreated());

        mockMvc.perform(
            post(String.format("%s/%d/votes", AGENDA_BASE_URL, agendaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterVoteRequest(CPF_ELIGIBLE_4, "NO")
                ))
        )
            .andExpect(status().isCreated());

        // Tally result - should be REJECTED
        mockMvc.perform(get(String.format("%s/%d/result", AGENDA_BASE_URL, agendaId)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.yesCount", is(1)))
            .andExpect(jsonPath("$.noCount", is(3)))
            .andExpect(jsonPath("$.result", is("REJECTED")));
    }

    /**
     * SCENARIO 11: Pagination
     * Tests: Listing with different pagination parameters
     */
    @Test
    void testPagination() throws Exception {
        // Create multiple agendas
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(
                post(AGENDA_BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        new CreateAgendaRequest("Agenda " + i, "Description " + i)
                    ))
            )
                .andExpect(status().isCreated());
        }

        // Test first page with size 3
        mockMvc.perform(
            get(AGENDA_BASE_URL)
                .queryParam("page", "0")
                .queryParam("size", "3")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(3)))
            .andExpect(jsonPath("$.currentPage", is(0)))
            .andExpect(jsonPath("$.pageSize", is(3)))
            .andExpect(jsonPath("$.totalPages", greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.hasNext", is(true)))
            .andExpect(jsonPath("$.hasPrevious", is(false)));

        // Test second page
        mockMvc.perform(
            get(AGENDA_BASE_URL)
                .queryParam("page", "1")
                .queryParam("size", "3")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.currentPage", is(1)))
            .andExpect(jsonPath("$.hasPrevious", is(true)));
    }

    /**
     * Utility: Extracts agenda ID from JSON response
     */
    private Long extractAgendaIdFromResponse(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString();
        return objectMapper.readTree(content).get("id").asLong();
    }
}
