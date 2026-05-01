package com.zarema.wallet_app.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Сведения об ошибке")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    @Schema(description = "Текстовое описание ошибки", example = "Insufficient funds")
    private String message;

    @Schema(description = "Внутренний код ошибки", example = "INSUFFICIENT_FUNDS")
    private String code;

    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Время возникновения ошибки", example = "2023-10-27 15:01:23")
    private LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "Детали ошибок валидации полей")
    private Map<String, String> errors;

    public ApiError(String message, String code) {
        this.message = message;
        this.code = code;
        this.timestamp = LocalDateTime.now();
    }

    public ApiError(String message, String code, Map<String, String> errors) {
        this(message, code);
        this.errors = errors;
    }
}


