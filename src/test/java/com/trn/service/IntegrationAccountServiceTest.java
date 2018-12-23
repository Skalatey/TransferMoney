package com.trn.service;


import com.trn.dto.AccountDto;
import com.trn.dto.TransferRequestDto;
import com.trn.entity.Account;
import com.trn.repository.AccountRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
public class IntegrationAccountServiceTest {

    private Long idAccount1;

    private Long idAccount2;

    @Autowired
    private AccountService service;

    @Autowired
    private AccountRepository repository;

    @Before
    public void init() throws Exception {
        Account account1 = new Account();
        account1.setAmount(BigDecimal.valueOf(1000.00));

        Account account2 = new Account();
        account2.setAmount(BigDecimal.valueOf(1000.00));

        repository.saveAndFlush(account1);
        repository.saveAndFlush(account2);

        idAccount1 = account1.getId();
        idAccount2 = account2.getId();
    }

    @After
    public void clear() throws Exception {
        repository.delete(idAccount1);
        repository.delete(idAccount2);
    }


    @Test
    public void testConcurrencyCalls() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors
                .newFixedThreadPool(Runtime.getRuntime()
                        .availableProcessors());

        //1000 - 500 = 500
        for (int i = 0; i < 5; i++) {
            System.out.println("Add execute withdraw = " + i);
            executor.execute(() -> {
                System.out.println("Start withdraw " + Thread.currentThread().getName());
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Execute withdraw " + Thread.currentThread().getName());
                service.withdraw(new AccountDto(idAccount1, 100));
            });
        }

        //1000 + 400 = 1400
        for (int i = 0; i < 4; i++) {
            System.out.println("Add execute deposit = " + i);
            executor.execute(() -> {
                System.out.println("Start deposit " + Thread.currentThread().getName());
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Execute deposit " + Thread.currentThread().getName());
                service.deposit(new AccountDto(idAccount2, 100));
            });
        }

        //1 = 500 + 100 = 600
        //2 = 1400 - 100 = 1300
        System.out.println("Add execute transfer");
        executor.execute(() -> {
            System.out.println("Start transfer " + Thread.currentThread().getName());
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Execute transfer " + Thread.currentThread().getName());
            service.transfer(new TransferRequestDto(idAccount1, idAccount2, 100));
        });
        TimeUnit.SECONDS.sleep(10);
        latch.countDown();

        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.SECONDS);


        Account oneAcc1 = service.getAccount(idAccount1);
        Account oneAcc2 = service.getAccount(idAccount2);

        Assert.assertEquals(new BigDecimal("600.00"), oneAcc1.getAmount());
        Assert.assertEquals(new BigDecimal("1300.00"), oneAcc2.getAmount());
    }
}
