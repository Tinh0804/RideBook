package com.project.BookCarOnline.identity.dto.summary;

public record AccountSummary(
        String accountId,
        String userName,
        String roleName,
        String fcmToken,
        Boolean accountStatus) {
}
