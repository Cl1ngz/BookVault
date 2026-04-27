package com.bookvault.library.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // Definiujemy kontakt
        Contact contact = new Contact();
        contact.setEmail("twoj-email@example.com");
        contact.setName("Twój Nick / Imię");
        contact.setUrl("https://github.com/twoj-github");

        // Definiujemy licencję (opcjonalnie)
        License mitLicense = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        // Serwer lokalny (można dodać więcej, np. produkcyjny)
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Serwer deweloperski (lokalny)");

        return new OpenAPI()
                .info(new Info()
                        .title("BookVault API - System Zarządzania Biblioteką")
                        .version("1.0")
                        .contact(contact)
                        .description("To API umożliwia zarządzanie zasobami biblioteki. " +
                                "System wykorzystuje zaawansowaną logikę po stronie bazy danych (PL/pgSQL) " +
                                "oraz Hibernate do mapowania obiektowo-relacyjnego.")
                        .license(mitLicense))
                .servers(List.of(devServer));
    }
}