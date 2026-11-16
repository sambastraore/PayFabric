package com.payfabric.ledger.entity;


import com.payfabric.shared.entity.Currency;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "ledger_transactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ledger_transactions_reference",
                        columnNames = "reference"
                ),
                @UniqueConstraint(
                        name = "uk_ledger_transactions_reversal",
                        columnNames = "reverses_transaction_id"
                )
        }
)
public class LedgerTransaction {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false)
    private UUID id;

    @Column(name = "reference", nullable = false, unique = true, length = 100)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private LedgerTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LedgerTransactionStatus status;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "reverses_transaction_id",
            unique = true,
            foreignKey = @ForeignKey(name = "fk_ledger_transactions_reversal")
    )
    private LedgerTransaction reversesTransaction;

    @OneToMany(
            mappedBy = "ledgerTransaction",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<LedgerEntry> entries = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    protected LedgerTransaction() {
    }

    public LedgerTransaction(
            String reference,
            LedgerTransactionType transactionType,
            Currency currency,
            String description
    ) {
        this.reference = reference;
        this.transactionType = transactionType;
        this.currency = currency;
        this.description = description;
        this.status = LedgerTransactionStatus.DRAFT;
    }

    public void addEntry(LedgerEntry entry) {
        entries.add(entry);
        entry.setLedgerTransaction(this);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public UUID getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public LedgerTransactionType getTransactionType() {
        return transactionType;
    }

    public Currency getCurrency() {
        return currency;
    }

    public LedgerTransactionStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public LedgerTransaction getReversesTransaction() {
        return reversesTransaction;
    }

    public List<LedgerEntry> getEntries() {
        return entries;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }
}
