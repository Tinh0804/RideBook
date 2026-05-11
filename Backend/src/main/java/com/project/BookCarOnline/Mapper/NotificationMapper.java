package com.project.BookCarOnline.Mapper;

import com.project.BookCarOnline.DTO.Response.NotificationResponse;
import com.project.BookCarOnline.Entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "bookingId", source = "bookingNo.bookingId")
    @Mapping(target = "isRead", source = "read")
    NotificationResponse toNotificationResponse(Notification notification);
}
