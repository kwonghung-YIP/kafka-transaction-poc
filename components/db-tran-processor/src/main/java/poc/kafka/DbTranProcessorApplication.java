package poc.kafka;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import poc.kafka.pojo.ApplicationRequest;
import poc.kafka.pojo.AuditTrail;
import poc.kafka.repo.ApplicationRequestRepository;
import poc.kafka.repo.AuditTrailRepository;

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
            log.info("received Application Request:({}){}",request.getSeq(),request.getApplicationId());

            AuditTrail auditTrail = new AuditTrail();
            auditTrail.setApplicationId(request.getApplicationId());
            auditTrail.setEvent("SUBMIT");
            auditTrail.setActionBy("SYS");
            auditTrail.setActionOn(LocalDateTime.now());

            tranFunc.saveAuditTrial(auditTrail);

            tranFunc.saveAppRequest(request);
        };
    }

    @Component
    @RequiredArgsConstructor
    static public class TranFunc {

        final private ApplicationRequestRepository appReqRepo;

        final private AuditTrailRepository auditTrailRepo;

        final private StreamBridge streamBridge;

        @Transactional(label = {"saveAppRequest"}, propagation = Propagation.REQUIRED)
        public void saveAppRequest(ApplicationRequest request) {
            log.info("Saving application request into postgres DB...");
            appReqRepo.save(request);

            log.info("sending message to topic {}...","downstream-A");
            streamBridge.send("downstream-A",request.getApplicationId());

            log.info("sending message to topic {}...","downstream-B");
            streamBridge.send("downstream-B",request.getApplicationId());

            //throw new RuntimeException("Boom!!!!");
        }

        @Transactional(propagation = Propagation.REQUIRED)
        public void saveAuditTrial(AuditTrail auditTrail) {
            log.info("Saving audit trail into postgres DB...");
            auditTrailRepo.save(auditTrail);
        }
    }
}
