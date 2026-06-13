package com.project.BookCarOnline.Document;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Entity
@Table(name = "chat_message")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, length = 36)
    String id;

    @Column(name = "booking_id", length = 36)
    String bookingId;

    @Column(name = "sender_id", length = 36)
    String senderId;

    @Column(name = "receiver_id", length = 36)
    String receiverId;

    @Column(name = "content", columnDefinition = "TEXT")
    String content;

    @Column(name = "timestamp")
    Date timestamp;
}
