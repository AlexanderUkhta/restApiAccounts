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
    @Transactional
    public Map<String, Integer> transferMoney(final Integer sourceAccount, final Integer targetAccount, final Double amount) {
        Optional<Account> accountFromOptional = accountRepository.findById(sourceAccount);
        if (!accountFromOptional.isPresent()) {
            logger.warn("TransferMoneyService.findAccount: could not find source account by id: " + sourceAccount);
            throw new NoRecordsFoundException();
        }

        if (Double.compare(accountFromOptional.get().getBalance(), amount) < 0) {
            logger.warn("TransferMoneyService.findAccount: balance is less than desired transfer amount, id: " + sourceAccount);
            throw new NotEnoughFundsException();
        }

        Optional<Account> accountToOptional = accountRepository.findById(targetAccount);
        if (!accountToOptional.isPresent()) {
            logger.warn("TransferMoneyService.findAccount: could not find target account by id: " + targetAccount);
            throw new NoRecordsFoundException();
        }

        Account accountFrom = accountFromOptional.get();
        accountFrom.setBalance(accountFrom.getBalance() - amount);
        Account accountTo = accountToOptional.get();
        accountTo.setBalance(accountTo.getBalance() + amount);

        return new HashMap<String, Integer>() {{
            put("sourceId", accountRepository.save(accountFrom).getId());
            put("targetId", accountRepository.save(accountTo).getId());
        }};


    }
}
