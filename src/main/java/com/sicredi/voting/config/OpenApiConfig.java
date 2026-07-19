package com.sicredi.voting.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI votingOpenApi() {
        return new OpenAPI().info(new Info()
            .title("Cooperative Voting API")
            .description("REST API to manage agendas and cooperative voting sessions")
            .version("v1"));
    }
}
