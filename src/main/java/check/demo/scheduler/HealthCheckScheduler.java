package check.demo.scheduler;

import check.demo.model.read.Cctv;
import check.demo.repository.read.CctvRepository;
import check.demo.service.HealthCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HealthCheckScheduler {

    private final HealthCheckService service;
    private final CctvRepository cctvRepository;

    // 10초마다 읽기 DB에서 대상 조회
    @Transactional(readOnly = true, transactionManager = "readTx")
    @Scheduled(cron = "*/10 * * * * *")
    public void run() {
        List<Cctv> targets = cctvRepository.findAll();
        // 한 줄씩 로그 출력
        for (Cctv cctv : targets ) {
            log.info("CCTV Loaded -> id={}, ip={}",
                    cctv.getId(),
                    cctv.getIpAddress());
        }
        for (Cctv t : targets) {
            if (t.getIpAddress() != null && !t.getIpAddress().isBlank()) {
                service.check(t.getId(), t.getIpAddress()); // ICMP + ffprobe
            }
        }
    }
}
