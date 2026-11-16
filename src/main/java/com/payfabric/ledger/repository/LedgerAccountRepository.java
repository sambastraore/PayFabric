package com.payfabric.ledger.repository;

import com.payfabric.ledger.entity.LedgerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LedgerAccountRepository
        extends JpaRepository<LedgerAccount, UUID> {

    Optional<LedgerAccount> findByWalletId(UUID walletId);

    Optional<LedgerAccount> findByAccountCode(String accountCode);

    boolean existsByAccountCode(String accountCode);
}