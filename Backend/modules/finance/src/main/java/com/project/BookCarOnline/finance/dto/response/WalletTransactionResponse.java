package com.project.BookCarOnline.finance.dto.response;

import com.project.BookCarOnline.finance.entity.enums.TransactionStatus;
import com.project.BookCarOnline.finance.entity.enums.TransactionType;

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
