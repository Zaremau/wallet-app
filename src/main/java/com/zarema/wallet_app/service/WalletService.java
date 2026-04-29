package com.zarema.wallet_app.service;

import com.zarema.wallet_app.dto.OperationType;
import com.zarema.wallet_app.dto.WalletRequest;
import com.zarema.wallet_app.exception.InsufficientFundsException;
import com.zarema.wallet_app.exception.WalletNotFoundException;
import com.zarema.wallet_app.model.Wallet;
import com.zarema.wallet_app.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository repository;

    @Transactional
    public void process(WalletRequest request) {

        Wallet wallet = repository.findByIdForUpdate(request.walletId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

        if (request.operationType() == OperationType.DEPOSIT) {
            wallet.setBalance(wallet.getBalance().add(request.amount()));
        } else {
            if (wallet.getBalance().compareTo(request.amount()) < 0) {
                throw new InsufficientFundsException("Insufficient funds for withdrawal");
            }
            wallet.setBalance(wallet.getBalance().subtract(request.amount()));
        }
    }

    public BigDecimal getBalance(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"))
                .getBalance();
    }
}