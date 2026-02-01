package az.kredo.loan.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI kredoLoanOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Kredo Loan Application API")
                                                .description("API for online loan application platform. Includes OTP verification, "
                                                                +
                                                                "loan application submission, and decision retrieval.")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("Credoline Tech Team")
                                                                .email("tech@kredo.az"))
                                                .license(new License()
                                                                .name("Proprietary")
                                                                .url("https://kredo.az")))
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:8080")
                                                                .description("Development Server")))
                                .components(new Components()
                                                .addSecuritySchemes("bearerAuth",
                                                                new SecurityScheme()
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")
                                                                                .description("JWT token obtained from OTP verification")))
                                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
        }
}
