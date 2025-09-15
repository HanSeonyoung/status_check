package check.demo.service;

import check.demo.model.metrics.CurrentCctvStatusCache;
import check.demo.repository.metrics.CurrentCctvStatusCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DevStatusCacheService {
    private final CurrentCctvStatusCacheRepository cacheRepository;

    private static record StatusMapping(
            CurrentCctvStatusCache.CurrentStatus currentStatus,
            CurrentCctvStatusCache.ResponsibleRole responsibleRole,
            CurrentCctvStatusCache.Severity severity
    ) {}

    @Transactional("metricsTx")
    public void updateStatusCache(Long cctvId, String eventCode, Long healthMetricId,
                                  String environment, Double avgRttMs, Double packetLossPct) {
        // dev 환경에서만 업데이트 (DEV_ENV1, DEV_ENV2)
        if (!environment.startsWith("DEV_ENV")) {
            log.debug("Skipping dev cache update for environment: {}", environment);
            return;
        }

        CurrentCctvStatusCache.EnvironmentMode envMode;
        switch (environment) {
            case "DEV_ENV1":
                envMode = CurrentCctvStatusCache.EnvironmentMode.DEV_ENV1;
                break;
            case "DEV_ENV2":
                envMode = CurrentCctvStatusCache.EnvironmentMode.DEV_ENV2;
                break;
            default:
                log.warn("Unsupported dev environment for cache update: {}", environment);
                return;
        }

        // 기존 캐시 데이터 조회
        Optional<CurrentCctvStatusCache> existing = cacheRepository.findByIdCctvIdAndIdEnvironmentMode(cctvId, envMode);

        // 이전 event_code와 비교
        if (existing.isPresent() && eventCode.equals(existing.get().getEventCode())) {
            log.debug("Event code unchanged for CCTV {} in {}: {}", cctvId, environment, eventCode);
            return; // 변화 없으면 업데이트 하지 않음
        }

        log.info("[DEV] Updating status cache for CCTV {} in {} with new event code: {}", cctvId, environment, eventCode);

        // 상태 매핑
        StatusMapping mapping = mapEventCodeToStatus(eventCode);

        CurrentCctvStatusCache cache = existing.orElse(new CurrentCctvStatusCache());
        cache.setId(new CurrentCctvStatusCache.CacheId(cctvId, envMode));
        cache.setCurrentStatus(mapping.currentStatus());
        cache.setEventCode(eventCode);
        cache.setResponsibleRole(mapping.responsibleRole());
        cache.setSeverity(mapping.severity());
        cache.setLastHealthMetricId(healthMetricId);
        cache.setLastUpdateAt(LocalDateTime.now());
        cache.setIcmpAvgRttMs(avgRttMs);
        cache.setIcmpPacketLossPct(packetLossPct);

        cacheRepository.save(cache);

        log.info("[DEV] Status cache updated for CCTV {} in {}: status={}, role={}, severity={}",
                cctvId, environment, mapping.currentStatus(), mapping.responsibleRole(), mapping.severity());

        // 향후 확장: Kafka 이벤트 발행 지점
//        publishCacheUpdateEvent(cctvId, environment, cache);
    }

    private StatusMapping mapEventCodeToStatus(String eventCode) {
        switch (eventCode.toUpperCase()) {
            case "OK":
            case "ICMP_OK":
            case "HLS_OK":
                return new StatusMapping(
                        CurrentCctvStatusCache.CurrentStatus.ACTIVE,
                        null,
                        CurrentCctvStatusCache.Severity.INFO
                );

            case "DEVICE_DOWN":
                return new StatusMapping(
                        CurrentCctvStatusCache.CurrentStatus.OFFLINE,
                        CurrentCctvStatusCache.ResponsibleRole.DEVICE_TECH,
                        CurrentCctvStatusCache.Severity.CRITICAL
                );

            case "STREAM_PORT_FAIL":
            case "RTSP_PORT_FAIL":
                return new StatusMapping(
                        CurrentCctvStatusCache.CurrentStatus.CRITICAL,
                        CurrentCctvStatusCache.ResponsibleRole.DEVICE_TECH,
                        CurrentCctvStatusCache.Severity.CRITICAL
                );

            case "NETWORK_CONGESTION":
                return new StatusMapping(
                        CurrentCctvStatusCache.CurrentStatus.WARNING,
                        CurrentCctvStatusCache.ResponsibleRole.NETWORK_TECH,
                        CurrentCctvStatusCache.Severity.INFO
                );

            case "NETWORK_OVERLOAD":
                return new StatusMapping(
                        CurrentCctvStatusCache.CurrentStatus.CRITICAL,
                        CurrentCctvStatusCache.ResponsibleRole.NETWORK_TECH,
                        CurrentCctvStatusCache.Severity.CRITICAL
                );

            case "SESSION_OR_RTSP_BUFFER":
            case "HLS_TIMEOUT":
                return new StatusMapping(
                        CurrentCctvStatusCache.CurrentStatus.CRITICAL,
                        CurrentCctvStatusCache.ResponsibleRole.DEVICE_TECH,
                        CurrentCctvStatusCache.Severity.CRITICAL
                );

            case "STREAM_DATA_CORRUPTION":
                return new StatusMapping(
                        CurrentCctvStatusCache.CurrentStatus.CRITICAL,
                        CurrentCctvStatusCache.ResponsibleRole.DEVICE_TECH,
                        CurrentCctvStatusCache.Severity.CRITICAL
                );

            case "UNDEFINED":
                return new StatusMapping(
                        CurrentCctvStatusCache.CurrentStatus.WARNING,
                        CurrentCctvStatusCache.ResponsibleRole.ADMIN,
                        CurrentCctvStatusCache.Severity.WARNING
                );

            case "NETWORK_UNREACHABLE":
                return new StatusMapping(
                        CurrentCctvStatusCache.CurrentStatus.WARNING,
                        CurrentCctvStatusCache.ResponsibleRole.NETWORK_TECH,
                        CurrentCctvStatusCache.Severity.WARNING
                );
            case "ICMP_LOSS":
            case "ICMP_TIMEOUT":
            case "ICMP_FAILED":
            case "UNKNOWN":
            default:
                return new StatusMapping(
                        CurrentCctvStatusCache.CurrentStatus.UNKNOWN,
                        CurrentCctvStatusCache.ResponsibleRole.ADMIN,
                        CurrentCctvStatusCache.Severity.WARNING
                );
        }
    }
}
