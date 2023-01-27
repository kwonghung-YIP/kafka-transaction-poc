package poc.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.binder.BinderFactory;
import org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.messaging.MessageChannel;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.util.Random;
import java.util.function.Function;

@Slf4j
@SpringBootApplication
//@EnableTransactionManagement
public class SplitterApplication {

    public static void main(String[] args) {
        SpringApplication.run(SplitterApplication.class, args);
    }

    //@Bean
    public PlatformTransactionManager transactionManager(BinderFactory binders,
                                                         @Value("${unique.tx.id.per.instance:tx-123}") String txId) {

        ProducerFactory<byte[], byte[]> pf = ((KafkaMessageChannelBinder) binders.getBinder(null,
                MessageChannel.class)).getTransactionalProducerFactory();
        KafkaTransactionManager tm = new KafkaTransactionManager(pf);
        tm.setTransactionIdPrefix(txId);
        return tm;
    }


    public class TransactionalSplitter implements Function<Flux<Long>,Tuple3<Flux<BreakdownRec>,Flux<BreakdownRec>,Flux<BreakdownRec>>> {

        @Transactional
        @Override
        public Tuple3<Flux<BreakdownRec>, Flux<BreakdownRec>, Flux<BreakdownRec>> apply(Flux<Long> counter) {
            Flux<TotalRec> source = counter.map(ttl -> {
                var r1 = (long)Math.floor(Math.random()*ttl);
                var r2 = (long)Math.floor(Math.random()*(ttl-r1));
                var r3 = ttl - r1 - r2;

                TotalRec rec = new TotalRec(ttl,r1,r2,r3);
                log.info("splitter: counter:{}, {} + {} + {} = {}",rec.ttl,rec.r1,rec.r2,rec.r3,rec.r1+rec.r2+rec.r3);
                return rec;
            }).publish().autoConnect(3);

//            source.subscribe(rec -> {
//                log.info("splitter: counter:{}, {} + {} + {} = {}",rec.ttl,rec.r1,rec.r2,rec.r3,rec.r1+rec.r2+rec.r3);
//            });

            Flux<BreakdownRec> r1 = source.map(rec -> new BreakdownRec(rec.ttl,rec.r1));
            Flux<BreakdownRec> r2 = source.map(rec -> new BreakdownRec(rec.ttl,rec.r2));
            Flux<BreakdownRec> r3 = source.map(rec -> {
                        if (Math.random() > 0.95) {
                            throw new RuntimeException(String.format("breakdown#3: Raise exception when process %s,%s", rec.ttl, rec.r3));
                        } else {
                            return new BreakdownRec(rec.ttl, rec.r3);
                        }
                    });
//                    .onErrorContinue((throwable, rec) -> {
//                        log.error("breakdown#3: Raise exception when process {}", rec);
//                    });

            Tuple3 tuple = Tuples.of(r1,r2,r3);

            return tuple;
        }
    }

    //@Bean
    public Function<Flux<Long>,Tuple3<Flux<BreakdownRec>,Flux<BreakdownRec>,Flux<BreakdownRec>>> breakdown2() {
        return new TransactionalSplitter();
    }

    @Bean
    //public TransactionalSplitter<Long,BreakdownRec> breakdown() {
    public Function<Flux<Long>,Tuple3<Flux<BreakdownRec>,Flux<BreakdownRec>,Flux<BreakdownRec>>> breakdown() {
        return (counter) -> {
            Flux<TotalRec> source = counter.map(ttl -> {
                    var r1 = (long)Math.floor(Math.random()*ttl);
                    var r2 = (long)Math.floor(Math.random()*(ttl-r1));
                    var r3 = ttl - r1 - r2;

                    TotalRec rec = new TotalRec(ttl,r1,r2,r3);
                    log.info("splitter: counter:{}, {} + {} + {} = {}",rec.ttl,rec.r1,rec.r2,rec.r3,rec.r1+rec.r2+rec.r3);
                    return rec;
                }).publish().autoConnect(3);

//            source.subscribe(rec -> {
//                log.info("splitter: counter:{}, {} + {} + {} = {}",rec.ttl,rec.r1,rec.r2,rec.r3,rec.r1+rec.r2+rec.r3);
//            });

            Flux<BreakdownRec> r1 = source.map(rec -> {
                        if (Math.random() > 0.95) {
                            throw new RuntimeException(String.format("breakdown#1: Raise exception when process %s,%s", rec.ttl, rec.r1));
                        } else {
                            return new BreakdownRec(rec.ttl, rec.r1);
                        }
                    });
//                    .onErrorContinue((throwable, rec) -> {
//                        log.error("breakdown#1: Raise exception when process {}", rec);
//                    });
            Flux<BreakdownRec> r2 = source.map(rec -> {
                        if (Math.random() > 0.95) {
                            throw new RuntimeException(String.format("breakdown#2: Raise exception when process %s,%s", rec.ttl, rec.r2));
                        } else {
                            return new BreakdownRec(rec.ttl, rec.r2);
                        }
                    });
//                    .onErrorContinue((throwable, rec) -> {
//                        log.error("breakdown#2: Raise exception when process {}", rec);
//                    });
            Flux<BreakdownRec> r3 = source.map(rec -> {
                        if (Math.random() > 0.95) {
                            throw new RuntimeException(String.format("breakdown#3: Raise exception when process %s,%s", rec.ttl, rec.r3));
                        } else {
                            return new BreakdownRec(rec.ttl, rec.r3);
                        }
                    });
//                    .onErrorContinue((throwable, rec) -> {
//                        log.error("breakdown#3: Raise exception when process {}", rec);
//                    });

            Tuple3 tuple = Tuples.of(r1,r2,r3);

            return tuple;
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static public class BreakdownRec {

        private long ttl;
        private long r;
    }
}
