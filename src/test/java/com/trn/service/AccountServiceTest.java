package com.trn.service;

import com.trn.dto.AccountDto;
import com.trn.entity.Account;
import com.trn.exception.OverDraftException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AccountServiceTest {

    private final static Long idAccount1 = 1L;
    private final static Long idAccount2 = 2L;

    @Autowired
    private AccountService service;

    @Before
    public void init() {
        Account one = service.getAccount(idAccount1);
        Account two = service.getAccount(idAccount2);

        one.setAmount(new BigDecimal("1000.00"));
        two.setAmount(new BigDecimal("1000.00"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenDepositGetNullThenThrowException() throws Exception {
        service.deposit(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenWithdrawGetNullThenThrowException() throws Exception {
        service.withdraw(null);
    }

    @Test(expected = OverDraftException.class)
    public void whenWithdrawOverDraftThenThrowException() throws Exception {
        service.withdraw(new AccountDto(idAccount1, 2000));
    }

    @Test
    public void testConcurrencyCalls() throws Exception {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors
                .newFixedThreadPool(Runtime.getRuntime()
                        .availableProcessors());

        for (int i = 0; i < 5; i++) {
            executor.execute(() -> service.withdraw(new AccountDto(idAccount1, 100)));
        }

        for (int i = 0; i < 10; i++) {
            executor.execute(() -> service.withdraw(new AccountDto(idAccount2, 100)));
        }

        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.SECONDS);


        Account oneAcc1 = service.getAccount(idAccount1);
        Account oneAcc2 = service.getAccount(idAccount2);

        Assert.assertEquals(new BigDecimal("500.00"), oneAcc1.getAmount());
        Assert.assertEquals(new BigDecimal("0.00"), oneAcc2.getAmount());
    }
}
