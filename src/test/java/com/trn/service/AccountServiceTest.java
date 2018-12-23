package com.trn.service;

import com.trn.dto.AccountDto;
import com.trn.dto.TransferRequestDto;
import com.trn.entity.Account;
import com.trn.exception.OverDraftException;
import com.trn.repository.AccountRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AccountServiceTest {

    @MockBean
    private AccountRepository repository;

    @Autowired
    private AccountService service;

    private Long idAccount;

    private Account account;

    @Before
    public void init() {
        idAccount = 10L;
        account = new Account();
        account.setId(idAccount);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenDepositGetNegativeValue_ThenThrowException() throws Exception {
        account.setAmount(BigDecimal.valueOf(10000.00));

        when(repository.getAndLockAccount(any(Long.class))).thenReturn(account);

        AccountDto accountDto = new AccountDto();
        accountDto.setId(idAccount);
        accountDto.setAmount(-2000);

        service.deposit(accountDto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenDepositGetNull_ThenThrowException() throws Exception {
        service.deposit(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenWithdrawGetNull_ThenThrowException() throws Exception {
        service.withdraw(null);
    }

    @Test(expected = OverDraftException.class)
    public void whenWithdrawOverDraft_ThenThrowException() throws Exception {

        account.setAmount(BigDecimal.valueOf(1000.00));

        when(repository.getAndLockAccount(any(Long.class))).thenReturn(account);

        AccountDto accountDto = new AccountDto();
        accountDto.setId(idAccount);
        accountDto.setAmount(2000);

        service.withdraw(accountDto);
    }

    @Test
    public void whenGetDeposit_thenSuccessReturnAccountDto() throws Exception {
        account.setAmount(BigDecimal.valueOf(10_000.00));

        when(repository.getAndLockAccount(any(Long.class))).thenReturn(account);

        AccountDto accountDto = new AccountDto();
        accountDto.setId(idAccount);
        accountDto.setAmount(150_000);

        Account result = service.deposit(accountDto);

        BigDecimal resultDecimal =
                new BigDecimal("160000").setScale(2);

        assertNotNull(result);
        assertEquals(idAccount, result.getId());
        assertEquals(resultDecimal, result.getAmount());
    }

    @Test
    public void whenGetWithdraw_thenSuccessReturnAccountDto() throws Exception {
        account.setAmount(BigDecimal.valueOf(10_000.00));

        when(repository.getAndLockAccount(any(Long.class))).thenReturn(account);

        AccountDto accountDto = new AccountDto();
        accountDto.setId(idAccount);
        accountDto.setAmount(5000.50);

        Account result = service.withdraw(accountDto);

        BigDecimal resultDecimal =
                new BigDecimal("4999.5000").setScale(2);

        assertNotNull(result);
        assertEquals(idAccount, result.getId());
        assertEquals(resultDecimal, result.getAmount());
    }



    @Test
    public void whenGetTransferReqDto_thenSuccess() throws Exception {
        //Init
        Long idAccountTo = 11L;
        Long idAccountFrom = 20L;
        double initToAmount = 10.00;
        double initFromAmount = 50_0000.00;
        double initTransfer = 25_000.00;

        Account to = new Account();
        to.setId(idAccountTo);
        to.setAmount(BigDecimal.valueOf(initToAmount).setScale(2, BigDecimal.ROUND_HALF_UP));

        Account from = new Account();
        from.setId(idAccountFrom);
        from.setAmount(BigDecimal.valueOf(initFromAmount).setScale(2, BigDecimal.ROUND_HALF_UP));

        List<Account> accounts = Arrays.asList(to, from);

        TransferRequestDto transferDto = new TransferRequestDto();
        transferDto.setIdToAccount(idAccountTo);
        transferDto.setIdFromAccount(idAccountFrom);
        transferDto.setAmount(initTransfer);

        //result
        Account resultTo = new Account();
        resultTo.setId(to.getId());
        resultTo.setAmount(to.getAmount().add(BigDecimal.valueOf(transferDto.getAmount()).setScale(2, BigDecimal.ROUND_HALF_UP)));

        Account resultFrom = new Account();
        resultFrom.setId(from.getId());
        resultFrom.setAmount(from.getAmount()
                .subtract(BigDecimal.valueOf(transferDto.getAmount()).setScale(2, BigDecimal.ROUND_HALF_UP)));

        //mock
        when(repository.getAndLockTransferAccounts(any(Long.class), any(Long.class)))
                .thenReturn(accounts);

        ArgumentCaptor<Long> idCaptorTo = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> idCaptorFrom = ArgumentCaptor.forClass(Long.class);

        ArgumentCaptor<Account> captorAccount = ArgumentCaptor.forClass(Account.class);

        //execute
        service.transfer(transferDto);

        //verify
        verify(repository, atLeastOnce()).getAndLockTransferAccounts(idCaptorTo.capture(), idCaptorFrom.capture());
        verify(repository, times(2)).saveAndFlush(captorAccount.capture());

        assertEquals(idAccountTo, idCaptorTo.getValue());
        assertEquals(idAccountFrom, idCaptorFrom.getValue());

        List<Account> allResult = captorAccount.getAllValues();
        Account account1 = allResult.stream().filter(f -> f.getId().equals(resultTo.getId())).findFirst().get();
        Account account2 = allResult.stream().filter(f -> f.getId().equals(resultFrom.getId())).findFirst().get();

        assertNotNull(account1);
        assertNotNull(account2);
        assertEquals(2, allResult.size());

        assertEquals(resultTo.getId(), account1.getId());
        assertEquals(resultTo.getAmount(), account1.getAmount());

        assertEquals(resultFrom.getId(), account2.getId());
        assertEquals(resultFrom.getAmount(), account2.getAmount());
    }

    /*
    @Test
    public void testConcurrencyCalls() throws Exception {
        TestTransaction.start();
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
        TestTransaction.flagForRollback();
        TestTransaction.end();

    }*/
}
