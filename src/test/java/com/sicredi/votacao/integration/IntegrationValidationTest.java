package com.sicredi.votacao.integration;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sicredi.votacao.controllers.request.AbrirSessaoRequest;
import com.sicredi.votacao.controllers.request.CriarPautaRequest;
import com.sicredi.votacao.controllers.request.RegistrarVotoRequest;
import com.sicredi.votacao.enums.StatusVotacao;
import com.sicredi.votacao.services.external.UserInfoClient;
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
 * Teste integrado completo da Cooperative Voting API.
 * Valida TODOS os endpoints e fluxos de negócio com dados reais.
 *
 * Fluxo testado:
 * 1. Criar pauta (POST /api/v1/pautas) - Status 201
 * 2. Listar pautas (GET /api/v1/pautas) - Status 200
 * 3. Abrir sessão (POST /api/v1/pautas/{id}/sessoes) - Status 200
 * 4. Registrar múltiplos votos (POST /api/v1/pautas/{id}/votos) - Status 201
 * 5. Apurar resultado (GET /api/v1/pautas/{id}/resultado) - Status 200
 * 6. Testar todos os cenários de erro (400, 404, 409, 422)
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

    private static final String PAUTA_BASE_URL = "/api/v1/pautas";
    private static final String CPF_HABILITADO_1 = "12345678901";
    private static final String CPF_HABILITADO_2 = "12345678902";
    private static final String CPF_HABILITADO_3 = "12345678903";
    private static final String CPF_HABILITADO_4 = "12345678904";
    private static final String CPF_HABILITADO_5 = "12345678905";
    private static final String CPF_NAO_HABILITADO = "99999999999";
    private static final String CPF_INVALIDO = "11111111111";

    @BeforeEach
    void setup() {
        // Mock do UserInfoClient retornando HABILITADO por padrão
        when(userInfoClient.consultar(anyString()))
            .thenAnswer(invocation -> {
                String cpf = invocation.getArgument(0);
                if (cpf.equals(CPF_NAO_HABILITADO)) {
                    return StatusVotacao.NAO_HABILITADO;
                }
                return StatusVotacao.HABILITADO;
            });
    }

    /**
     * CENÁRIO 1: Fluxo completo de votação
     * Testa: POST create, GET list, POST open session, POST vote, GET resultado
     */
    @Test
    void testFluxoCompletoVotacao() throws Exception {
        // Passo 1: Criar pauta - Status 201 CREATED
        String pautaTitulo = "Reforma Estatutária 2026";
        String pautaDescricao = "Discussão sobre mudanças nos estatutos da cooperativa";

        MvcResult createPautaResult = mockMvc.perform(
            post(PAUTA_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CriarPautaRequest(pautaTitulo, pautaDescricao)
                ))
        )
            .andDo(print())
            .andExpect(status().isCreated())  // ✓ Status 201
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.titulo", is(pautaTitulo)))
            .andExpect(jsonPath("$.descricao", is(pautaDescricao)))
            .andExpect(jsonPath("$.criadoEm", notNullValue()))
            .andExpect(jsonPath("$.status", is("NAO_INICIADA")))
            .andReturn();

        Long pautaId = extractPautaIdFromResponse(createPautaResult);

        // Passo 2: Listar pautas - Status 200 OK
        mockMvc.perform(get(PAUTA_BASE_URL)
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

        // Passo 3: Abrir sessão com 60 segundos - Status 200 OK
        mockMvc.perform(
            post(String.format("%s/%d/sessoes", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new AbrirSessaoRequest(60L)
                ))
        )
            .andDo(print())
            .andExpect(status().isOk())  // ✓ Status 200
            .andExpect(jsonPath("$.id", is(pautaId.intValue())))
            .andExpect(jsonPath("$.sessaoAbertaEm", notNullValue()))
            .andExpect(jsonPath("$.sessaoFechaEm", notNullValue()))
            .andExpect(jsonPath("$.status", is("ABERTA")));

        // Passo 4: Registrar votos SIM - Status 201 CREATED
        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest(CPF_HABILITADO_1, "SIM")
                ))
        )
            .andDo(print())
            .andExpect(status().isCreated())  // ✓ Status 201
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.pautaId", is(pautaId.intValue())))
            .andExpect(jsonPath("$.cpfAssociado", is(CPF_HABILITADO_1)))
            .andExpect(jsonPath("$.voto", is("SIM")))
            .andExpect(jsonPath("$.criadoEm", notNullValue()));

        // Segundo voto SIM
        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest(CPF_HABILITADO_2, "SIM")
                ))
        )
            .andExpect(status().isCreated())  // ✓ Status 201
            .andExpect(jsonPath("$.voto", is("SIM")));

        // Terceiro voto SIM
        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest(CPF_HABILITADO_3, "SIM")
                ))
        )
            .andExpect(status().isCreated())  // ✓ Status 201
            .andExpect(jsonPath("$.voto", is("SIM")));

        // Passo 5: Registrar votos NAO - Status 201 CREATED
        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest(CPF_HABILITADO_4, "NAO")
                ))
        )
            .andDo(print())
            .andExpect(status().isCreated())  // ✓ Status 201
            .andExpect(jsonPath("$.voto", is("NAO")));

        // Segundo voto NAO
        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest(CPF_HABILITADO_5, "NAO")
                ))
        )
            .andExpect(status().isCreated())  // ✓ Status 201
            .andExpect(jsonPath("$.voto", is("NAO")));

        // Passo 6: Apurar resultado - Status 200 OK
        mockMvc.perform(get(String.format("%s/%d/resultado", PAUTA_BASE_URL, pautaId)))
            .andDo(print())
            .andExpect(status().isOk())  // ✓ Status 200
            .andExpect(jsonPath("$.pautaId", is(pautaId.intValue())))
            .andExpect(jsonPath("$.totalSim", is(3)))
            .andExpect(jsonPath("$.totalNao", is(2)))
            .andExpect(jsonPath("$.resultado", is("APROVADA")))  // 3 > 2 = APROVADA
            .andExpect(jsonPath("$.status", is("ABERTA")));  // Sessão ainda está aberta (60 segundos)

        // Validar que a pauta foi obtida com sucesso
        mockMvc.perform(get(String.format("%s/%d", PAUTA_BASE_URL, pautaId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(pautaId.intValue())))
            .andExpect(jsonPath("$.titulo", is(pautaTitulo)));
    }

    /**
     * CENÁRIO 2: Erro 404 - Pauta não encontrada
     * Testa: GET, POST sessão, POST voto, GET resultado com ID inexistente
     */
    @Test
    void testErro404PautaNaoEncontrada() throws Exception {
        Long pautaIdInexistente = 99999L;

        // Obter pauta inexistente
        mockMvc.perform(get(String.format("%s/%d", PAUTA_BASE_URL, pautaIdInexistente)))
            .andDo(print())
            .andExpect(status().isNotFound())  // ✓ Status 404
            .andExpect(jsonPath("$.status", is(404)))
            .andExpect(jsonPath("$.message", containsString("não encontrada")));

        // Abrir sessão em pauta inexistente
        mockMvc.perform(
            post(String.format("%s/%d/sessoes", PAUTA_BASE_URL, pautaIdInexistente))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new AbrirSessaoRequest(60L)
                ))
        )
            .andExpect(status().isNotFound())  // ✓ Status 404
            .andExpect(jsonPath("$.status", is(404)));

        // Registrar voto em pauta inexistente
        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaIdInexistente))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest(CPF_HABILITADO_1, "SIM")
                ))
        )
            .andExpect(status().isNotFound())  // ✓ Status 404
            .andExpect(jsonPath("$.status", is(404)));

        // Apurar resultado de pauta inexistente
        mockMvc.perform(get(String.format("%s/%d/resultado", PAUTA_BASE_URL, pautaIdInexistente)))
            .andExpect(status().isNotFound())  // ✓ Status 404
            .andExpect(jsonPath("$.status", is(404)));
    }

    /**
     * CENÁRIO 3: Erro 422 - Sessão não aberta
     * Testa: Tentar votar sem abrir sessão
     */
    @Test
    void testErro422SessaoNaoAberta() throws Exception {
        // Criar pauta
        MvcResult createResult = mockMvc.perform(
            post(PAUTA_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CriarPautaRequest("Pauta Teste Sessão", "Descrição")
                ))
        )
            .andExpect(status().isCreated())
            .andReturn();

        Long pautaId = extractPautaIdFromResponse(createResult);

        // Tentar votar sem abrir sessão
        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest(CPF_HABILITADO_1, "SIM")
                ))
        )
            .andDo(print())
            .andExpect(status().isUnprocessableEntity())  // ✓ Status 422
            .andExpect(jsonPath("$.status", is(422)))
            .andExpect(jsonPath("$.message", containsString("não foi aberta")));
    }

    /**
     * CENÁRIO 4: Erro 422 - Sessão encerrada
     * Testa: Tentar votar após encerramento da sessão
     */
    @Test
    void testErro422SessaoEncerrada() throws Exception {
        // Criar pauta
        MvcResult createResult = mockMvc.perform(
            post(PAUTA_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CriarPautaRequest("Pauta Sessão Curta", "Descrição")
                ))
        )
            .andExpect(status().isCreated())
            .andReturn();

        Long pautaId = extractPautaIdFromResponse(createResult);

        // Abrir sessão com 1 segundo
        mockMvc.perform(
            post(String.format("%s/%d/sessoes", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new AbrirSessaoRequest(1L)
                ))
        )
            .andExpect(status().isOk());

        // Aguardar encerramento da sessão
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Tentar votar em sessão encerrada
        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest(CPF_HABILITADO_1, "SIM")
                ))
        )
            .andDo(print())
            .andExpect(status().isUnprocessableEntity())  // ✓ Status 422
            .andExpect(jsonPath("$.status", is(422)))
            .andExpect(jsonPath("$.message", containsString("encerrada")));
    }

    /**
     * CENÁRIO 5: Erro 409 - Sessão já aberta
     * Testa: Tentar abrir sessão já aberta
     */
    @Test
    void testErro409SessaoJaAberta() throws Exception {
        // Criar pauta
        MvcResult createResult = mockMvc.perform(
            post(PAUTA_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CriarPautaRequest("Pauta Dupla Sessão", "Descrição")
                ))
        )
            .andExpect(status().isCreated())
            .andReturn();

        Long pautaId = extractPautaIdFromResponse(createResult);

        // Abrir sessão
        mockMvc.perform(
            post(String.format("%s/%d/sessoes", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new AbrirSessaoRequest(60L)
                ))
        )
            .andExpect(status().isOk());

        // Tentar abrir sessão novamente
        mockMvc.perform(
            post(String.format("%s/%d/sessoes", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new AbrirSessaoRequest(60L)
                ))
        )
            .andDo(print())
            .andExpect(status().isConflict())  // ✓ Status 409
            .andExpect(jsonPath("$.status", is(409)))
            .andExpect(jsonPath("$.message", containsString("já aberta")));
    }

    /**
     * CENÁRIO 6: Erro 409 - Voto duplicado
     * Testa: Tentar votar duas vezes na mesma pauta com mesmo CPF
     */
    @Test
    void testErro409VotoDuplicado() throws Exception {
        // Criar pauta e abrir sessão
        MvcResult createResult = mockMvc.perform(
            post(PAUTA_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CriarPautaRequest("Pauta Voto Duplo", "Descrição")
                ))
        )
            .andExpect(status().isCreated())
            .andReturn();

        Long pautaId = extractPautaIdFromResponse(createResult);

        mockMvc.perform(
            post(String.format("%s/%d/sessoes", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new AbrirSessaoRequest(60L)
                ))
        )
            .andExpect(status().isOk());

        // Primeiro voto
        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest(CPF_HABILITADO_1, "SIM")
                ))
        )
            .andExpect(status().isCreated());

        // Tentar votar novamente com o mesmo CPF
        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest(CPF_HABILITADO_1, "NAO")
                ))
        )
            .andDo(print())
            .andExpect(status().isConflict())  // ✓ Status 409
            .andExpect(jsonPath("$.status", is(409)))
            .andExpect(jsonPath("$.message", containsString("já votou")));
    }

    /**
     * CENÁRIO 7: Erro 422 - Associado não habilitado
     * Testa: Tentar votar com CPF não habilitado
     */
    @Test
    void testErro422AssociadoNaoHabilitado() throws Exception {
        // Criar pauta e abrir sessão
        MvcResult createResult = mockMvc.perform(
            post(PAUTA_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CriarPautaRequest("Pauta Não Habilitado", "Descrição")
                ))
        )
            .andExpect(status().isCreated())
            .andReturn();

        Long pautaId = extractPautaIdFromResponse(createResult);

        mockMvc.perform(
            post(String.format("%s/%d/sessoes", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new AbrirSessaoRequest(60L)
                ))
        )
            .andExpect(status().isOk());

        // Tentar votar com CPF não habilitado
        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest(CPF_NAO_HABILITADO, "SIM")
                ))
        )
            .andDo(print())
            .andExpect(status().isUnprocessableEntity())  // ✓ Status 422
            .andExpect(jsonPath("$.status", is(422)))
            .andExpect(jsonPath("$.message", containsString("não habilitado")));
    }

    /**
     * CENÁRIO 8: Erro 400 - Validação de request
     * Testa: Enviar dados inválidos nas requisições
     */
    @Test
    void testErro400ValidacaoRequest() throws Exception {
        // Título em branco
        mockMvc.perform(
            post(PAUTA_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CriarPautaRequest("", "Descrição")
                ))
        )
            .andDo(print())
            .andExpect(status().isBadRequest())  // ✓ Status 400
            .andExpect(jsonPath("$.status", is(400)))
            .andExpect(jsonPath("$.message", containsString("Validação")));

        // CPF com formato inválido
        MvcResult createResult = mockMvc.perform(
            post(PAUTA_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CriarPautaRequest("Pauta Validação", "Descrição")
                ))
        )
            .andExpect(status().isCreated())
            .andReturn();

        Long pautaId = extractPautaIdFromResponse(createResult);

        mockMvc.perform(
            post(String.format("%s/%d/sessoes", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new AbrirSessaoRequest(60L)
                ))
        )
            .andExpect(status().isOk());

        // CPF com menos de 11 dígitos
        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest("123", "SIM")
                ))
        )
            .andDo(print())
            .andExpect(status().isBadRequest())  // ✓ Status 400
            .andExpect(jsonPath("$.status", is(400)));

        // Opção de voto inválida
        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest(CPF_HABILITADO_1, "TALVEZ")
                ))
        )
            .andExpect(status().isBadRequest())  // ✓ Status 400
            .andExpect(jsonPath("$.status", is(400)));

        // Voto em branco
        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest(CPF_HABILITADO_1, "")
                ))
        )
            .andExpect(status().isBadRequest())  // ✓ Status 400
            .andExpect(jsonPath("$.status", is(400)));
    }

    /**
     * CENÁRIO 9: Resultado com empate
     * Testa: Apuração quando total de SIM == total de NAO
     */
    @Test
    void testResultadoEmpate() throws Exception {
        // Criar pauta e abrir sessão
        MvcResult createResult = mockMvc.perform(
            post(PAUTA_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CriarPautaRequest("Pauta Empate", "Teste de empate")
                ))
        )
            .andExpect(status().isCreated())
            .andReturn();

        Long pautaId = extractPautaIdFromResponse(createResult);

        mockMvc.perform(
            post(String.format("%s/%d/sessoes", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new AbrirSessaoRequest(60L)
                ))
        )
            .andExpect(status().isOk());

        // 2 votos SIM
        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest(CPF_HABILITADO_1, "SIM")
                ))
        )
            .andExpect(status().isCreated());

        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest(CPF_HABILITADO_2, "SIM")
                ))
        )
            .andExpect(status().isCreated());

        // 2 votos NAO
        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest(CPF_HABILITADO_3, "NAO")
                ))
        )
            .andExpect(status().isCreated());

        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest(CPF_HABILITADO_4, "NAO")
                ))
        )
            .andExpect(status().isCreated());

        // Apurar resultado - deve ser EMPATE
        mockMvc.perform(get(String.format("%s/%d/resultado", PAUTA_BASE_URL, pautaId)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalSim", is(2)))
            .andExpect(jsonPath("$.totalNao", is(2)))
            .andExpect(jsonPath("$.resultado", is("EMPATE")));
    }

    /**
     * CENÁRIO 10: Resultado reprovado
     * Testa: Apuração quando total de NAO > total de SIM
     */
    @Test
    void testResultadoReprovada() throws Exception {
        // Criar pauta e abrir sessão
        MvcResult createResult = mockMvc.perform(
            post(PAUTA_BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CriarPautaRequest("Pauta Reprovada", "Teste de reprovação")
                ))
        )
            .andExpect(status().isCreated())
            .andReturn();

        Long pautaId = extractPautaIdFromResponse(createResult);

        mockMvc.perform(
            post(String.format("%s/%d/sessoes", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new AbrirSessaoRequest(60L)
                ))
        )
            .andExpect(status().isOk());

        // 1 voto SIM
        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest(CPF_HABILITADO_1, "SIM")
                ))
        )
            .andExpect(status().isCreated());

        // 3 votos NAO
        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest(CPF_HABILITADO_2, "NAO")
                ))
        )
            .andExpect(status().isCreated());

        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest(CPF_HABILITADO_3, "NAO")
                ))
        )
            .andExpect(status().isCreated());

        mockMvc.perform(
            post(String.format("%s/%d/votos", PAUTA_BASE_URL, pautaId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegistrarVotoRequest(CPF_HABILITADO_4, "NAO")
                ))
        )
            .andExpect(status().isCreated());

        // Apurar resultado - deve ser REPROVADA
        mockMvc.perform(get(String.format("%s/%d/resultado", PAUTA_BASE_URL, pautaId)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalSim", is(1)))
            .andExpect(jsonPath("$.totalNao", is(3)))
            .andExpect(jsonPath("$.resultado", is("REPROVADA")));
    }

    /**
     * CENÁRIO 11: Paginação
     * Testa: Listagem com diferentes parâmetros de paginação
     */
    @Test
    void testPaginacao() throws Exception {
        // Criar múltiplas pautas
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(
                post(PAUTA_BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                        new CriarPautaRequest("Pauta " + i, "Descrição " + i)
                    ))
            )
                .andExpect(status().isCreated());
        }

        // Testar primeira página com size 3
        mockMvc.perform(
            get(PAUTA_BASE_URL)
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

        // Testar segunda página
        mockMvc.perform(
            get(PAUTA_BASE_URL)
                .queryParam("page", "1")
                .queryParam("size", "3")
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.currentPage", is(1)))
            .andExpect(jsonPath("$.hasPrevious", is(true)));
    }

    /**
     * Utilitário: Extrai o ID da pauta da resposta JSON
     */
    private Long extractPautaIdFromResponse(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString();
        return objectMapper.readTree(content).get("id").asLong();
    }
}
