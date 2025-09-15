package check.demo.model.cnfproperties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@ConfigurationProperties(prefix = "app")
@Component
@Data
@Validated
public class AppProperties {
    @NotBlank(message = "Environment mode is required")
    @Pattern(regexp = "^(DEV_ENV1|DEV_ENV2|PROD_ENV1|PROD_ENV2)$",
            message = "Environment mode must be one of: DEV-ENV1, DEV_ENV2, PROD-ENV1, PROD-ENV2")
    private String environmentMode; // ${APP_ENVIRONMENT_MODE} 환경변수로 주입

    @Valid
    private Cors cors = new Cors();

    @Data
    public static class Cors {
        @NotEmpty(message = "CORS allowed origins cannot be empty")
        private List<String> allowedOrigins; // ${CORS_ALLOWED_ORIGINS} 환경변수로 주입 (쉼표 구분)
    }
}
