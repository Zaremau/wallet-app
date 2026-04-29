package com.zarema.wallet_app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletRequest(
        @NotNull UUID walletId,
        @NotNull OperationType operationType,
        @NotNull @Positive BigDecimal amount
) {}