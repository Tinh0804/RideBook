package com.project.BookCarOnline.Service;

import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.GeocodingResult;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleMapService {
    @Value("${google.maps.api-key}")
    private String apiKey;

    public DistanceMatrixElement getDistanceInfo(String origin, String destination) {
        GeoApiContext context = new GeoApiContext.Builder().apiKey(apiKey).build();
        try {
            DistanceMatrix matrix = DistanceMatrixApi.getDistanceMatrix(context,
                    new String[]{origin}, new String[]{destination}).await();
            return matrix.rows[0].elements[0];
        } catch (Exception e) {
            throw new RuntimeException("Lỗi gọi Google Maps API");
        }
    }

    public GeocodingResult geocode(@NotBlank(message = "Điểm đón không được để trống") String pickupLocation) {
        GeoApiContext context = new GeoApiContext.Builder().apiKey(apiKey).build();
        try {
            GeocodingResult[] results = com.google.maps.GeocodingApi.geocode(context, pickupLocation).await();
            if (results.length > 0) {
                return results[0];
            } else {
                throw new RuntimeException("Không tìm thấy địa chỉ");
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi gọi Google Maps API");
        }
    }
}