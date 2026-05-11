package com.project.BookCarOnline.Controller;

import com.project.BookCarOnline.DTO.APIResponse;
import com.project.BookCarOnline.DTO.Request.ChatMessageRequest;
import com.project.BookCarOnline.DTO.Response.ChatMessageResponse;
import com.project.BookCarOnline.Service.ChatService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatController {

    ChatService chatService;

    @PostMapping("/send")
    @ResponseStatus(HttpStatus.CREATED)
    public APIResponse<ChatMessageResponse> sendMessage(@Valid @RequestBody ChatMessageRequest request) {
        ChatMessageResponse response = chatService.sendMessage(request);
        return APIResponse.<ChatMessageResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Gửi tin nhắn thành công")
                .result(response)
                .build();
    }

    @GetMapping("/{bookingId}")
    public APIResponse<List<ChatMessageResponse>> getChatHistory(@PathVariable String bookingId) {
        List<ChatMessageResponse> messages = chatService.getChatHistory(bookingId);
        return APIResponse.<List<ChatMessageResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Lịch sử trò chuyện")
                .result(messages)
                .build();
    }
}
