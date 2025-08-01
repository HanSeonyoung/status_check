package check.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class HealthMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cctvId;
    private LocalDateTime timestamp;
    private boolean icmpStatus;
    private boolean hlsStatus;
    private String eventCode;

    private Double icmpAvgRttMs;         // RTT(ms) 평균
    private Double icmpPacketLossPct;    // 패킷 손실률(%)
}
