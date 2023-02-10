package poc.kafka;

import javax.persistence.EntityManagerFactory;

import org.springframework.cloud.stream.binder.BinderFactory;
import org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.transaction.ChainedKafkaTransactionManager;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.messaging.MessageChannel;
import org.springframework.orm.jpa.JpaTransactionManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
//@Configuration
public class TransactionManagerConfig {
    
    //@Autowired
    //private JpaTransactionManager jpaTranManager;

    //@Autowired
    //private KafkaTransactionManager kafkaTranManager;

    @Bean
    public JpaTransactionManager jpaTransactionManager(EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }

    //@Bean
    KafkaTransactionManager<byte[], byte[]> kafkaTransactionManager(BinderFactory bf) {
        return new KafkaTransactionManager<>(((KafkaMessageChannelBinder) bf.getBinder("kafka", MessageChannel.class))
                .getTransactionalProducerFactory());
    }

    @Bean
    public ChainedKafkaTransactionManager chainedKafkaTransactionManager(BinderFactory bf) {
        KafkaTransactionManager kafkaTranManager = new KafkaTransactionManager<>(((KafkaMessageChannelBinder) bf.getBinder("kafka", MessageChannel.class))
                .getTransactionalProducerFactory());
        return new ChainedKafkaTransactionManager<>(kafkaTranManager,jpaTransactionManager(null));
    }
}
