package poc.kafka.pojo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Data
@Entity
@Table(name = "application_request")
public class ApplicationRequest {
    @Id
    @Column(name="app_id")
    private UUID applicationId;

    private long seq;

    @Column(name="channel_id")
    private String channelId;

    @Column(name="reqest_type")
    private String requestType;

    private String status;
}