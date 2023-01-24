package poc.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class ReactorFlowTests {

    //@Test
    public void flowTest() {

        AtomicLong counter = new AtomicLong();
        counter.set(100);

        Flux<TotalRec> source = Flux.interval(Duration.ofMillis(1000 * 1))
                .map(l -> counter.incrementAndGet())
                .map(ttl -> {
                    var r1 = (long) Math.floor(Math.random() * (ttl - 2));
                    var r2 = (long) Math.floor(Math.random() * (ttl - r1 - 1));
                    var r3 = ttl - r1 - r2;

                    TotalRec rec = new TotalRec(ttl, r1, r2, r3);
                    //log.info("source: {} + {} + {} = {}",rec.r1,rec.r2,rec.r3,rec.ttl);
                    return rec;
                })
                .publish().autoConnect(3);

        source.subscribe(rec -> {
            log.info("source: {} + {} + {} = {}", rec.r1, rec.r2, rec.r3, rec.ttl);
        });

        Flux<BreakdownRec> r1 = source.map(rec -> new BreakdownRec(rec.ttl, rec.r1));
        Flux<BreakdownRec> r2 = source.map(rec -> new BreakdownRec(rec.ttl, rec.r2));
        Flux<BreakdownRec> r3 = source.map(rec -> {
                    if (Math.random() > 0.2) {
                        return new BreakdownRec(rec.ttl, rec.r3);
                    } else {
                        throw new RuntimeException(String.format("breakdown#3: Raise exception when process %s,%s", rec.ttl, rec.r3));
                    }
                })
                .onErrorContinue((throwable, rec) -> {
                    log.error("breakdown#3: Raise exception when process {}", rec);
                });

        r1.subscribe(rec -> {
            log.info("breakdown#1: total:{}, breakdown:{}", rec.ttl, rec.r);
        });
        r2.subscribe(rec -> {
            log.info("breakdown#2: total:{}, breakdown:{}", rec.ttl, rec.r);
        });
        r3.subscribe(rec -> {
            log.info("breakdown#3: total:{}, breakdown:{}", rec.ttl, rec.r);
        });

        Tuple3<Flux<BreakdownRec>, Flux<BreakdownRec>, Flux<BreakdownRec>> tuple = Tuples.of(r1, r2, r3);

        Flux<TotalRec> sink = Flux.zip(tuple.getT1(), tuple.getT2(), tuple.getT3())
                .<TotalRec>flatMap(t3 -> {
                    TotalRec ttlRec = new TotalRec(t3.getT1().ttl, t3.getT1().r, t3.getT2().r, t3.getT3().r);
                    return Flux.just(ttlRec);
                });


        sink.subscribe(rec -> {
            log.info("sink: {} + {} + {} = {}, ttl:{}, checking:{}", rec.r1, rec.r2, rec.r3,
                    rec.r1 + rec.r2 + rec.r3, rec.ttl, (rec.r1 + rec.r2 + rec.r3) == rec.ttl);
        });
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
