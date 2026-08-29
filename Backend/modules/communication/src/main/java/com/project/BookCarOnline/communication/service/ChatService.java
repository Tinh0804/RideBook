package com.project.BookCarOnline.communication.service;

import com.project.BookCarOnline.communication.dto.request.ChatMessageRequest;
import com.project.BookCarOnline.communication.dto.response.ChatMessageResponse;
import com.project.BookCarOnline.communication.entity.ChatMessage;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import com.project.BookCarOnline.communication.mapper.ChatMapper;
import com.project.BookCarOnline.communication.repository.ChatMessageRepository;
import com.project.BookCarOnline.shared.security.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatService {
    ChatMessageRepository chatMessageRepository;
    ChatMapper chatMapper;
    SimpMessagingTemplate messagingTemplate;

    public ChatMessageResponse sendMessage(ChatMessageRequest request) {
        String senderId = SecurityUtils.getCurrentAccountId()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTACATED));

        ChatMessage message = ChatMessage.builder()
                .bookingId(request.getBookingId())
                .senderId(senderId)
                .receiverId(request.getReceiverId())
                .content(request.getContent())
                .timestamp(new Date())
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        ChatMessageResponse response = chatMapper.toChatMessageResponse(saved);

        // Broadcast to the specific booking / chat room
        messagingTemplate.convertAndSend("/topic/chat/" + request.getBookingId(), response);

        return response;
    }

    public List<ChatMessageResponse> getChatHistory(String bookingId) {
        List<ChatMessage> messages = chatMessageRepository.findByBookingIdOrderByTimestampAsc(bookingId);
        return messages.stream().map(chatMapper::toChatMessageResponse).collect(Collectors.toList());
    }
}
