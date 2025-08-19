// src/main/java/check/demo/controller/DashboardController.java
package check.demo.controller;

import check.demo.dto.NetworkDashboardDto;
import check.demo.model.IcmpResult;
import check.demo.model.metrics.HealthMetric;
import check.demo.repository.metrics.HealthMetricRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final HealthMetricRepository metricsRepo;

    // 예: GET /api/dashboard/123?lim=100
    @GetMapping("/{id}")
    public List<NetworkDashboardDto> getMetrics(
            @PathVariable("id") Long cctvId,
            @RequestParam(name = "lim", defaultValue = "100") int lim
    ) {
        if (lim <= 0) lim = 1;
        if (lim > 1000) lim = 1000;

        Pageable page = PageRequest.of(0, lim, Sort.by(Sort.Direction.DESC, "eventTimestamp"));
        List<HealthMetric> rows = metricsRepo.findByCctvId(cctvId, page).getContent();

        return rows.stream().map(this::toDto).toList();
    }

    private NetworkDashboardDto toDto(HealthMetric m) {
        IcmpResult.Status status = parseStatus(m.getIcmpStatusEnum());

        return NetworkDashboardDto.builder()
                .timestamp(m.getEventTimestamp())
                .status(status)
                .avgRttMs(m.getIcmpAvgRttMs())
                .maxRttMs(m.getIcmpMaxRttMs())
                .minRttMs(m.getIcmpMinRttMs())
                .packetLossPct(m.getIcmpPacketLossPct())
                .build();
    }

    private IcmpResult.Status parseStatus(String raw) {
        if (raw == null) return IcmpResult.Status.UNDEFINED;
        try { return IcmpResult.Status.valueOf(raw); }
        catch (IllegalArgumentException e) { return IcmpResult.Status.UNDEFINED; }
    }
}
