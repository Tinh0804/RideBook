package com.project.BookCarOnline.DTO.Request;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageRequest {
    @NotBlank(message = "ID chuyến đi không được để trống")
    String bookingId;

    @NotBlank(message = "Người nhận không được để trống")
    String receiverId;

    @NotBlank(message = "Nội dung không được để trống")
    String content;
}
