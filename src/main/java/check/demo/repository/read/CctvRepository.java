package check.demo.repository.read;

import check.demo.model.read.Cctv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CctvRepository extends JpaRepository<Cctv, Long> {
    List<Cctv> findByDeletedAtIsNull();
}
