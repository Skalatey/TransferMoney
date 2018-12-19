package com.trn.service;


import com.trn.dto.AccountDto;
import com.trn.dto.TransferRequestDto;
import com.trn.entity.Account;
import com.trn.exception.AccountNotExistException;
import com.trn.exception.OverDraftException;
import com.trn.repository.AccountRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BankAccountService implements AccountService {

    public static final Logger logger =
            Logger.getLogger(BankAccountService.class);

    @Autowired
    private AccountRepository repository;

    @Override
    public List<Account> getAccounts() {
        return repository.findAll();
    }

    @Override
    public Account getAccount(Long accountId) {
        Assert.notNull(accountId, "Account id must not be null");
        Account account = repository.findOne(accountId);
        if (account == null) {
            throw new AccountNotExistException("Account with id:" + accountId + " is not found", "ACCOUNT_ERROR");
        }
        return account;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Account deposit(AccountDto accountDto) {
        Assert.notNull(accountDto, "Account id is not be null.");
        Account account = repository.getAndLockAccount(accountDto.getId());
        account.setAmount(plus(account.getAmount(), accountDto.getAmount()));
        repository.saveAndFlush(account);
        return account;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Account withdraw(AccountDto accountDto) {

        Assert.notNull(accountDto, "Account is not be null.");

        Account acc = repository.getAndLockAccount(accountDto.getId());
        BigDecimal subtract = subtract(acc.getAmount(), accountDto.getAmount());
        if (subtract.compareTo(BigDecimal.ZERO) < 0) {
            throw new OverDraftException("Account with id:" + accountDto.getId() + " does not have enough balance.", "CLIENT_ERROR");
        }
        acc.setAmount(subtract);
        repository.saveAndFlush(acc);

        return acc;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void transfer(TransferRequestDto transferDto) {

        double transferAmount = transferDto.getAmount();

        List<Account> list = repository
                .getAndLockTransferAccounts(transferDto.getIdToAccount(),
                        transferDto.getIdFromAccount());

        Map<Long, Account> map = list
                .stream()
                .collect(Collectors
                        .toMap(Account::getId, v -> v));

        if (!map.containsKey(transferDto.getIdToAccount())) {
            throw new AccountNotExistException("Account with id:" + transferDto.getIdToAccount() + " is not found", "ACCOUNT_ERROR");
        } else if (!map.containsKey(transferDto.getIdFromAccount())) {
            throw new AccountNotExistException("Account with id:" + transferDto.getIdFromAccount() + "  is not found", "ACCOUNT_ERROR");
        }

        if (transferDto.getIdToAccount().equals(transferDto.getIdFromAccount())) {
            throw new IllegalArgumentException("The same account transfer can not allow.");
        }

        Account accTo = map.get(transferDto.getIdToAccount());
        Account accFrom = map.get(transferDto.getIdFromAccount());

        if (accFrom.getAmount().compareTo(createBigDecimalAccount(transferAmount)) < 0) {
            throw new OverDraftException("Account with id:" + accFrom.getId() + " does not have enough balance to transfer.", "ACCOUNT_ERROR");
        }

        accFrom.setAmount(subtract(accFrom.getAmount(), transferAmount));
        accTo.setAmount(plus(accTo.getAmount(), transferAmount));

        repository.saveAndFlush(accFrom);
        repository.saveAndFlush(accTo);
    }

    private BigDecimal createBigDecimalAccount(double amount) {
        BigDecimal amountDec = new BigDecimal(amount);
        amountDec = amountDec.setScale(2, BigDecimal.ROUND_HALF_UP);
        return amountDec;
    }

    private BigDecimal plus(BigDecimal account,
                            double amount) {
        return account.add(createBigDecimalAccount(amount));
    }

    private BigDecimal subtract(BigDecimal account,
                                double amount) {
        return account.subtract(createBigDecimalAccount(amount));
    }
}
