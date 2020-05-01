package com.apitest;

import com.api.app.MainApp;
import com.api.model.Account;
import com.api.model.TransactionDto;
import com.api.repo.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainApp.class)
@AutoConfigureMockMvc
@Sql({"/schema-test.sql"})
public class ApiControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(ApiControllerTest.class);

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AccountRepository accountRepository;
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void createAccounts_OK() throws Exception {
        List<TransactionDto> initialDatabaseData = Arrays.asList(
                new TransactionDto("Dave", 1100d),
                new TransactionDto("Nick", 1200d),
                new TransactionDto("Ben", 1300d),
                new TransactionDto("Sam", 1400d),
                new TransactionDto("Matthew", 1500d)
        );

        for (TransactionDto transactionDto : initialDatabaseData) {
            mockMvc.perform(post("/accounts/create-acc").contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(transactionDto))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());
        }
    }

    @Test
    public void putMoney_OK() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        HashMap<Integer, Account> accounts = new HashMap<>();
        accountRepository.findAll()
                .forEach(account -> {
                    accounts.put(account.getId(), account);

                    TransactionDto transactionDto = new TransactionDto();
                    transactionDto.setAccountMain(account.getId());
                    transactionDto.setAmount(500d);

                    CompletableFuture<Void> oneFuture = CompletableFuture.runAsync(() -> {
                        try {
                            mockMvc.perform(post("/accounts/create-acc").contentType(MediaType.APPLICATION_JSON)
                                    .content(mapper.writeValueAsString(transactionDto))
                                    .accept(MediaType.APPLICATION_JSON))
                                    .andExpect(status().isCreated());

                        } catch (Exception e) {
                            logger.warn("Error occurred while processing 'PutMoney' operation, trace: " + e);
                        }
                    });

                   transactionDto.setAmount(700d);
                    CompletableFuture<Void> twoFuture = CompletableFuture.runAsync(() -> {
                        try {
                            mockMvc.perform(post("/accounts/create-acc").contentType(MediaType.APPLICATION_JSON)
                                    .content(mapper.writeValueAsString(transactionDto))
                                    .accept(MediaType.APPLICATION_JSON))
                                    .andExpect(status().isCreated());

                        } catch (Exception e) {
                            logger.warn("Error occurred while processing 'PutMoney' operation, trace: " + e);
                        }
                    });

                    transactionDto.setAmount(900d);
                    CompletableFuture<Void> threeFuture = CompletableFuture.runAsync(() -> {
                        try {
                            mockMvc.perform(post("/accounts/create-acc").contentType(MediaType.APPLICATION_JSON)
                                    .content(mapper.writeValueAsString(transactionDto))
                                    .accept(MediaType.APPLICATION_JSON))
                                    .andExpect(status().isCreated());

                        } catch (Exception e) {
                            logger.warn("Error occurred while sending 'PutMoney' request, trace: " + e);
                        }
                    });

                    try {
                        CompletableFuture.allOf(oneFuture, threeFuture, threeFuture).get(10, TimeUnit.SECONDS);

                    } catch (Exception e) {
                        logger.warn("Error occurred while waiting for 'PutMoney' requests to finish, trace: " + e);
                    }

                    Optional<Account> optionalAccount = accountRepository.findById(account.getId());
                    Assert.isTrue(optionalAccount.get().getBalance() - account.getBalance() == 2100d,
                            "Actual and desired account balance don't match after several 'Put-amount'");

                });


        //threadPoolExecutor --> execute(new PutTask(accId, amount)) --> go to repo and check balance (no HTTP status assertion)

    }

    @Test
    public void withdrawMoney_OK() {

    }

    @Test
    public void transferMoney_OK() {

    }
}
