package poc.kafka.pojo;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "audit_trail")
public class AuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long seq;

    @Column(name="app_id")
    private UUID applicationId;

    private String event;

    @Column(name="action_by")
    private String actionBy;

    @Column(name="action_on")
    private LocalDateTime actionOn;

}