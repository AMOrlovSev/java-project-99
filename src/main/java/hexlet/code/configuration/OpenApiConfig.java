package hexlet.code.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Task Manager API",
                version = "1.0",
                description = "REST API для управления задачами с поддержкой пользователей, статусов и меток",
                contact = @Contact(
                        name = "Support Team",
                        email = "support@taskmanager.com",
                        url = "https://java-project-99-bntq.onrender.com"
                )
        ),
        servers = {
                @Server(
                        description = "Production Server",
                        url = "https://java-project-99-bntq.onrender.com"
                ),
                @Server(
                        description = "Local Server",
                        url = "http://localhost:80800"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer",
        description = "JWT аутентификация. Получите токен через endpoint /api/login"
)
public class OpenApiConfig {
}
