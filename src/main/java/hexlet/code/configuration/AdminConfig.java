package hexlet.code.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.admin")
@Getter
@Setter
public class AdminConfig {
    private String email;
    private String password;
}
