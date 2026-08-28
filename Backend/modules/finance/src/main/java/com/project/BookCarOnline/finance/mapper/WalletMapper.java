package com.project.BookCarOnline.finance.mapper;

import com.project.BookCarOnline.finance.dto.response.WalletResponse;
import com.project.BookCarOnline.finance.dto.response.WalletTransactionResponse;
import com.project.BookCarOnline.finance.entity.Wallet;
import com.project.BookCarOnline.finance.entity.WalletTransaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WalletMapper {

    public WalletResponse toWalletResponse(Wallet wallet) {
        if (wallet == null) {
            return null;
        }
        return new WalletResponse(
                wallet.getWalletId(),
                wallet.getBalance() != null ? wallet.getBalance() : 0.0,
                wallet.getStatus()
        );
    }

    public List<WalletTransactionResponse> toTransactionHistoryResponse(List<WalletTransaction> transactionHistory) {
        if (transactionHistory == null) {
            return List.of();
        }
        return transactionHistory.stream()
                .map(this::walletTransactionToWalletTransactionResponse)
                .toList();
    }

    public WalletTransactionResponse walletTransactionToWalletTransactionResponse(WalletTransaction txn) {
        if (txn == null) {
            return null;
        }
        return WalletTransactionResponse.builder()
                .transactionId(txn.getTransactionId())
                .amount(txn.getAmount())
                .type(txn.getType())
                .status(txn.getStatus())
                .createdAt(txn.getCreatedAt())
                .build();
    }
}
