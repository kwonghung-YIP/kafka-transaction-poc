package poc.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@SpringBootApplication
public class DbTranProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DbTranProducerApplication.class, args);
    }

    private long seq = 1;

    @Bean
    public Supplier<ApplicationRequest> produce() {
        return () -> {
            UUID appId = UUID.randomUUID();
            log.info("Send application request: ({}) {}",seq,appId);
            return new ApplicationRequest(appId,seq++,"channelId","A","S");
        };
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationRequest {
        private UUID applicationId;

        private long seq;

        private String channelId;

        private String requestType;

        private String status;
    }
}
