package com.project.BookCarOnline.communication.repository;

import com.project.BookCarOnline.communication.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    List<ChatMessage> findByBookingIdOrderByTimestampAsc(String bookingId);
}
