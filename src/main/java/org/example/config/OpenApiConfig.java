package org.example.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI forkTheBillOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Fork the Bill API")
                        .description("API documentation for Fork the Bill application")
                        .version("v1.0")
                        .contact(new Contact().name("Thameena").email("thameena8@gmail.com"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Fork the Bill Wiki Documentation")
                        .url("https://github.com/your-repo/fork-the-bill"));
    }
}
