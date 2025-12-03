package com.hrms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI Configuration for API documentation.
 * Access at: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI hrmsOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("HRMS SaaS API")
                .description("Human Resource Management System - REST API Documentation")
                .version("1.0.0")
                .contact(new Contact()
                    .name("HRMS Support")
                    .email("support@hrms.com")
                    .url("https://hrms.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("Development Server")
            ));
    }
}
