package poc.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import poc.kafka.pojo.ApplicationRequest;
import poc.kafka.repo.ApplicationRequestRepository;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@SpringBootApplication
@EnableTransactionManagement
public class DbTranProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DbTranProcessorApplication.class, args);
    }

    @Bean
    public Consumer<ApplicationRequest> saveRequest(TranFunc tranFunc) {
        return (request) -> {
            log.info("received Application Request:{} - {}",request.getSeq(),request.getApplicationId());
            tranFunc.saveAppRequest(request);
        };
    }

    @Component
    @RequiredArgsConstructor
    static public class TranFunc {

        final private ApplicationRequestRepository repo;

        final private StreamBridge streamBridge;

        @Transactional(label = {"saveAppRequest"}, propagation = Propagation.REQUIRED)

        public void saveAppRequest(ApplicationRequest request) {
            log.info("Saving application request into postgres DB...");
            repo.save(request);

            log.info("sending message to topic {}...","downstream-A");
            streamBridge.send("downstream-A",request.getApplicationId());

            log.info("sending message to topic {}...","downstream-B");
            streamBridge.send("downstream-B",request.getApplicationId());

            throw new RuntimeException("Boom!!!");
        }

    }
}
