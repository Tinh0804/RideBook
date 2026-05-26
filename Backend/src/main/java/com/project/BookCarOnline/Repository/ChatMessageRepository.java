package com.project.BookCarOnline.Repository;

import com.project.BookCarOnline.Document.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByBookingIdOrderByTimestampAsc(String bookingId);
}
