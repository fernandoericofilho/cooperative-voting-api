package com.sicredi.votacao.services.util;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CpfUtilsMaskTest {

    @Test
    void maskCpfComprido() {
        String result = CpfUtils.mask("12345678901");
        assertThat(result).isNotNull();
        assertThat(result).contains("***");
        assertThat(result).startsWith("123");
        assertThat(result).endsWith("01");
    }

    @Test
    void maskCpfNull() {
        String result = CpfUtils.mask(null);
        assertThat(result).isEqualTo("****");
    }

    @Test
    void maskCpfTodos9() {
        String result = CpfUtils.mask("99999999999");
        assertThat(result).isNotNull();
        assertThat(result).contains("***");
        assertThat(result).startsWith("999");
        assertThat(result).endsWith("99");
    }

    @Test
    void maskCpfTodos0() {
        String result = CpfUtils.mask("00000000000");
        assertThat(result).isNotNull();
        assertThat(result).contains("***");
        assertThat(result).startsWith("000");
        assertThat(result).endsWith("00");
    }

    @Test
    void maskCpfAlternado() {
        String result = CpfUtils.mask("12312312312");
        assertThat(result).isNotNull();
        assertThat(result).contains("***");
    }
}
