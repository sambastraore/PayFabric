package com.payfabric.wallet.dto;

import java.util.UUID;

public record WalletResponse(
        UUID id,
        String currency,
        String status,
        Long balance
) {}