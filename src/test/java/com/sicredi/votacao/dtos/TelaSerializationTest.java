package com.sicredi.votacao.dtos;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
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
        JsonNode node = objectMapper.readTree(json);

        assertThat(node.get("tipo").asText()).isEqualTo("FORMULARIO");
        assertThat(node.has("botaoCancelar")).isFalse();
        assertThat(node.has("botaoOk")).isTrue();
    }

    @Test
    void itemTextoOmiteCamposDeInput() throws Exception {
        ItemFormulario item = ItemFormulario.texto("Lorem ipsum");

        String json = objectMapper.writeValueAsString(item);
        JsonNode node = objectMapper.readTree(json);

        assertThat(node.has("tipo")).isTrue();
        assertThat(node.get("tipo").asText()).isEqualTo("TEXTO");
        assertThat(node.has("texto")).isTrue();
        assertThat(node.get("texto").asText()).isEqualTo("Lorem ipsum");

        assertThat(node.has("id")).isFalse();
        assertThat(node.has("titulo")).isFalse();
        assertThat(node.has("valor")).isFalse();
    }

    @Test
    void itemInputTextoOmiteCampoTexto() throws Exception {
        ItemFormulario item = ItemFormulario.inputTexto("titulo", "Título", "");

        String json = objectMapper.writeValueAsString(item);
        JsonNode node = objectMapper.readTree(json);

        assertThat(node.has("tipo")).isTrue();
        assertThat(node.get("tipo").asText()).isEqualTo("INPUT_TEXTO");
        assertThat(node.has("id")).isTrue();
        assertThat(node.get("id").asText()).isEqualTo("titulo");
        assertThat(node.has("titulo")).isTrue();
        assertThat(node.get("titulo").asText()).isEqualTo("Título");
        assertThat(node.has("valor")).isTrue();

        assertThat(node.has("texto")).isFalse();
    }

    @Test
    void itemInputNumeroOmiteCampoTexto() throws Exception {
        ItemFormulario item = ItemFormulario.inputNumero("idade", "Idade", 42);

        String json = objectMapper.writeValueAsString(item);
        JsonNode node = objectMapper.readTree(json);

        assertThat(node.has("tipo")).isTrue();
        assertThat(node.get("tipo").asText()).isEqualTo("INPUT_NUMERO");
        assertThat(node.has("id")).isTrue();
        assertThat(node.get("id").asText()).isEqualTo("idade");
        assertThat(node.has("titulo")).isTrue();
        assertThat(node.get("titulo").asText()).isEqualTo("Idade");
        assertThat(node.has("valor")).isTrue();
        assertThat(node.get("valor").asInt()).isEqualTo(42);

        assertThat(node.has("texto")).isFalse();
    }

    @Test
    void selecaoSerializaListaDeItens() throws Exception {
        TelaSelecao tela = new TelaSelecao(
            "Lista de seleção",
            List.of(new ItemSelecao("Opção 1", "http://seudominio.com/OPT1", Map.of("dadosOpcao", "campo de teste")))
        );

        String json = objectMapper.writeValueAsString(tela);
        JsonNode node = objectMapper.readTree(json);

        assertThat(node.get("tipo").asText()).isEqualTo("SELECAO");
        assertThat(node.get("itens").get(0).get("texto").asText()).isEqualTo("Opção 1");
    }
}
