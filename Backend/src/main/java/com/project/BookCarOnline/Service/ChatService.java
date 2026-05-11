package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.DTO.Request.ChatMessageRequest;
import com.project.BookCarOnline.DTO.Response.ChatMessageResponse;
import com.project.BookCarOnline.Document.ChatMessage;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Mapper.ChatMapper;
import com.project.BookCarOnline.Repository.ChatMessageRepository;
import com.project.BookCarOnline.Utils.SecurityUtils;
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
