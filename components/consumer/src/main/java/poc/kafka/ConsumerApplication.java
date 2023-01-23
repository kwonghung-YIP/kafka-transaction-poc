package poc.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Consumer;

@Slf4j
@SpringBootApplication
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

    @Bean
    public Consumer<TotalRec> recvCounter() {
        return (rec) -> {
            log.info("aggregator: {} + {} + {} = {}, ttl:{}, checking:{}",rec.r1,rec.r2,rec.r3,
                    rec.r1+rec.r2+rec.r3,rec.ttl,(rec.r1+rec.r2+rec.r3)==rec.ttl);
        };
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static public class TotalRec {

        private long ttl;
        private long r1;
        private long r2;
        private long r3;

    }
}
