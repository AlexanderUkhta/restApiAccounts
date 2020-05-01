package com.api.service;

import com.api.exception.NoRecordsFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.api.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.api.repo.AccountRepository;

import java.util.Optional;

@Service
public class PutMoneyService {
    private static final Logger logger = LoggerFactory.getLogger(PutMoneyService.class);

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    public Integer putMoney(final Integer accountId, final Double amount) {
        Optional<Account> accountOptional = accountRepository.findById(accountId);
        if (!accountOptional.isPresent()) {
            logger.warn("PutMoneyService.findAccount: could not find account by id: " + accountId);
            throw new NoRecordsFoundException();
        }

        logger.info("PutMoneyService.findAccount - found account by id: {}. " +
                "Payload is: {}", accountId, accountOptional.get());

        Account account = accountOptional.get();
        account.setBalance(account.getBalance() + amount);

        return accountRepository.save(account).getId();

    }
}
