package com.payfabric.ledger.entity;

import com.payfabric.shared.entity.Currency;
import com.payfabric.wallet.entity.Wallet;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(
        name = "ledger_accounts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ledger_accounts_wallet_id",
                        columnNames = "wallet_id"
                ),
                @UniqueConstraint(
                        name = "uk_ledger_accounts_account_code",
                        columnNames = "account_code"
                )
        }
)
public class LedgerAccount {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "wallet_id",
            foreignKey = @ForeignKey(name = "fk_ledger_accounts_wallet")
    )
    private Wallet wallet;

    @Column(name = "account_code", nullable = false, length = 50)
    private String accountCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_role", nullable = false, length = 30)
    private AccountRole accountRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected LedgerAccount() {
    }

    public LedgerAccount(
            Wallet wallet,
            String accountCode,
            AccountType accountType,
            AccountRole accountRole,
            Currency currency
    ) {
        this.wallet = wallet;
        this.accountCode = accountCode;
        this.accountType = accountType;
        this.accountRole = accountRole;
        this.currency = currency;
        this.status = AccountStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public UUID getId() {
        return id;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public AccountRole getAccountRole() {
        return accountRole;
    }

    public Currency getCurrency() {
        return currency;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}