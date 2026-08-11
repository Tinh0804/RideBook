package com.project.BookCarOnline.communication.mapper;

import com.project.BookCarOnline.communication.dto.response.NotificationResponse;
import com.project.BookCarOnline.communication.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "isRead", source = "read")
    NotificationResponse toNotificationResponse(Notification notification);
}
