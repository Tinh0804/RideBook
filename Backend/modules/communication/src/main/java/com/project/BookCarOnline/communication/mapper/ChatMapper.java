package com.project.BookCarOnline.communication.mapper;

import com.project.BookCarOnline.communication.dto.response.ChatMessageResponse;
import com.project.BookCarOnline.communication.entity.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper {

    /** Maps a persisted chat message to the API response contract. */
    public ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage) {
        if (chatMessage == null) {
            return null;
        }
        return ChatMessageResponse.builder()
                .id(chatMessage.getId())
                .bookingId(chatMessage.getBookingId())
                .senderId(chatMessage.getSenderId())
                .receiverId(chatMessage.getReceiverId())
                .content(chatMessage.getContent())
                .timestamp(chatMessage.getTimestamp())
                .build();
    }
}
