package com.zarema.wallet_app;

import com.zarema.wallet_app.model.Wallet;
import com.zarema.wallet_app.repository.WalletRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class WalletAppApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("wallet")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private WalletRepository walletRepository;

    @AfterEach
    void cleanup() {
        walletRepository.deleteAll();
    }

	@Test
	void deposit_shouldIncreaseBalance() throws Exception {

		Wallet wallet = new Wallet();
		wallet.setBalance(new BigDecimal("200.00"));
		walletRepository.save(wallet);

		mockMvc.perform(post("/api/v1/wallet")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
                {
                  "walletId": "%s",
                  "operationType": "DEPOSIT",
                  "amount": 100.00
                }
                """.formatted(wallet.getId())))
				.andExpect(status().isOk());
	}

	@Test
	void withdraw_shouldWithdrawAmountFromBalance() throws Exception{
		Wallet wallet = new Wallet();
		wallet.setBalance(new BigDecimal("200.00"));
		walletRepository.save(wallet);

		mockMvc.perform(post("/api/v1/wallet")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
							"walletId": "%s",
							"operationType": "WITHDRAW",
							"amount": 100.00
						}
						""".formatted(wallet.getId())))
				.andExpect(status().isOk());
	}

	@Test
	void withdraw_insufficientFunds_shouldThrowInsufficientFundsException() throws Exception{
		Wallet wallet = new Wallet();
		wallet.setBalance(new BigDecimal("200.00"));
		walletRepository.save(wallet);

		mockMvc.perform(post("/api/v1/wallet")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
						{
							"walletId": "%s",
							"operationType": "WITHDRAW",
							"amount": 250.00
						}
						""".formatted(wallet.getId())))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INSUFFICIENT_FUNDS"));
	}

	@Test
	void withdraw_negativeAmount_shouldThrowValidationException() throws Exception{
		UUID id = UUID.randomUUID();

		mockMvc.perform(post("/api/v1/wallet")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
						{
							"walletId": "%s",
							"operationType": "WITHDRAW",
							"amount": -250.00
						}
						""".formatted(id)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_ARGUMENTS"))
				.andExpect(jsonPath("$.errors.amount").exists());
	}

	@Test
	void withdraw_invalidJson_shouldThrowJsonException() throws Exception{
		Wallet wallet = new Wallet();
		wallet.setBalance(new BigDecimal("200.00"));
		walletRepository.save(wallet);

		mockMvc.perform(post("/api/v1/wallet")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
						{
							"walletId": "%s",
							"operationType": "WITHDRAW",,
							"amount": 150.00
						}
						""".formatted(wallet.getId())))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_JSON_SYNTAX"));
	}

	@Test
	void getBalance_shouldReturnWalletBalance() throws Exception{
		Wallet wallet = new Wallet();
		wallet.setBalance(new BigDecimal("200.00"));
		walletRepository.save(wallet);

		mockMvc.perform(get("/api/v1/wallets/{id}", wallet.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").value(200.00));
	}

	@Test
	void getBalance_shouldThrowWalletNotFoundException() throws Exception{
		UUID id = UUID.randomUUID();

		mockMvc.perform(get("/api/v1/wallets/{id}", id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("WALLET_NOT_FOUND"));
	}

	@Test
	void concurrentWithdrawals_shouldHandleBalanceCorrectly() throws InterruptedException {
		Wallet wallet = new Wallet();
		wallet.setBalance(new BigDecimal("1000.00"));
		walletRepository.save(wallet);

		int numberOfThreads = 10;
		BigDecimal withdrawAmount = new BigDecimal("100.00");

		ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
		CountDownLatch latch = new CountDownLatch(numberOfThreads);

		for (int i = 0; i < numberOfThreads; i++) {
			executorService.execute(() -> {
				try {
					mockMvc.perform(post("/api/v1/wallet")
									.contentType(MediaType.APPLICATION_JSON)
									.content("""
                        {
                            "walletId": "%s",
                            "operationType": "WITHDRAW",
                            "amount": %s
                        }
                        """.formatted(wallet.getId(), withdrawAmount)))
							.andExpect(status().isOk());
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executorService.shutdown();

		Wallet updatedWallet = walletRepository.findById(wallet.getId()).orElseThrow();
		assertEquals(0, new BigDecimal("0.00").compareTo(updatedWallet.getBalance()),
				"Balance should be 0.00");
	}
}
