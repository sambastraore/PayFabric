package com.payfabric.transfer.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TransferResponse(
        UUID id,
        String reference,
        UUID sourceWalletId,
        UUID destinationWalletId,
        Long amount,
        String currency,
        String status,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {}