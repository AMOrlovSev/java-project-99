package hexlet.code.configuration;

import io.sentry.Sentry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SentryConfig {

    @Value("${sentry.dsn:}")
    private String sentryDsn;

    @Value("${sentry.environment:development}")
    private String environment;

    @PostConstruct
    public void init() {
        if (sentryDsn != null && !sentryDsn.isEmpty() && !sentryDsn.isBlank()) {
            Sentry.init(options -> {
                options.setDsn(sentryDsn);
                options.setEnvironment(environment);
                options.setRelease("task-manager@1.0.0");
                options.setTracesSampleRate(1.0);
                options.setEnableExternalConfiguration(true);
                options.setSendDefaultPii(true);
            });

            System.out.println("Sentry initialized for environment: " + environment);
        } else {
            System.out.println("Sentry DSN not configured or empty");
        }
    }
}
