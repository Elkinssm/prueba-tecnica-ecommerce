package org.example.pruebatecnicaecommerce.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Ecommerce API",
                description = "API del ecommerce con operaciones de inventario y ordenes",
                version = "v1",
                contact = @Contact(name = "Equipo Ecommerce")
        ),
        security = { @SecurityRequirement(name = "bearerAuth") }
)
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi ecommerceApi() {
        return GroupedOpenApi.builder()
                .group("ecommerce")
                .packagesToScan("org.example.pruebatecnicaecommerce.infrastructure.rest")
                .build();
    }
}
