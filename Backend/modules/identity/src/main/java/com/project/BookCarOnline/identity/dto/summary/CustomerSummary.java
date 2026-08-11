package com.project.BookCarOnline.identity.dto.summary;

public record CustomerSummary(
        String customerId,
        String customerName,
        String phone,
        AccountSummary account) {
}
