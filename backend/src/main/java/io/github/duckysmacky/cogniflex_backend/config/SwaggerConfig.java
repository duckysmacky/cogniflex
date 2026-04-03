package io.github.duckysmacky.cogniflex_backend.config;// <- Здесь должен быть путь к твоей папке, VS Code подскажет

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cogniflex API")
                        .version("1.0")
                        .description("Документация для проекта Cogniflex")
                );
    }
}