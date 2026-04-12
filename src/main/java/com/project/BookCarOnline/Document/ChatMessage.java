package com.project.BookCarOnline.Document;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Document(collection = "chat_messages")
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessage {
    @Id
    String id;

    @Field("booking_id")
    String bookingId;

    @Field("sender_id")
    String senderId;

    @Field("receiver_id")
    String receiverId;

    @Field("content")
    String content;

    @Field("timestamp")
    Date timestamp;
}
