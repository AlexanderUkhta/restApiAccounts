package com.api.service;

import com.api.exception.NoRecordsFoundException;
import com.api.exception.NotEnoughFundsException;
import com.api.model.Account;
import com.api.repo.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TransferMoneyService {
    private static final Logger logger = LoggerFactory.getLogger(TransferMoneyService.class);

    @Autowired
    AccountRepository accountRepository;
    @Autowired
    LockService lockService;

    public Map<String, Integer> transferMoney(final Integer sourceAccount, final Integer targetAccount,
                                              final Double amount) throws InterruptedException {
        try {
            lockService.takeLock(sourceAccount < targetAccount ? sourceAccount : targetAccount);
            lockService.takeLock(sourceAccount < targetAccount ? targetAccount : sourceAccount);
            return transferMoneyTransact(sourceAccount, targetAccount, amount);

        } finally {
            lockService.releaseLock(sourceAccount);
            lockService.releaseLock(targetAccount);
        }
    }

    @Transactional
    public Map<String, Integer> transferMoneyTransact(final Integer sourceAccount, final Integer targetAccount,
                                                      final Double amount) {

        Optional<Account> accountFromOptional = accountRepository.findById(sourceAccount);
        if (!accountFromOptional.isPresent()) {
            logger.warn("TransferMoneyService.findAccount: could not find source account by id: " + sourceAccount);
            throw new NoRecordsFoundException();
        }
        logger.info("TransferMoneyService.findAccount - found account by id: {}. " +
                "Payload is: {}, Going to withdraw value {}", sourceAccount, accountFromOptional.get(), amount);

        Optional<Account> accountToOptional = accountRepository.findById(targetAccount);
        if (!accountToOptional.isPresent()) {
            logger.warn("TransferMoneyService.findAccount: could not find target account by id: " + targetAccount);
            throw new NoRecordsFoundException();
        }
        logger.info("TransferMoneyService.findAccount - found account by id: {}. " +
                "Payload is: {}, Going to receive value {}", targetAccount, accountToOptional.get(), amount);

        if (Double.compare(accountFromOptional.get().getBalance(), amount) < 0) {
            logger.warn("TransferMoneyService.findAccount: balance is less than desired transfer amount, id: " + sourceAccount);
            throw new NotEnoughFundsException();
        }

        Account accountFrom = accountFromOptional.get();
        Account accountTo = accountToOptional.get();
        accountFrom.setBalance(accountFrom.getBalance() - amount);
        accountTo.setBalance(accountTo.getBalance() + amount);

        return new HashMap<String, Integer>() {{
            put("sourceId", accountRepository.save(accountFrom).getId());
            put("targetId", accountRepository.save(accountTo).getId());
        }};

    }

}
