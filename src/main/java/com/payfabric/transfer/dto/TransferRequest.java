package com.payfabric.transfer.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record TransferRequest(

        @NotNull
        UUID destinationWalletId,

        @NotNull
        @Positive
        Long amount

) {}