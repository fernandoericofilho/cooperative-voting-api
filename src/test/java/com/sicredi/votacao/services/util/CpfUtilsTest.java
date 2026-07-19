package com.sicredi.votacao.services.util;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CpfUtilsTest {

    @Test
    void maskCpfValido() {
        String masked = CpfUtils.mask("12345678901");

        assertThat(masked).isEqualTo("123.***.**-01");
    }

    @Test
    void maskCpfCurto() {
        String masked = CpfUtils.mask("123");

        assertThat(masked).isEqualTo("****");
    }

    @Test
    void maskCpfNull() {
        String masked = CpfUtils.mask(null);

        assertThat(masked).isEqualTo("****");
    }

    @Test
    void maskCpfVazio() {
        String masked = CpfUtils.mask("");

        assertThat(masked).isEqualTo("****");
    }

    @Test
    void maskCpfExatamenteLimite() {
        String masked = CpfUtils.mask("1234");

        assertThat(masked).isEqualTo("123.***.**-34");
    }
}
