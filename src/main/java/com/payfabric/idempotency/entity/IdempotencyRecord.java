package com.payfabric.idempotency.entity;

import com.payfabric.user.entity.User;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(
        name = "idempotency_records",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_idempotency_user_scope_key",
                        columnNames = {
                                "user_id",
                                "operation_scope",
                                "idempotency_key"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_idempotency_expires_at",
                        columnList = "expires_at"
                )
        }
)
public class IdempotencyRecord {

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_idempotency_user")
    )
    private User user;

    @NotBlank
    @Column(name = "operation_scope", nullable = false, length = 50)
    private String operationScope;

    @NotBlank
    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;

    @NotBlank
    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "resource_type", length = 30)
    private String resourceType;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "resource_id", columnDefinition = "BINARY(16)")
    private UUID resourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private IdempotencyStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    protected IdempotencyRecord() {
    }

    public IdempotencyRecord(
            User user,
            String operationScope,
            String idempotencyKey,
            String requestHash,
            LocalDateTime expiresAt
    ) {
        this.user = user;
        this.operationScope = operationScope;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.status = IdempotencyStatus.IN_PROGRESS;
        this.expiresAt = expiresAt;
    }

    public void complete(String resourceType, UUID resourceId) {
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.status = IdempotencyStatus.COMPLETED;
    }

    public void fail() {
        this.status = IdempotencyStatus.FAILED;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getOperationScope() {
        return operationScope;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public String getResourceType() {
        return resourceType;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public IdempotencyStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}