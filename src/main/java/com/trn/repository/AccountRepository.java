package com.trn.repository;

import com.trn.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            value = "SELECT t FROM Account t WHERE t.id = ?1 or t.id= ?2")
    List<Account> getAndLockTransferAccounts(Long acc1, Long acc2);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT t FROM Account t WHERE t.id = ?1")
    Account getAndLockAccount(Long acc);
}
