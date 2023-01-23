package poc.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Supplier;

@Slf4j
@SpringBootApplication
public class ProducerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProducerApplication.class, args);
    }

    private long counter = 1000;
    @Bean
    public Supplier<Long> sendCounter() {
        return () -> {
            log.info("Send counter {} to topic",counter);
            return counter++;
        };
    }
}
