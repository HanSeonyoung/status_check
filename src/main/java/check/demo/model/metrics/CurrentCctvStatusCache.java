package check.demo.model.metrics;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name= "current_cctv_status_cache")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentCctvStatusCache {
    @EmbeddedId
    private CacheId id;

    @Column(name = "current_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrentStatus currentStatus = CurrentStatus.UNKNOWN;

    @Column(name = "event_code", length = 50)
    private String eventCode;

    @Column(name = "responsible_role")
    @Enumerated(EnumType.STRING)
    private ResponsibleRole responsibleRole;

    @Column(name = "severity", nullable = false)
    @Enumerated(EnumType.STRING)
    private Severity severity = Severity.INFO;

    @Column(name = "last_health_metric_id")
    private Long lastHealthMetricId;

    @Column(name = "last_update_at", nullable = false)
    private LocalDateTime lastUpdateAt;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheId implements Serializable {
        @Column(name = "cctv_id")
        private Long cctvId;

        @Column(name = "environment_mode")
        @Enumerated(EnumType.STRING)
        private EnvironmentMode environmentMode;
    }

    public enum CurrentStatus {
        UNKNOWN, ACTIVE, OFFLINE, WARNING, CRITICAL
    }

    public enum ResponsibleRole {
        NETWORK_TECH, DEVICE_TECH, ADMIN
    }

    public enum Severity {
        INFO, WARNING, CRITICAL
    }

    // TODO: need to add profile when db changed
    public enum EnvironmentMode {
        DEV_ENV1, DEV_ENV2, PROD_ENV1, PROD_ENV2
    }

    @Column(name = "icmp_avg_rtt_ms")
    private Double icmpAvgRttMs;

    @Column(name = "icmp_packet_loss_pct")
    private Double icmpPacketLossPct;
}
