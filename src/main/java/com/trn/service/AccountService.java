package com.trn.service;

import com.trn.dto.AccountDto;
import com.trn.dto.TransferRequestDto;
import com.trn.entity.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    List<Account> getAccounts();

    Account getAccount(Long accountId);

    Account deposit(AccountDto accountDto);

    Account withdraw(AccountDto accountDto);

    void transfer(TransferRequestDto transferDto);
}
