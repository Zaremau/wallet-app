package com.zarema.wallet_app;

import com.zarema.wallet_app.controller.WalletController;
import com.zarema.wallet_app.exception.WalletNotFoundException;
import com.zarema.wallet_app.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
public class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WalletService service;

    @Test
    void process_WhenLockingFailure_ShouldReturnConflict() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new org.springframework.dao.PessimisticLockingFailureException("Locked"))
                .when(service).process(any());

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
						{
							"walletId": "%s",
							"operationType": "WITHDRAW",
							"amount": 100.00
						}
						""".formatted(id)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_LOCKED"))
                .andExpect(jsonPath("$.message").value("The wallet is currently busy. Please try again later."));
    }

    @Test
    void process_WhenWalletNotFound_ShouldReturnNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        String errorMessage = "Wallet not found";

        doThrow(new WalletNotFoundException(errorMessage))
                .when(service).process(any());

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "walletId": "%s",
                        "operationType": "DEPOSIT",
                        "amount": 100.00
                    }
                    """.formatted(id)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("WALLET_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.timestamp").exists());
    }

}
