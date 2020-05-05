package com.api.service;

import com.api.exception.NoRecordsFoundException;
import com.api.exception.NotEnoughFundsException;
import com.api.model.Account;
import com.api.repo.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class WithdrawMoneyService {
    private static final Logger logger = LoggerFactory.getLogger(WithdrawMoneyService.class);

    @Autowired
    AccountRepository accountRepository;
    @Autowired
    LockService lockService;

    @Transactional
    public Integer withdrawMoney(final Integer accountId, final Double amount) throws InterruptedException {
        try {
            lockService.takeLock(accountId);

            Optional<Account> accountOptional = accountRepository.findById(accountId);
            if (!accountOptional.isPresent()) {
                logger.warn("WithdrawMoneyService.findAccount: could not find account by id: " + accountId);
                throw new NoRecordsFoundException();
            }

            if (Double.compare(accountOptional.get().getBalance(), amount) < 0) {
                logger.warn("WithdrawMoneyService.findAccount: balance is less than desired amount, id: " + accountId);
                throw new NotEnoughFundsException();
            }

            logger.info("WithdrawMoneyService.findAccount - found account by id: {}. " +
                    "Payload is: {}", accountId, accountOptional.get());

            Account account = accountOptional.get();
            account.setBalance(account.getBalance() - amount);

            return accountRepository.saveAndFlush(account).getId();

        } finally {
            lockService.releaseLock(accountId);

        }

   }
}
