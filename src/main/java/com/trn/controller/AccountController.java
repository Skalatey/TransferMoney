package com.trn.controller;

import com.trn.entity.Account;
import com.trn.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AccountController {

    @Autowired
    private AccountService service;

    @GetMapping(value = "/accounts", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> getAccounts() {
        List<Account> accounts = service.getAccounts();
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    @GetMapping(value = "/accounts/{accountId}", produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> getAccount(@PathVariable Long accountId) {
        Account account = service.getAccount(accountId);
        return new ResponseEntity<>(account, HttpStatus.OK);
    }

}
