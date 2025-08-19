// src/main/java/check/demo/repository/metrics/HealthMetricRepository.java
package check.demo.repository.metrics;

import check.demo.model.metrics.HealthMetric;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthMetricRepository extends JpaRepository<HealthMetric, Long> {
    Page<HealthMetric> findByCctvId(Long cctvId, Pageable pageable);
}
