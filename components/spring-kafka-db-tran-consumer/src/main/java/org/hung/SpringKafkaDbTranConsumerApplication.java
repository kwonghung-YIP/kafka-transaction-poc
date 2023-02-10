package org.hung;

import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.hung.pojo.Txn;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class SpringKafkaDbTranConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringKafkaDbTranConsumerApplication.class, args);
	}

	@KafkaListener(topics = {"debt-txn","credit-txn"})
	public void readTxn(ConsumerRecord<UUID,Txn> record) {
		log.info("Received Txn {} from topic {}",record.value(),record.topic());
	}
}
