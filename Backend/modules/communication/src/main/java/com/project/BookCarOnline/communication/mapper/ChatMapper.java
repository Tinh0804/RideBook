package com.project.BookCarOnline.communication.mapper;

import com.project.BookCarOnline.communication.dto.response.ChatMessageResponse;
import com.project.BookCarOnline.communication.entity.ChatMessage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatMapper {
    ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage);
}
