package com.project.BookCarOnline.finance.mapper;


import com.project.BookCarOnline.finance.dto.response.WalletResponse;
import com.project.BookCarOnline.finance.dto.response.WalletTransactionResponse;
import com.project.BookCarOnline.finance.entity.Wallet;
import com.project.BookCarOnline.finance.entity.WalletTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WalletMapper {
    WalletResponse toWalletResponse(Wallet wallet);

    List<WalletTransactionResponse> toTransactionHistoryResponse(List<WalletTransaction> transactionHistory);
}
