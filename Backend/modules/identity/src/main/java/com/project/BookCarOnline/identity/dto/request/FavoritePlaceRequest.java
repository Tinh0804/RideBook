package com.project.BookCarOnline.identity.dto.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FavoritePlaceRequest {
    String label;
    String address;
    Double lat;
    Double lng;
    String icon;
}
