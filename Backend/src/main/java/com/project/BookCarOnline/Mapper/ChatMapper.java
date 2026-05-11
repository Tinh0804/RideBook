package com.project.BookCarOnline.Mapper;

import com.project.BookCarOnline.DTO.Response.ChatMessageResponse;
import com.project.BookCarOnline.Document.ChatMessage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatMapper {
    ChatMessageResponse toChatMessageResponse(ChatMessage chatMessage);
}
