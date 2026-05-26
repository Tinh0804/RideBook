package com.project.BookCarOnline.Mapper;


import com.project.BookCarOnline.DTO.Response.WalletResponse;
import com.project.BookCarOnline.DTO.Response.WalletTransactionResponse;
import com.project.BookCarOnline.Entity.Wallet;
import com.project.BookCarOnline.Entity.WalletTransaction;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WalletMapper {
    WalletResponse toWalletResponse(Wallet wallet);

    List<WalletTransactionResponse> toTransactionHistoryResponse(List<WalletTransaction> transactionHistory);
}
