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
import reactor.util.function.Tuples;

import java.util.Random;
import java.util.function.Function;

@Slf4j
@SpringBootApplication
public class SplitterApplication {

    public static void main(String[] args) {
        SpringApplication.run(SplitterApplication.class, args);
    }

    @Bean
    public Function<Flux<Long>, Tuple3<Flux<BreakdownRec>,Flux<BreakdownRec>,Flux<BreakdownRec>>> breakdown() {
        return (counter) -> {
            Flux<TotalRec> source = counter.map(ttl -> {
                    var r1 = (long)Math.floor(Math.random()*ttl);
                    var r2 = (long)Math.floor(Math.random()*(ttl-r1));
                    var r3 = ttl - r1 - r2;

                    TotalRec rec = new TotalRec(ttl,r1,r2,r3);
                    //log.info("source: {} + {} + {} = {}",rec.r1,rec.r2,rec.r3,rec.ttl);
                    return rec;
                }).publish().autoConnect(3);

            source.subscribe(rec -> {
                log.info("splitter: counter:{}, {} + {} + {} = {}",rec.ttl,rec.r1,rec.r2,rec.r3,rec.r1+rec.r2+rec.r3);
            });

            Flux<BreakdownRec> r1 = source.map(rec -> new BreakdownRec(rec.ttl,rec.r1));
            Flux<BreakdownRec> r2 = source.map(rec -> new BreakdownRec(rec.ttl,rec.r2));
            Flux<BreakdownRec> r3 = source.map(rec -> new BreakdownRec(rec.ttl,rec.r3));

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
