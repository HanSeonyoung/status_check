package check.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HealthMetricEventDto {
    private Long cctvId;
    private LocalDateTime timestamp;
    private String icmpStatusEnum;  // "OK", "TIMEOUT", "FAILED"
    private String ffprobeStatusEnum;  // "OK", "TIMEOUT", "ERROR", "PORT_UNREACHABLE"
    private String eventCode;

    private Double icmpAvgRttMs;
    private Double icmpPacketLossPct;
}
