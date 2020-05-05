package com.apitest;

import com.api.app.MainApp;
import com.api.model.Account;
import com.api.model.TransactionDto;
import com.api.repo.AccountRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MainApp.class)
@AutoConfigureMockMvc
@Sql({"/schema-test.sql"})
public class ApiControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(ApiControllerTest.class);
    private ObjectMapper mapper = new ObjectMapper();
    private HashMap<Integer, Account> accountsOld = new HashMap<>();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AccountRepository accountRepository;

    @Before
    public void createBefore() throws Exception {
        accountRepository.saveAndFlush(new Account(1, "Dave", 1100d));
        accountRepository.saveAndFlush(new Account(2, "Nick", 1200d));
        accountRepository.saveAndFlush(new Account(3, "Ben", 1300d));
        accountRepository.saveAndFlush(new Account(4, "Sam", 1400d));
        accountRepository.saveAndFlush(new Account(5, "Matthew", 1500d));
    }

    @Test
    public void createAccountsTest() throws Exception {
        List<TransactionDto> initialDatabaseData = Arrays.asList(
                new TransactionDto("Mike", 2100d),
                new TransactionDto("Becky", 2200d),
                new TransactionDto("Bill", 2300d)
        );
        for (TransactionDto transactionDto : initialDatabaseData) {
            mockMvc.perform(post("/accounts/create-acc").contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(transactionDto))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());
        }

        List<Account> currentAccounts = accountRepository.findAll();
        Assert.assertEquals(8, currentAccounts.size());
        Assert.assertTrue(currentAccounts.stream()
                .anyMatch(account ->
                        (account.getOwnerName().equals("Mike") && Double.compare(account.getBalance(), 2100d) == 0)));
        Assert.assertTrue(currentAccounts.stream()
                .anyMatch(account ->
                        (account.getOwnerName().equals("Becky") && Double.compare(account.getBalance(), 2200d) == 0)));
        Assert.assertTrue(currentAccounts.stream()
                .anyMatch(account ->
                        (account.getOwnerName().equals("Bill") && Double.compare(account.getBalance(), 2300d) == 0)));

    }

    @Test
    public void putMoneyTest() {
        accountRepository.findAll()
                .forEach(account -> {
                    CompletableFuture<Void> oneFuture = CompletableFuture.runAsync(() -> {
                        try {
                            sendPutMoney(account.getId(), 500d);
                        } catch (Exception e) {
                            logger.warn("Error occurred while processing 'Put-amount' operation, trace: " + e);
                        }
                    });

                    CompletableFuture<Void> twoFuture = CompletableFuture.runAsync(() -> {
                        try {
                            sendPutMoney(account.getId(), 700d);
                        } catch (Exception e) {
                            logger.warn("Error occurred while processing 'Put-amount' operation, trace: " + e);
                        }
                    });

                    CompletableFuture<Void> threeFuture = CompletableFuture.runAsync(() -> {
                        try {
                            sendPutMoney(account.getId(), 900d);
                        } catch (Exception e) {
                            logger.warn("Error occurred while sending 'Put-amount' request, trace: " + e);
                        }
                    });

                    try {
                        CompletableFuture.allOf(oneFuture, twoFuture, threeFuture).get(10, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        logger.warn("Error occurred while waiting for 'Put-amount' requests to finish, trace: " + e);
                    }

                    Optional<Account> optionalAccount = accountRepository.findById(account.getId());
                    Assert.assertEquals("Actual and desired account balances don't match after several 'Put-amount'",
                            2100d, optionalAccount.get().getBalance() - account.getBalance(), 0.0);

                });

    }

    @Test
    public void withdrawMoneyTest() {
        accountRepository.findAll()
                .forEach(account -> {
                    CompletableFuture<Void> oneFuture = CompletableFuture.runAsync(() -> {
                        try {
                            MockHttpServletResponse response = sendWithdrawMoney(account.getId(), 100d);
                            Assert.assertEquals(200, response.getStatus());
                        } catch (Exception e) {
                            logger.warn("Error occurred while processing 'Withdraw-amount' operation, trace: " + e);
                        }
                    });

                    CompletableFuture<Void> twoFuture = CompletableFuture.runAsync(() -> {
                        try {
                            MockHttpServletResponse response = sendWithdrawMoney(account.getId(), 200d);
                            Assert.assertEquals(200, response.getStatus());
                        } catch (Exception e) {
                            logger.warn("Error occurred while processing 'Withdraw-amount' operation, trace: " + e);
                        }
                    });

                    CompletableFuture<Void> threeFuture = CompletableFuture.runAsync(() -> {
                        try {
                            MockHttpServletResponse response = sendWithdrawMoney(account.getId(), 300d);
                            Assert.assertEquals(200, response.getStatus());
                        } catch (Exception e) {
                            logger.warn("Error occurred while sending 'Withdraw-amount' request, trace: " + e);
                        }
                    });

                    CompletableFuture<Void> fourFuture = CompletableFuture.runAsync(() -> {
                        try {
                            MockHttpServletResponse response = sendWithdrawMoney(account.getId(), 3000d);
                            Assert.assertEquals(400, response.getStatus());
                            Assert.assertEquals("'Withdraw-amount' could not be processed, not enough money.",
                                    response.getContentAsString());
                        } catch (Exception e) {
                            logger.warn("Error occurred while sending 'Withdraw-amount' request, trace: " + e);
                        }
                    });

                    CompletableFuture<Void> fiveFuture = CompletableFuture.runAsync(() -> {
                        try {
                            MockHttpServletResponse response = sendWithdrawMoney(account.getId(), 400d);
                            Assert.assertEquals(200, response.getStatus());
                        } catch (Exception e) {
                            logger.warn("Error occurred while sending 'Withdraw-amount' request, trace: " + e);
                        }
                    });

                    try {
                        CompletableFuture.allOf(oneFuture, twoFuture, threeFuture, fourFuture, fiveFuture)
                                .get(10, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        logger.warn("Error occurred while waiting for 'Withdraw-amount' requests to finish, trace: " + e);
                    }

                    Optional<Account> optionalAccount = accountRepository.findById(account.getId());
                    System.out.println(optionalAccount.get());
                    Assert.assertEquals("Actual and desired account balances don't match after several 'Withdraw-amount'",
                            1000d,  account.getBalance() - optionalAccount.get().getBalance(), 0.0);

                });

        try {
            MockHttpServletResponse response = sendWithdrawMoney(777, 100d);
            Assert.assertEquals(404, response.getStatus());
            Assert.assertEquals("'Withdraw-amount' could not be processed, account does not exist.",
                    response.getContentAsString());

        } catch (Exception e) {
            logger.warn("Error occurred while sending 'Withdraw-amount' request, trace: " + e);
        }

    }

    @Test
    public void transferMoneyTest_OK() {
        CompletableFuture<Void> oneFuture = CompletableFuture.runAsync(() -> {
            try {
                MockHttpServletResponse response = sendTransferMoney(1, 2, 100d);
                Assert.assertEquals(200, response.getStatus());
            } catch (Exception e) {
                logger.warn("Error occurred while processing 'Transfer-amount' operation, trace: " + e);
            }
        });

        CompletableFuture<Void> twoFuture = CompletableFuture.runAsync(() -> {
            try {
                MockHttpServletResponse response = sendTransferMoney(2, 1, 200d);
                Assert.assertEquals(200, response.getStatus());
            } catch (Exception e) {
                logger.warn("Error occurred while processing 'Transfer-amount' operation, trace: " + e);
            }
        });

        CompletableFuture<Void> threeFuture = CompletableFuture.runAsync(() -> {
            try {
                MockHttpServletResponse response = sendTransferMoney(2, 1, 150d);
                Assert.assertEquals(200, response.getStatus());
            } catch (Exception e) {
                logger.warn("Error occurred while sending 'Transfer-amount' request, trace: " + e);
            }
        });

        CompletableFuture<Void> fourFuture = CompletableFuture.runAsync(() -> {
            try {
                MockHttpServletResponse response = sendTransferMoney(1, 2, 80d);
                Assert.assertEquals(200, response.getStatus());
            } catch (Exception e) {
                logger.warn("Error occurred while processing 'Transfer-amount' operation, trace: " + e);
            }
        });

        CompletableFuture<Void> fiveFuture = CompletableFuture.runAsync(() -> {
            try {
                MockHttpServletResponse response = sendTransferMoney(2, 4, 200d);
                Assert.assertEquals(200, response.getStatus());
            } catch (Exception e) {
                logger.warn("Error occurred while processing 'Transfer-amount' operation, trace: " + e);
            }
        });

        CompletableFuture<Void> sixFuture = CompletableFuture.runAsync(() -> {
            try {
                MockHttpServletResponse response = sendTransferMoney(3, 4, 150d);
                Assert.assertEquals(200, response.getStatus());
            } catch (Exception e) {
                logger.warn("Error occurred while sending 'Transfer-amount' request, trace: " + e);
            }
        });

        CompletableFuture<Void> sevenFuture = CompletableFuture.runAsync(() -> {
            try {
                MockHttpServletResponse response = sendTransferMoney(4, 3, 850d);
                Assert.assertEquals(200, response.getStatus());
            } catch (Exception e) {
                logger.warn("Error occurred while sending 'Transfer-amount' request, trace: " + e);
            }
        });

        try {
            CompletableFuture.allOf(oneFuture, twoFuture, threeFuture, fourFuture, fiveFuture, sixFuture, sevenFuture)
                    .get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("Error occurred while waiting for 'Transfer-amount' requests to finish, trace: " + e);
        }

        Optional<Account> account = accountRepository.findById(1);
        Assert.assertEquals(
                "Actual and desired account balances for acc 1 don't match after several 'Transfer-amount'",
                1270d, account.get().getBalance(), 0.0);

        account = accountRepository.findById(2);
        Assert.assertEquals(
                "Actual and desired account balances for acc 2 don't match after several 'Transfer-amount'",
                830d, account.get().getBalance(), 0.0);

        account = accountRepository.findById(3);
        Assert.assertEquals(
                "Actual and desired account balances for acc 3 don't match after several 'Transfer-amount'",
                2000d, account.get().getBalance(), 0.0);

        account = accountRepository.findById(4);
        Assert.assertEquals(
                "Actual and desired account balances for acc 4 don't match after several 'Transfer-amount'",
                900d, account.get().getBalance(), 0.0);

    }

    @Test
    public void transferMoneyTest_nOK() {
        try {
            MockHttpServletResponse response = sendTransferMoney(777, 2, 900d);
            Assert.assertEquals(404, response.getStatus());
            Assert.assertEquals("'Transfer-amount' could not be processed, one or both accounts don't exist.",
                    response.getContentAsString());

            response = sendTransferMoney(777, 888, 900d);
            Assert.assertEquals(404, response.getStatus());
            Assert.assertEquals("'Transfer-amount' could not be processed, one or both accounts don't exist.",
                    response.getContentAsString());

            response = sendTransferMoney(1, 2, 9000d);
            Assert.assertEquals(400, response.getStatus());
            Assert.assertEquals("'Transfer-amount' could not be processed, not enough money on source id.",
                    response.getContentAsString());

        } catch (Exception e) {
            logger.warn("Error occurred while sending 'Transfer-amount' request, trace: " + e);
        }


    }
    private void sendPutMoney(final Integer id, final Double amount) throws Exception {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAccountMain(id);
        transactionDto.setAmount(amount);
        mockMvc.perform(post("/accounts/put-amount").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(transactionDto))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    private MockHttpServletResponse sendWithdrawMoney(final Integer id, final Double amount) throws Exception {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAccountMain(id);
        transactionDto.setAmount(amount);
        return mockMvc.perform(post("/accounts/withdraw-amount").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(transactionDto))
                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

    }

    private MockHttpServletResponse sendTransferMoney(final Integer idFrom, final Integer idTo, final Double amount)
            throws Exception {
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setAccountMain(idFrom);
        transactionDto.setAccountExternal(idTo);
        transactionDto.setAmount(amount);
        return mockMvc.perform(post("/accounts/transfer-amount").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(transactionDto))
                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

    }

}
