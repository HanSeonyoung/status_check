CREATE TABLE IF NOT EXISTS health_metrics (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    cctv_id BIGINT NULL,
    event_code VARCHAR(255) NULL,
    hls_status BOOLEAN NOT NULL,
    icmp_avg_rtt_ms DOUBLE NULL,
    icmp_packet_loss_pct DOUBLE NULL,
    icmp_status BOOLEAN NULL,
    event_timestamp TIMESTAMP(6) NULL
);
