package com.trn.controller;

import com.trn.dto.AccountDto;
import com.trn.dto.TransferRequestDto;
import com.trn.entity.Account;
import com.trn.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping(value = "transaction")
public class TransferController {

    @Autowired
    private AccountService service;

    @PutMapping(value = "/withdrawal", consumes = "application/json;charset=UTF-8")
    public ResponseEntity<?> withdrawalMoney(@RequestBody AccountDto accountDto) {
        Account withdraw = service.withdraw(accountDto);
        return new ResponseEntity<>(withdraw, HttpStatus.OK);
    }

    @PutMapping(value = "/deposit", consumes = "application/json;charset=UTF-8")
    public ResponseEntity<?> depositMoney(@RequestBody AccountDto accountDto) {
        Account withdraw = service.deposit(accountDto);
        return new ResponseEntity<>(withdraw, HttpStatus.OK);
    }

    @PutMapping(value = "/transfer", consumes = "application/json;charset=UTF-8")
    public ResponseEntity<?> transfer(@RequestBody TransferRequestDto transferDto){
        service.transfer(transferDto);
        return new ResponseEntity(HttpStatus.OK);
    }
}
