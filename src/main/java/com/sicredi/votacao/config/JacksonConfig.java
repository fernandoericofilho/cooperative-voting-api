package com.sicredi.votacao.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.data.domain.Page;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Remove pageable and sort fields from Page serialization
        FilterProvider filters = new SimpleFilterProvider()
            .addFilter("pageFilter",
                SimpleBeanPropertyFilter.serializeAllExcept("pageable", "sort"));

        mapper.setFilterProvider(filters);
        return mapper;
    }
}
