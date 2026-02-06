package com.bcttg.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {
    @Value("${app.swagger.title:BCTTG API}")
    private String title;

    @Value("${app.swagger.description:API tai lieu va thuc thi So tay dien tu}")
    private String description;

    @Value("${app.swagger.version:v1}")
    private String version;

    @Value("${app.swagger.server-url:/}")
    private String serverUrl;

    @Value("${app.swagger.contact-name:Support}")
    private String contactName;

    @Value("${app.swagger.contact-email:support@bcttg.local}")
    private String contactEmail;

    @Value("${app.swagger.contact-url:https://bcttg.local}")
    private String contactUrl;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title(title)
                .description(description)
                .version(version)
                .contact(new Contact().name(contactName).email(contactEmail).url(contactUrl))
                .license(new License().name("Proprietary")))
            .addServersItem(new Server().url(serverUrl).description("API Server"))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components().addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
