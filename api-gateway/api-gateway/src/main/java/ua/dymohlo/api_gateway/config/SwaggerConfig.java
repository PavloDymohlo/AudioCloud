package ua.dymohlo.api_gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${swagger.info.title}")
    private String title;

    @Value("${swagger.info.description}")
    private String description;

    @Value("${swagger.info.version}")
    private String version;

    @Value("${swagger.info.contact.name}")
    private String contactName;

    @Value("${swagger.info.contact.email}")
    private String contactEmail;

    @Value("${swagger.info.contact.url}")
    private String contactUrl;

    @Value("${swagger.info.license.name}")
    private String licenseName;

    @Value("${swagger.info.license.url}")
    private String licenseUrl;

    @Value("${swagger.server.url}")
    private String serverUrl;

    @Value("${swagger.server.description}")
    private String serverDescription;

    @Value("${swagger.security.scheme-name}")
    private String securitySchemeName;

    @Value("${swagger.security.description}")
    private String securityDescription;

    @Bean
    public OpenAPI apiGatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .description(description)
                        .version(version)
                        .contact(new Contact()
                                .name(contactName)
                                .email(contactEmail)
                                .url(contactUrl))
                        .license(new License()
                                .name(licenseName)
                                .url(licenseUrl)))
                .servers(List.of(
                        new Server()
                                .url(serverUrl)
                                .description(serverDescription)))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description(securityDescription)))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName));
    }
}