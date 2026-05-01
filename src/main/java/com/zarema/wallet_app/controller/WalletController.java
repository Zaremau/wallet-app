package com.zarema.wallet_app.controller;

import com.zarema.wallet_app.dto.WalletRequest;
import com.zarema.wallet_app.exception.ApiError;
import com.zarema.wallet_app.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService service;

    @Operation(summary = "Проведение транзакции", description = "Позволяет пополнить счёт или списать средства")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Операция успешна"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или нехватки средств",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Кошелёк не найден",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Конфликт блокировки(ресурс занят)",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/wallet")
    public ResponseEntity<Void> process(@RequestBody @Valid WalletRequest request) {
        service.process(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получение баланса кошелька", description = "Позволяет получить баланс кошелька")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Операция прошла успешно"),
            @ApiResponse(responseCode = "404", description = "Кошелёк не найден",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/wallets/{id}")
    public ResponseEntity<BigDecimal> balance(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getBalance(id));
    }
}
