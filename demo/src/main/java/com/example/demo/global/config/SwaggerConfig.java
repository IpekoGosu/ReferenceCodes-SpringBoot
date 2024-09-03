package com.example.demo.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "ReferenceCodes-Spring Boot",
                description = "Spring Boot 프로젝트를 만들 때 자주 사용하는 코드들을 참고용으로 정리",
                version = "v1"
        )
)
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("Authorization");
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("local server");

        Server deployServer = new Server();
        deployServer.setUrl("https://qwerqwerqwer.qwerqwer");
        deployServer.setDescription("not a real server yet");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .security(Arrays.asList(securityRequirement))
                .servers(Arrays.asList(localServer));

    }
}
