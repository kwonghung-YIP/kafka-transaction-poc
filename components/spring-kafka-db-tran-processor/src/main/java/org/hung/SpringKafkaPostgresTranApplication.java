package org.hung;

import java.util.Optional;
import java.util.UUID;

import org.hung.pojo.Account;
import org.hung.pojo.Txn;
import org.hung.repo.AccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@EnableTransactionManagement
public class SpringKafkaPostgresTranApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringKafkaPostgresTranApplication.class, args);
	}

	@Bean
	public ApplicationRunner runner(TransferService service) {
		return (args) -> {
			long counter = 1;
			while (true && counter <= 10) {
				log.info("Tranfer #{}", counter);
				service.transfer(counter, "john", "peter", 200d);
				Thread.sleep(1000*1);
				counter++;
			}
		};
	}

	@Component
	@RequiredArgsConstructor
	static public class TransferService {

		@Value("${app.error-rate:0.90}")
		private double errorRate;

		final private KafkaTemplate<UUID,Txn> template;
		
		final private AccountRepository accountRepo;

		@Transactional(transactionManager="transactionManager")
		public void transfer(long counter, String fromName, String toName, double transferAmount) throws AccountNotFoundException {

			Optional<Account> fromAcct = accountRepo.findByName(fromName);
			fromAcct.orElseThrow(() -> new AccountNotFoundException(fromName));

			Optional<Account> toAcct = accountRepo.findByName(toName);
			toAcct.orElseThrow(() -> new AccountNotFoundException(toName));

			fromAcct.ifPresent(from -> {
				if (from.getBalance() < transferAmount) {
					throw new InsufficientBalanceException(fromName,from.getBalance(),transferAmount);
				} else {
					from.setBalance(from.getBalance()-transferAmount);
					accountRepo.save(from);
					log.info("{} Account balance after transfer {}", from.getName(), from.getBalance());
					if (Math.random() > 1+errorRate) {		
						throw new RuntimeException("throw exception after debt balance ...");
					}
					
					template.send("debt-txn", new Txn(from.getId(),counter,"DR",transferAmount));
					if (Math.random() > 1+errorRate) {
						throw new RuntimeException("throw exception after sent first message ...");
					}

					toAcct.ifPresent(to -> {
						to.setBalance(to.getBalance()+transferAmount);
						accountRepo.save(to);
						log.info("{} Account balance after transfer {}", to.getName(), to.getBalance());
						if (Math.random() > errorRate) {		
							throw new RuntimeException("throw exception after credit balance ...");
						}

						template.send("credit-txn", new Txn(to.getId(),counter,"CR",transferAmount));
						if (Math.random() > errorRate) {
							throw new RuntimeException("throw exception after sent second message ...");
						}
					});
				}
			});
		}
	}

	@RequiredArgsConstructor
	static public class AccountNotFoundException extends Exception {
		final private String name;
	}
	
	@RequiredArgsConstructor
	static public class InsufficientBalanceException extends RuntimeException {
		final private String name;
		final private double balance;
		final private double debtAmount;
	}

	// @Bean
	// public JpaTransactionManager transactionManager(EntityManagerFactory factory) {
	// 	return new JpaTransactionManager(factory);
	// };

	// @Bean
	// public ChainedKafkaTransactionManager chainedKafkaTransactionManager(
	// 	JpaTransactionManager jpaTransactionManager,
	// 	KafkaTransactionManager kafkaTransactionManager
	// ) {
	// 	return new ChainedKafkaTransactionManager<>(jpaTransactionManager,kafkaTransactionManager);
	// }
}
