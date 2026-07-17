package com.sicredi.votacao.dtos;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TelaSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void formularioSerializaSemCamposNulos() throws Exception {
        TelaFormulario tela = new TelaFormulario(
            "TITULO TELA",
            List.of(
                ItemFormulario.texto("Lorem ipsum"),
                ItemFormulario.inputTexto("titulo", "Título", "")
            ),
            new Botao("Ação 1", "http://seudominio.com/ACAO1", Map.of()),
            null
        );

        String json = objectMapper.writeValueAsString(tela);

        assertThat(json).contains("\"tipo\":\"FORMULARIO\"");
        assertThat(json).doesNotContain("botaoCancelar");
        assertThat(json).contains("\"tipo\":\"INPUT_TEXTO\"");
    }

    @Test
    void selecaoSerializaListaDeItens() throws Exception {
        TelaSelecao tela = new TelaSelecao(
            "Lista de seleção",
            List.of(new ItemSelecao("Opção 1", "http://seudominio.com/OPT1", Map.of("dadosOpcao", "campo de teste")))
        );

        String json = objectMapper.writeValueAsString(tela);

        assertThat(json).contains("\"tipo\":\"SELECAO\"");
        assertThat(json).contains("\"texto\":\"Opção 1\"");
    }
}
