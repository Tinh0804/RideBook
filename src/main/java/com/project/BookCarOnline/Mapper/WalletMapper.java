package com.project.BookCarOnline.Mapper;

import com.project.BookCarOnline.DTO.Response.WalletResponse;
import com.project.BookCarOnline.Entity.Wallet;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WalletMapper {
    WalletResponse toWalletResponse(Wallet wallet);
}
