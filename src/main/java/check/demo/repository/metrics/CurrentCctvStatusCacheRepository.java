package check.demo.repository.metrics;

import check.demo.model.metrics.CurrentCctvStatusCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface CurrentCctvStatusCacheRepository extends JpaRepository<CurrentCctvStatusCache, CurrentCctvStatusCache.CacheId> {
    Optional<CurrentCctvStatusCache> findByIdCctvIdAndIdEnvironmentMode(Long cctvId, CurrentCctvStatusCache.EnvironmentMode environmentMode);

    List<CurrentCctvStatusCache> findByIdEnvironmentMode(CurrentCctvStatusCache.EnvironmentMode environmentMode);

    List<CurrentCctvStatusCache> findByIdEnvironmentModeAndCurrentStatusNot(
            CurrentCctvStatusCache.EnvironmentMode environmentMode,
            CurrentCctvStatusCache.CurrentStatus currentStatus
    );

    // 장애 상태(WARNING, CRITICAL) 조회
    List<CurrentCctvStatusCache> findByIdEnvironmentModeAndCurrentStatusIn(
            CurrentCctvStatusCache.EnvironmentMode environmentMode,
            List<CurrentCctvStatusCache.CurrentStatus> statusList
    );

    // 알림 필요한 상태 조회 (WARNING, CRITICAL)
    default List<CurrentCctvStatusCache> findErrorStatusByEnvironment(CurrentCctvStatusCache.EnvironmentMode environmentMode) {
        return findByIdEnvironmentModeAndCurrentStatusIn(
                environmentMode,
                Arrays.asList(CurrentCctvStatusCache.CurrentStatus.WARNING, CurrentCctvStatusCache.CurrentStatus.CRITICAL)
        );
    }

    // 정상 상태가 아닌 모든 상태 조회 (OFFLINE, WARNING, CRITICAL, UNKNOWN)
    default List<CurrentCctvStatusCache> findNonActiveStatusByEnvironment(CurrentCctvStatusCache.EnvironmentMode environmentMode) {
        return findByIdEnvironmentModeAndCurrentStatusNot(environmentMode, CurrentCctvStatusCache.CurrentStatus.ACTIVE);
    }
}
