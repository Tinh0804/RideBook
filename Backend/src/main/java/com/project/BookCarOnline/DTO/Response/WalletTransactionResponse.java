package com.project.BookCarOnline.DTO.Response;

import com.project.BookCarOnline.Entity.Enum.TransactionStatus;
import com.project.BookCarOnline.Entity.Enum.TransactionType;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class WalletTransactionResponse {
     String transactionId;
     Double amount;
     TransactionType type;
     TransactionStatus status;
     Timestamp createdAt;
}
