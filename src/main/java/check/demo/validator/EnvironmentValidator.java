package check.demo.validator;

import check.demo.model.cnfproperties.AppProperties;
import check.demo.model.cnfproperties.DataSourceProperties;
import check.demo.model.cnfproperties.RtspProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class EnvironmentValidator implements ApplicationListener<ApplicationReadyEvent> {
    private final AppProperties appProperties;
    private final RtspProperties rtspProperties;
    private final DataSourceProperties dataSourceProperties;
    private final Environment environment;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        validateEnvironmentVariables();
    }

    private void validateEnvironmentVariables() {
        log.info("=== Environment Variables Validation ===");

        // 환경변수 직접 확인
        String envMode = environment.getProperty("APP_ENVIRONMENT_MODE");
        String activeProfile = environment.getProperty("spring.profiles.active");

        log.info("Environment Variable - APP_ENVIRONMENT_MODE: {}", envMode);
        log.info("Active Profile: {}", activeProfile);
        log.info("Resolved Environment Mode: {}", appProperties.getEnvironmentMode());

        // 환경변수와 Properties 매핑 검증
        if (!Objects.equals(envMode, appProperties.getEnvironmentMode())) {
            log.warn("Environment variable mismatch! ENV: {}, Properties: {}",
                    envMode, appProperties.getEnvironmentMode());
        }

        // RTSP 설정 검증 (패스워드 마스킹)
        log.info("RTSP Config - Username: {}, Port: {}, Path: {}",
                rtspProperties.getUsername(),
                rtspProperties.getPort(),
                rtspProperties.getPath());

        // DB URL 환경변수 검증
        log.info("Database URLs from environment:");
        log.info("  READ_DB_URL: {}", maskSensitiveInfo(environment.getProperty("READ_DB_URL")));
        log.info("  METRICS_DB_URL: {}", maskSensitiveInfo(environment.getProperty("METRICS_DB_URL")));

        log.info("=== Environment Variables Validation Completed ===");
    }

    private String maskSensitiveInfo(String value) {
        if (value == null) return "NOT_SET";
        return value.replaceAll("password=[^&]*", "password=***");
    }
}
