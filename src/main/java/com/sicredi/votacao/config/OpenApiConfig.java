package com.sicredi.votacao.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI votacaoOpenApi() {
        return new OpenAPI().info(new Info()
            .title("Cooperative Voting API")
            .description("API para cadastro de pautas, sessões de votação e apuração de resultados, incluindo o protocolo de telas server-driven consumido pelo app mobile.")
            .version("v1"));
    }
}
