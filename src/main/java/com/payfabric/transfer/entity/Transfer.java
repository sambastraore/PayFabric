package com.payfabric.transfer.entity;

import com.payfabric.ledger.entity.LedgerTransaction;
import com.payfabric.shared.entity.Currency;
import com.payfabric.wallet.entity.Wallet;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(
        name = "transfers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transfers_reference",
                        columnNames = "reference"
                ),
                @UniqueConstraint(
                        name = "uk_transfers_ledger_transaction",
                        columnNames = "ledger_transaction_id"
                )
        }
)
public class Transfer {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(
            name = "id",
            columnDefinition = "BINARY(16)",
            updatable = false
    )
    private UUID id;

    @Column(name = "reference", nullable = false, length = 100)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "source_wallet_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_transfers_source_wallet")
    )
    private Wallet sourceWallet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "destination_wallet_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_transfers_destination_wallet")
    )
    private Wallet destinationWallet;

    @Positive
    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransferStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "ledger_transaction_id",
            unique = true,
            foreignKey = @ForeignKey(name = "fk_transfers_ledger_transaction")
    )
    private LedgerTransaction ledgerTransaction;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    protected Transfer() {
    }

    public Transfer(
            String reference,
            Wallet sourceWallet,
            Wallet destinationWallet,
            Long amount,
            Currency currency
    ) {
        this.reference = reference;
        this.sourceWallet = sourceWallet;
        this.destinationWallet = destinationWallet;
        this.amount = amount;
        this.currency = currency;
        this.status = TransferStatus.PENDING;
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

    public Wallet getSourceWallet() {
        return sourceWallet;
    }

    public Wallet getDestinationWallet() {
        return destinationWallet;
    }

    public Long getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public LedgerTransaction getLedgerTransaction() {
        return ledgerTransaction;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void markAsProcessing() {
        if (this.status != TransferStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending transfers can be processed"
            );
        }

        this.status = TransferStatus.PROCESSING;
    }

    public void complete(LedgerTransaction ledgerTransaction) {
        if (this.status != TransferStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Only processing transfers can be completed"
            );
        }

        if (ledgerTransaction == null) {
            throw new IllegalArgumentException(
                    "Ledger transaction cannot be null"
            );
        }

        this.ledgerTransaction = ledgerTransaction;
        this.status = TransferStatus.SUCCEEDED;
        this.completedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}