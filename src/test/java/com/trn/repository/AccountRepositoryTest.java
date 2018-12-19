package com.trn.repository;

import com.trn.entity.Account;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@DataJpaTest
@RunWith(SpringRunner.class)
public class AccountRepositoryTest {

    @Autowired
    private AccountRepository repository;

    @Test
    public void checkCount() throws Exception {
        Assert.assertEquals(2, repository.count());
    }
}
