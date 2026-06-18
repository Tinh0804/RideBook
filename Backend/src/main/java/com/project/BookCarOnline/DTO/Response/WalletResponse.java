package com.project.BookCarOnline.DTO.Response;

import com.project.BookCarOnline.Entity.Enum.WalletStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class WalletResponse {
    String walletId;
    double balance;
    WalletStatus status;
}
