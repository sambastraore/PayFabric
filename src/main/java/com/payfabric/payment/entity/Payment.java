package com.payfabric.payment.entity;

import com.payfabric.ledger.entity.LedgerTransaction;
import com.payfabric.merchant.entity.Merchant;
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
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payments_reference",
                        columnNames = "reference"
                ),
                @UniqueConstraint(
                        name = "uk_payments_ledger_transaction",
                        columnNames = "ledger_transaction_id"
                )
        }
)
public class Payment {

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
            name = "customer_wallet_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_payments_customer_wallet")
    )
    private Wallet customerWallet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "merchant_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_payments_merchant")
    )
    private Merchant merchant;

    @Positive
    @Column(name = "amount", nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "ledger_transaction_id",
            unique = true,
            foreignKey = @ForeignKey(name = "fk_payments_ledger_transaction")
    )
    private LedgerTransaction ledgerTransaction;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    protected Payment() {
    }

    public Payment(
            String reference,
            Wallet customerWallet,
            Merchant merchant,
            Long amount,
            Currency currency
    ) {
        this.reference = reference;
        this.customerWallet = customerWallet;
        this.merchant = merchant;
        this.amount = amount;
        this.currency = currency;
        this.status = PaymentStatus.PENDING;
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

    public Wallet getCustomerWallet() {
        return customerWallet;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public Long getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public PaymentStatus getStatus() {
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
}