package com.project.BookCarOnline.finance.dto;

public record WalletTransactionResult(String transactionId, Double amount, Double newBalance) {
}
