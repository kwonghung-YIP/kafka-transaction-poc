package poc.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.binder.BinderFactory;
import org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.messaging.MessageChannel;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;

@Slf4j
@SpringBootApplication
//@EnableTransactionManagement
public class Splitter2Application {

    public static void main(String[] args) {
        SpringApplication.run(Splitter2Application.class, args);
    }

    @Autowired
    private StreamBridge streamBridge;

    //@Bean
    public PlatformTransactionManager transactionManager(BinderFactory binders,
                                                         @Value("${unique.tx.id.per.instance:tx-123}") String txId) {

        ProducerFactory<byte[], byte[]> pf = ((KafkaMessageChannelBinder) binders.getBinder(null,
                MessageChannel.class)).getTransactionalProducerFactory();
        KafkaTransactionManager tm = new KafkaTransactionManager(pf);
        tm.setTransactionIdPrefix(txId);
        return tm;
    }

    @Bean
    //@Transactional
    public Consumer<Long> breakdown() {
        return (ttl) -> {
            var r1 = (long)Math.floor(Math.random()*ttl);
            var r2 = (long)Math.floor(Math.random()*(ttl-r1));
            var r3 = ttl - r1 - r2;

            log.info("splitter: counter:{}, {} + {} + {} = {}",ttl,r1,r2,r3,r1+r2+r3);

            streamBridge.send("split0",new BreakdownRec(ttl,r1));
            if (Math.random()>0.95) {
                log.error("Throw exception in splitter after sending message to split0 for counter {}",ttl);
                throw new RuntimeException("Throw exception in splitter after sending message to split0");
            }
            streamBridge.send("split1",new BreakdownRec(ttl,r2));
            if (Math.random()>0.95) {
                log.error("Throw exception in splitter after sending message to split1 for counter {}",ttl);
                throw new RuntimeException("Throw exception in splitter after sending message to split1");
            }
            streamBridge.send("split2",new BreakdownRec(ttl,r3));
            if (Math.random()>0.95) {
                log.error("Throw exception in splitter after sending message to split3 for counter {}",ttl);
                throw new RuntimeException("Throw exception in splitter after sending message to split2");
            }
        };
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static public class BreakdownRec {

        private long ttl;
        private long r;
    }
}
