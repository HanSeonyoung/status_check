package check.demo.scheduler;

import check.demo.service.HealthCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HealthCheckScheduler {

    private final HealthCheckService service;

    //Kafka 등록 목록으로 대체 예정임돵 아직은 가데이터임
    private final List<CctvTarget> targets = List.of(
            new CctvTarget(1L, "172.30.29.101"),
            new CctvTarget(2L, "172.30.29.23"),
            new CctvTarget(3L, "192.168.32.32")
    );

    @Scheduled(cron = "*/10 * * * * *")
    public void run() {
        for (CctvTarget target : targets) {
            service.check(target.id(), target.ip()); 
        }
    }

    // 간단한 CCTV 정보 클래스
    public record CctvTarget(Long id, String ip) {}
}
