package check.demo.model.cnfproperties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@ConfigurationProperties(prefix = "spring.datasource")
@Component
@Data
@Validated
@Profile("!test")
public class DataSourceProperties {
    @Valid
    private DatabaseConfig read = new DatabaseConfig();

    @Valid
    private DatabaseConfig metrics = new DatabaseConfig();

    @Data
    public static class DatabaseConfig {
        @NotBlank(message = "Database driver class name is required")
        private String driverClassName; // ${READ_DB_DRIVER_CLASS_NAME} 등 환경변수로 주입

        @NotBlank(message = "Database JDBC URL is required")
        @Pattern(regexp = "^jdbc:mariadb://.*", message = "JDBC URL must be MariaDB format")
        private String jdbcUrl; // ${READ_DB_URL} 등 환경변수로 주입

        @NotBlank(message = "Database username is required")
        private String username; // ${READ_DB_USERNAME} 등 환경변수로 주입

        @NotBlank(message = "Database password is required")
        private String password; // ${READ_DB_PASSWORD} 등 환경변수로 주입
    }
}
