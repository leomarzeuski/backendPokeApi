package com.example.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pokemonOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pokemon API")
                        .description("API para acessar dados de Pokémon via PokeAPI")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Sua Nome")
                                .email("seu.email@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}