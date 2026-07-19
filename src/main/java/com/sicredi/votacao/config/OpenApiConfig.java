package com.sicredi.votacao.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class OpenApiConfig {

    @Bean
    public OpenAPI votacaoOpenApi() {
        return new OpenAPI().info(new Info()
            .title("Cooperative Voting API")
            .description("API REST para gerenciar pautas e sessoes de votacao cooperativa")
            .version("v1"));
    }
}
