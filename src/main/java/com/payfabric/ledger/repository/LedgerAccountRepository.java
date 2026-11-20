package com.payfabric.ledger.repository;

import com.payfabric.ledger.entity.LedgerAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface LedgerAccountRepository
        extends JpaRepository<LedgerAccount, UUID> {

    Optional<LedgerAccount> findByWalletId(UUID walletId);

    Optional<LedgerAccount> findByAccountCode(String accountCode);

    boolean existsByAccountCode(String accountCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT a
        FROM LedgerAccount a
        WHERE a.wallet.id = :walletId
        """)
    Optional<LedgerAccount> findByWalletIdForUpdate(
            @Param("walletId") UUID walletId
    );
}