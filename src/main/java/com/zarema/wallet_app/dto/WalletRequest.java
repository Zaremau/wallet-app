package com.zarema.wallet_app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Запрос на проведение операции с кошельком")
public record WalletRequest(
        @Schema(description = "UUID кошелька", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull UUID walletId,

        @Schema(description = "Тип операции", example = "DEPOSIT", allowableValues = {"DEPOSIT", "WITHDRAW"})
        @NotNull OperationType operationType,

        @Schema(description = "Сумма операции (должна быть положительной)", example = "1000.50")
        @NotNull @Positive BigDecimal amount
) {}