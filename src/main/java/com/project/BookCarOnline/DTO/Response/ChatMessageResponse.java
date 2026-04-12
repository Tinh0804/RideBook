package com.project.BookCarOnline.DTO.Response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageResponse {
    String id;
    String bookingId;
    String senderId;
    String receiverId;
    String content;
    Date timestamp;
}
