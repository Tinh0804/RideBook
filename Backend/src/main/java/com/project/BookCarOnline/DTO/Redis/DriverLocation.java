package com.project.BookCarOnline.DTO.Redis;

import java.io.Serializable;

import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class DriverLocation implements Serializable {
    double lat;
    double lng;
    long   timestamp;
}