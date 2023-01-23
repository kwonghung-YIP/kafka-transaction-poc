package poc.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple3;

import java.util.function.Function;

@Slf4j
@SpringBootApplication
public class AggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AggregatorApplication.class, args);
    }

    @Bean
    public Function<Tuple3<Flux<BreakdownRec>,Flux<BreakdownRec>,Flux<BreakdownRec>>,Flux<TotalRec>> consolidate() {
        return (tuple) -> {
            Flux<TotalRec> sink =  Flux.zip(tuple.getT1(),tuple.getT2(),tuple.getT3())
                    .<TotalRec>flatMap(t3 -> {
                        TotalRec rec = new TotalRec(t3.getT1().ttl,t3.getT1().r,t3.getT2().r,t3.getT3().r);
                        log.info("aggregator: {} + {} + {} = {}, ttl:{}, checking:{}",rec.r1,rec.r2,rec.r3,
                                rec.r1+rec.r2+rec.r3,rec.ttl,(rec.r1+rec.r2+rec.r3)==rec.ttl);
                        return Flux.just(rec);
                    });


//            sink.subscribe(rec -> {
//                log.info("aggregator: {} + {} + {} = {}, ttl:{}, checking:{}",rec.r1,rec.r2,rec.r3,
//                        rec.r1+rec.r2+rec.r3,rec.ttl,(rec.r1+rec.r2+rec.r3)==rec.ttl);
//            });

            return sink;
        };
    }

    @Data
    @AllArgsConstructor
    static public class TotalRec {

        private long ttl;
        private long r1;
        private long r2;
        private long r3;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static public class BreakdownRec {

        private long ttl;
        private long r;
    }
}
