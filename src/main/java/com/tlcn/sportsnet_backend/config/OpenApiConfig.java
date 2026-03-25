package com.tlcn.sportsnet_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SportsNet Backend API")
                        .version("1.0")
                        .description("API documentation for SportsNet Backend")
                        .contact(new Contact()
                                .name("SportsNet Team")
                                .email("support@sportsnet.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development server")
                ));
    }
}