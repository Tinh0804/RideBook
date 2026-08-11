package com.project.BookCarOnline.communication.dto.response;

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
