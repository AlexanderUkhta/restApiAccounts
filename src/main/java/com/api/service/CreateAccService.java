package com.api.service;

import com.api.model.Account;
import com.api.repo.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateAccService {
    private static final Logger logger = LoggerFactory.getLogger(CreateAccService.class);

    @Autowired
    AccountRepository accountRepository;

    @Transactional
    public Integer createAccount(final String ownerName, final Double initialAmount) {
        Account account = new Account();
        account.setOwnerName(ownerName);
        account.setBalance(initialAmount);

        return accountRepository.save(account).getId();

    }

}
