package com.atelier.gestion.config;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.*;
@Configuration public class OpenApiConfig {
  @Bean OpenAPI atelierOpenApi(){return new OpenAPI().info(new Info().title("ATELIER Gestión Digital API").version("v1").description("API para la gestión de proyectos arquitectónicos")).components(new Components().addSecuritySchemes("bearerAuth",new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));}
}

