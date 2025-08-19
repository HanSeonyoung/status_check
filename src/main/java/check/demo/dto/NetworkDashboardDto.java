// src/main/java/check/demo/dto/NetworkDashboardDto.java
package check.demo.dto;

import check.demo.model.IcmpResult;
import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class NetworkDashboardDto {
    private LocalDateTime timestamp;
    private final IcmpResult.Status status;
    private final Double avgRttMs;
    private final Double maxRttMs;
    private final Double minRttMs;
    private final Double packetLossPct;
}
