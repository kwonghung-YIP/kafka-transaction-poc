package poc.kafka.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import poc.kafka.pojo.ApplicationRequest;

import java.util.UUID;

public interface ApplicationRequestRepository extends JpaRepository<ApplicationRequest, UUID> {
}