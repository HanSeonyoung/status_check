package check.demo.service;

import check.demo.model.FFProbeResult;
import check.demo.model.IcmpResult;
import check.demo.model.metrics.HealthMetric;
import check.demo.repository.metrics.HealthMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static check.demo.service.FFProbeUtil.runFFProbe;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckService {

    @Value("${RTSP_USERNAME}")
    private String username;

    @Value("${RTSP_PASSWORD}")
    private String password;

    @Value("${RTSP_PORT}")
    private String port;

    @Value("${RTSP_PATH}")
    private String path;

    private final HealthMetricRepository repository;
    private final IcmpChecker icmpChecker;

    private static final double HIGH_RTT_THRESHOLD_MS = 200.0; // 고 RTT 기준 (ms)

    @Async
    @Transactional("metricsTx") // 메트릭 DB 트랜잭션
    public void check(Long cctvId, String ip) {
        String rtspUrl = String.format("rtsp://%s:%s@%s:%s%s", username, password, ip, port, path);
        log.info("rtsp://{}:*****@{}:{}{}", username, ip, port, path);

        IcmpResult icmp = icmpChecker.check(ip);
        FFProbeResult ffprobe = runFFProbe(rtspUrl);

        HealthMetric metric = new HealthMetric();
        metric.setCctvId(cctvId);
        metric.setEventTimestamp(LocalDateTime.now());

        metric.setIcmpStatusEnum(icmp.getStatus().name());
        metric.setFfprobeStatusEnum(ffprobe.getStatus().name());
        metric.setIcmpAvgRttMs(icmp.getAvgRttMs());
        metric.setIcmpPacketLossPct(icmp.getPacketLossPct());
        metric.setIcmpMinRttMs(icmp.getMinRttMs());
        metric.setIcmpMaxRttMs(icmp.getMaxRttMs());

        // eventCode 계산
        String eventCode = calculateEventCode(icmp, ffprobe);
        metric.setEventCode(eventCode);

        // 메트릭 DB 저장
        repository.save(metric);
    }

    private String calculateEventCode(IcmpResult icmp, FFProbeResult ffprobe) {
        // UNDEFINED 상태 체크 (8번 레코드)
        if (icmp.getStatus() == IcmpResult.Status.UNDEFINED || ffprobe.getStatus() == FFProbeResult.Status.UNDEFINED) {
            return "UNDEFINED";
        }

        // ICMP와 FFProbe 모두 OK일 때 (1번 레코드)
        if (icmp.getStatus() == IcmpResult.Status.OK && ffprobe.getStatus() == FFProbeResult.Status.OK
                && (icmp.getAvgRttMs() == null || icmp.getAvgRttMs() < HIGH_RTT_THRESHOLD_MS)
                && (icmp.getPacketLossPct() == null || icmp.getPacketLossPct() == 0)) {
            return "OK";
        }

        // ICMP 상태 기반 코드
        String icmpCode;
        switch (icmp.getStatus()) {
            case OK:
                if (icmp.getAvgRttMs() != null && icmp.getAvgRttMs() >= HIGH_RTT_THRESHOLD_MS) {
                    icmpCode = "NETWORK_CONGESTION"; // 레코드 4, 5
                } else if (icmp.getPacketLossPct() != null && icmp.getPacketLossPct() > 0) {
                    icmpCode = "ICMP_LOSS";
                } else {
                    icmpCode = "ICMP_OK";
                }
                break;
            case TIMEOUT:
                icmpCode = "ICMP_TIMEOUT";
                break;
            case FAILED:
                icmpCode = "ICMP_FAILED";
                break;
            default:
                icmpCode = "UNKNOWN";
        }

        // FFProbe 상태 기반 코드
        String ffprobeCode;
        switch (ffprobe.getStatus()) {
            case OK:
                ffprobeCode = "HLS_OK";
                break;
            case TIMEOUT:
                ffprobeCode = "HLS_TIMEOUT"; // 레코드 6
                break;
            case ERROR:
                ffprobeCode = "STREAM_DATA_CORRUPTION"; // 레코드 7
                break;
            case PORT_UNREACHABLE:
                ffprobeCode = "RTSP_PORT_FAIL"; // 레코드 3
                break;
            default:
                ffprobeCode = "UNKNOWN";
        }

        // 조합 로직 (주어진 테이블 기반)
        if (icmp.getStatus() == IcmpResult.Status.FAILED && ffprobe.getStatus() == FFProbeResult.Status.PORT_UNREACHABLE) {
            return "DEVICE_DOWN"; // 레코드 2
        } else if (icmp.getStatus() == IcmpResult.Status.OK && icmp.getAvgRttMs() != null && icmp.getAvgRttMs() >= HIGH_RTT_THRESHOLD_MS && ffprobe.getStatus() == FFProbeResult.Status.OK) {
            return "NETWORK_CONGESTION"; // 레코드 4
        } else if (icmp.getStatus() == IcmpResult.Status.OK && icmp.getAvgRttMs() != null && icmp.getAvgRttMs() >= HIGH_RTT_THRESHOLD_MS && ffprobe.getStatus() == FFProbeResult.Status.ERROR) {
            return "NETWORK_OVERLOAD"; // 레코드 5
        } else if (icmp.getStatus() == IcmpResult.Status.OK && ffprobe.getStatus() == FFProbeResult.Status.TIMEOUT) {
            return "SESSION_OR_RTSP_BUFFER"; // 레코드 6
        } else if (icmp.getStatus() == IcmpResult.Status.OK && ffprobe.getStatus() == FFProbeResult.Status.ERROR) {
            return "STREAM_DATA_CORRUPTION"; // 레코드 7
        } else if (icmp.getStatus() == IcmpResult.Status.OK && ffprobe.getStatus() == FFProbeResult.Status.PORT_UNREACHABLE) {
            return "RTSP_PORT_FAIL"; // 레코드 3
        }

        // 기본적으로 FFProbe 우선, 없으면 ICMP 코드
        return ffprobeCode.equals("HLS_OK") ? icmpCode : ffprobeCode;
    }

}
