package check.demo.service;

import check.demo.dto.HealthMetricEventDto;
import check.demo.model.FFProbeResult;
import check.demo.model.HealthMetric;
import check.demo.repository.HealthMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
    private final KafkaEventProducer producer;
    private final IcmpChecker icmpChecker; 
//    private final FfprobeChecker ffprobeChecker;

    @Async
    public void check(Long cctvId, String ip) {
        String rtspUrl = String.format("rtsp://%s:%s@%s:%s%s", username, password, ip, port, path);
        log.info("rtsp://{}:*****@{}:{}{}", username, ip, port, path);

        IcmpChecker.IcmpResult icmp = icmpChecker.check(ip);
        FFProbeResult result = runFFProbe(rtspUrl);
//        FfprobeChecker.StreamStatus streamStatus = ffprobeChecker.check(rtspUrl);

        HealthMetric metric = new HealthMetric();
        metric.setCctvId(cctvId);
        metric.setTimestamp(LocalDateTime.now());

        if (icmp == null) {
            // ❌ ping 자체 실패
            metric.setIcmpStatus(false);
            metric.setEventCode("ICMP_FAILED");
            metric.setIcmpAvgRttMs(null);
            metric.setIcmpPacketLossPct(null);
        } else if (!icmp.isSuccess()) {
            // ❗ 응답 없음
            metric.setIcmpStatus(false);
            metric.setEventCode("ICMP_TIMEOUT");
            metric.setIcmpAvgRttMs(icmp.getAvgRtt());
            metric.setIcmpPacketLossPct(icmp.getPacketLossPct());
        } else if (icmp.getPacketLossPct() > 0) {
            // ⚠ 패킷 손실 있음
            metric.setIcmpStatus(true); // 응답은 있으므로 true
            metric.setEventCode("ICMP_LOSS");
            metric.setIcmpAvgRttMs(icmp.getAvgRtt());
            metric.setIcmpPacketLossPct(icmp.getPacketLossPct());
        } else {
            // ✅ 정상 응답
            metric.setIcmpStatus(true);
            metric.setEventCode("ICMP_OK");
            metric.setIcmpAvgRttMs(icmp.getAvgRtt());
            metric.setIcmpPacketLossPct(icmp.getPacketLossPct());
        }
      
        switch (result.getStatus()) {
            case OK -> {
                metric.setHlsStatus(true);
                metric.setEventCode(icmp.isSuccess() ? "HLS_OK" : "ICMP_FAIL");
            }
            case TIMEOUT -> {
                metric.setHlsStatus(false);
                metric.setEventCode("HLS_TIMEOUT");
            }
            case ERROR -> {
                metric.setHlsStatus(false);
                metric.setEventCode("HLS_ERROR");
            }
            case PORT_UNREACHABLE -> {
                metric.setHlsStatus(false);
                metric.setEventCode("RTSP_PORT_FAIL");
            }
        }

        repository.save(metric);

        HealthMetricEventDto dto = new HealthMetricEventDto();
        dto.setCctvId(metric.getCctvId());
        dto.setTimestamp(metric.getTimestamp());
        dto.setIcmpStatus(metric.isIcmpStatus());
        dto.setHlsStatus(metric.isHlsStatus());
        dto.setEventCode(metric.getEventCode());
        dto.setIcmpAvgRttMs(metric.getIcmpAvgRttMs());
        dto.setIcmpPacketLossPct(metric.getIcmpPacketLossPct());

//        producer.sendEvent(dto);
    }
}