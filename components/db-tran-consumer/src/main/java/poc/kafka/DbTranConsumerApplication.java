package poc.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@SpringBootApplication
public class DbTranConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DbTranConsumerApplication.class, args);
    }

    @Bean
    public Consumer<Message<UUID>> consume() {
        return (msg) -> {
            MessageHeaders headers = msg.getHeaders();
            UUID payload = msg.getPayload();
            String recvTopic = headers.get(KafkaHeaders.RECEIVED_TOPIC,String.class);
            log.info("Received message from topic {} - {}",recvTopic,payload);
        };
    }
}
