package com.api.controller;

import com.api.exception.NoRecordsFoundException;
import com.api.exception.NotEnoughFundsException;
import com.api.model.TransactionDto;
import com.api.service.CreateAccService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.api.service.WithdrawMoneyService;
import com.api.service.PutMoneyService;
import com.api.service.TransferMoneyService;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("accounts")
public class ApiController {
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    CreateAccService createAccService;
    @Autowired
    WithdrawMoneyService withdrawMoneyService;
    @Autowired
    PutMoneyService putMoneyService;
    @Autowired
    TransferMoneyService transferMoneyService;

    @PostMapping("/create-acc")
    public ResponseEntity<String> processAccCreating(@Valid @RequestBody TransactionDto transactionDto) {
        return new ResponseEntity<>("'Acc-creating' processed successfully, created accountId " +
                createAccService.createAccount(transactionDto.getOwnerName(), transactionDto.getAmount()),
                HttpStatus.CREATED);

    }

    @PostMapping("/put-amount")
    public ResponseEntity<String> processMoneyPutting(@Valid @RequestBody TransactionDto transactionDto) {
        try {
            return new ResponseEntity<>("'Put-amount' processed successfully for account_id = " +
                    putMoneyService.putMoney(transactionDto.getAccountMain(), transactionDto.getAmount()), HttpStatus.OK);

        } catch (NoRecordsFoundException e) {
            return new ResponseEntity<>("'Put-amount' could not be processed, account does not exist.",
                    HttpStatus.NOT_FOUND);

        } catch (InterruptedException e) {
            return new ResponseEntity<>("'Put-amount' could not be processed, account was busy for too long.",
                    HttpStatus.LOCKED);
        }

    }

    @PostMapping("/withdraw-amount")
    public ResponseEntity<String> processMoneyTaking(@Valid @RequestBody TransactionDto transactionDto) {
        try {
            return new ResponseEntity<>("'Withdraw-amount' processed successfully for account_id = " +
                    withdrawMoneyService.withdrawMoney(transactionDto.getAccountMain(), transactionDto.getAmount()),
                    HttpStatus.OK);

        } catch (NoRecordsFoundException e) {
            return new ResponseEntity<>("'Withdraw-amount' could not be processed, account does not exist.",
                    HttpStatus.NOT_FOUND);

        } catch (NotEnoughFundsException e) {
            return new ResponseEntity<>("'Withdraw-amount' could not be processed, not enough money.",
                    HttpStatus.BAD_REQUEST);

        } catch (InterruptedException e) {
            return new ResponseEntity<>("'Withdraw-amount' could not be processed, account was busy for too long.",
                    HttpStatus.LOCKED);
        }

    }

    @PostMapping("/transfer-amount")
    public ResponseEntity<String> processMoneyTransfer(@Valid @RequestBody TransactionDto transactionDto) {
        try {
            Map<String, Integer> resultIds = transferMoneyService.transferMoney(transactionDto.getAccountMain(),
                    transactionDto.getAccountExternal(), transactionDto.getAmount());

            return new ResponseEntity<>(
                    String.format("'Transfer-amount' processed successfully from id=%s to id=%s.",
                            resultIds.get("sourceId"), resultIds.get("targetId")), HttpStatus.OK);

        } catch (NoRecordsFoundException e) {
            return new ResponseEntity<>("'Transfer-amount' could not be processed, one or both accounts don't exist.",
                    HttpStatus.NOT_FOUND);

        } catch (NotEnoughFundsException e) {
            return new ResponseEntity<>("'Transfer-amount' could not be processed, not enough money on source id.",
                    HttpStatus.BAD_REQUEST);

        } catch (InterruptedException e) {
            return new ResponseEntity<>("'Transfer-amount' could not be processed, one or both accounts were busy for too long.",
                    HttpStatus.LOCKED);
        }

    }

}
