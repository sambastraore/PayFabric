package com.payfabric.ledger.repository;

import com.payfabric.ledger.entity.EntryType;
import com.payfabric.ledger.entity.LedgerEntry;
import com.payfabric.ledger.entity.LedgerTransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository
        extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findByLedgerTransactionId(UUID transactionId);

    List<LedgerEntry> findByLedgerAccountId(UUID accountId);

    @Query("""
        SELECT COALESCE(SUM(e.amount), 0)
        FROM LedgerEntry e
        WHERE e.ledgerAccount.id = :accountId
        AND e.entryType = :entryType
        AND e.ledgerTransaction.status = :status
        """)
    Long calculateTotalByAccountAndEntryType(
            @Param("accountId") UUID accountId,
            @Param("entryType") EntryType entryType,
            @Param("status") LedgerTransactionStatus status
    );
}