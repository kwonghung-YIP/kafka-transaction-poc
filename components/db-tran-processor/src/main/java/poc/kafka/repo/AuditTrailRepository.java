package poc.kafka.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import poc.kafka.pojo.AuditTrail;

public interface AuditTrailRepository extends JpaRepository<AuditTrail,Long> {
}