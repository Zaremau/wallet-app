package com.zarema.wallet_app;

import com.zarema.wallet_app.dto.OperationType;
import com.zarema.wallet_app.dto.WalletRequest;
import com.zarema.wallet_app.exception.InsufficientFundsException;
import com.zarema.wallet_app.exception.WalletNotFoundException;
import com.zarema.wallet_app.model.Wallet;
import com.zarema.wallet_app.repository.WalletRepository;
import com.zarema.wallet_app.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {

    @Mock
    private WalletRepository repository;

    @InjectMocks
    private WalletService service;

    private UUID id;
    private Wallet wallet;

    @BeforeEach
    void setUp(){
        id = UUID.randomUUID();
        wallet = new Wallet();
        wallet.setId(id);
        wallet.setBalance(new BigDecimal("100.00"));

    }

    @Test
    void process_Deposit_Success(){
        WalletRequest request = new WalletRequest(id, OperationType.DEPOSIT, new BigDecimal("50.00"));
        when(repository.findByIdForUpdate(id)).thenReturn(Optional.of(wallet));

        service.process(request);

        assertEquals(new BigDecimal("150.00"), wallet.getBalance());
    }

    @Test
    void process_Withdraw_Success(){
        WalletRequest request = new WalletRequest(id, OperationType.WITHDRAW, new BigDecimal("30.00"));
        when(repository.findByIdForUpdate(id)).thenReturn(Optional.of(wallet));

        service.process(request);

        assertEquals(new BigDecimal("70.00"), wallet.getBalance());
    }

    @Test
    void process_Withdraw_Insufficient_Funds_Should_Throw_Exception(){
        WalletRequest request = new WalletRequest(id, OperationType.WITHDRAW, new BigDecimal("200.00"));
        when(repository.findByIdForUpdate(id)).thenReturn(Optional.of(wallet));

        assertThrows(InsufficientFundsException.class, () -> service.process(request));
        assertEquals(new BigDecimal("100.00"), wallet.getBalance());
    }

    @Test
    void process_WalletNotFound_ShouldThrowException(){
        WalletRequest request = new WalletRequest(id, OperationType.WITHDRAW, new BigDecimal("20.00"));
        when(repository.findByIdForUpdate(id)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> service.process(request));
    }

    @Test
    void getBalance_Success() {
        when(repository.findById(id)).thenReturn(Optional.of(wallet));

        assertEquals(new BigDecimal("100.00"), service.getBalance(id));
        verify(repository, times(1)).findById(id);
    }

    @Test
    void getBalance_WalletNotFound_ShouldThrowException() {
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(WalletNotFoundException.class, () -> service.getBalance(id));
    }

}

