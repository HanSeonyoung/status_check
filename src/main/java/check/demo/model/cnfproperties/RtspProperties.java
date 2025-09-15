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
@ConfigurationProperties(prefix = "rtsp")
@Component
@Data
@Validated
public class RtspProperties {
    @NotBlank(message = "RTSP username is required")
    private String username; // ${RTSP_USERNAME} 환경변수로 주입

    @NotBlank(message = "RTSP password is required")
    private String password; // ${RTSP_PASSWORD} 환경변수로 주입

    @NotBlank(message = "RTSP port is required")
    @Pattern(regexp = "^\\d{1,5}$", message = "RTSP port must be a valid port number")
    private String port; // ${RTSP_PORT} 환경변수로 주입

    @NotBlank(message = "RTSP path is required")
    @Pattern(regexp = "^/.*", message = "RTSP path must start with /")
    private String path; // ${RTSP_PATH} 환경변수로 주입
}
