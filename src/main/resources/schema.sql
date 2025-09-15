--version old
--CREATE TABLE IF NOT EXISTS health_metrics (
--    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
--    cctv_id BIGINT NULL,
--    event_code VARCHAR(255) NULL,
--    hls_status BOOLEAN NOT NULL,
--    icmp_avg_rtt_ms DOUBLE NULL,
--    icmp_packet_loss_pct DOUBLE NULL,
--    icmp_status BOOLEAN NULL,
--    event_timestamp TIMESTAMP(6) NULL
--);
-- TODO: need to activate last version of ddl on profile test
--version new
CREATE TABLE IF NOT EXISTS health_metrics (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    cctv_id BIGINT NULL,
    event_timestamp DATETIME(6) NULL,
    icmp_status_enum VARCHAR(255) NULL,
    ffprobe_status_enum VARCHAR(255) NULL,
    event_code VARCHAR(255) NULL,
    icmp_avg_rtt_ms DOUBLE NULL,
    icmp_min_rtt_ms DOUBLE NULL,
    icmp_max_rtt_ms DOUBLE NULL,
    icmp_packet_loss_pct DOUBLE NULL
);
-- TODO: must active all of these query on profile test
CREATE TABLE IF NOT EXISTS current_cctv_status_cache (
    cctv_id BIGINT NOT NULL COMMENT 'CCTV 고유 ID',
    environment_mode ENUM('DEV_ENV1', 'DEV_ENV2', 'PROD_ENV1', 'PROD_ENV2') NOT NULL COMMENT '환경 구분',
    current_status ENUM('UNKNOWN', 'ACTIVE', 'OFFLINE', 'WARNING', 'CRITICAL')
        NOT NULL DEFAULT 'UNKNOWN' COMMENT '관리자용 축약 상태',
    event_code VARCHAR(50) NULL COMMENT '이벤트 코드 (ok, device_down, stream_port_fail 등)',
    responsible_role ENUM('NETWORK_TECH', 'DEVICE_TECH', 'ADMIN') NULL COMMENT '담당 역할',
    severity ENUM('INFO', 'WARNING', 'CRITICAL') NOT NULL DEFAULT 'info' COMMENT '심각도',
    last_health_metric_id BIGINT NULL COMMENT '마지막 health_metrics ID 참조',
    icmp_avg_rtt_ms DOUBLE NULL COMMENT 'ICMP 평균 응답시간 (ms)',
    icmp_packet_loss_pct DOUBLE NULL COMMENT 'ICMP 패킷 손실률 (%)',
    last_update_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '캐시 갱신 시간',

    PRIMARY KEY (cctv_id, environment_mode)
) DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='CCTV 현재 상태 캐시 - health_metrics 기반 집계, 환경별 공통 참조';

CREATE TABLE IF NOT EXISTS cctv (
    id BIGINT PRIMARY KEY,
    ip_address VARCHAR(255),
    deleted_at TIMESTAMP
);