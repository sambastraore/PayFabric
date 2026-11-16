package com.payfabric.merchant.entity;

import com.payfabric.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "merchants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_merchants_user_id",
                        columnNames = "user_id"
                ),
                @UniqueConstraint(
                        name = "uk_merchants_merchant_code",
                        columnNames = "merchant_code"
                )
        }
)
public class Merchant {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "id", columnDefinition = "BINARY(16)", updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_merchants_user")
    )
    private User user;

    @NotBlank
    @Size(max = 150)
    @Column(name = "business_name", nullable = false, length = 150)
    private String businessName;

    @NotBlank
    @Size(max = 50)
    @Column(
            name = "merchant_code",
            nullable = false,
            length = 50
    )
    private String merchantCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MerchantStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Merchant() {
    }

    public Merchant(User user, String businessName, String merchantCode) {
        this.user = user;
        this.businessName = businessName;
        this.merchantCode = merchantCode;
        this.status = MerchantStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;

        if (this.status == null) {
            this.status = MerchantStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(java.time.ZoneOffset.UTC);
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getMerchantCode() {
        return merchantCode;
    }

    public MerchantStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}