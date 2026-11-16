package com.payfabric.ledger.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ledger_transaction_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_ledger_entries_transaction")
    )
    private LedgerTransaction ledgerTransaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ledger_account_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_ledger_entries_account")
    )
    private LedgerAccount ledgerAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 10)
    private EntryType entryType;

    @Positive
    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected LedgerEntry() {
    }

    public LedgerEntry(
            LedgerAccount ledgerAccount,
            EntryType entryType,
            Long amount
    ) {
        this.ledgerAccount = ledgerAccount;
        this.entryType = entryType;
        this.amount = amount;
    }

    void setLedgerTransaction(LedgerTransaction ledgerTransaction) {
        this.ledgerTransaction = ledgerTransaction;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public UUID getId() {
        return id;
    }

    public LedgerTransaction getLedgerTransaction() {
        return ledgerTransaction;
    }

    public LedgerAccount getLedgerAccount() {
        return ledgerAccount;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public Long getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}