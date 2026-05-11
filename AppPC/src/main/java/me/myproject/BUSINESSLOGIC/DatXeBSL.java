package me.myproject.BUSINESSLOGIC;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import me.myproject.Utilities.APIHelper;
import me.myproject.Utilities.AppConfig;

public class DatXeBSL {
    public Map<String, Object> taoDatXe(String customerId, String pickupLocation, String dropoffLocation,
                                        double distance, String vehicleTypeId, String paymentMethod, String promotionId) throws IOException {
        Map<String, Object> request = new HashMap<>();
        request.put("customerId", customerId);
        request.put("pickupLocation", pickupLocation);
        request.put("dropoffLocation", dropoffLocation);
        request.put("distance", distance);
        request.put("vehicleTypeId", vehicleTypeId);
        request.put("paymentMethod", paymentMethod);
        if (promotionId != null && !promotionId.isBlank()) {
            request.put("promotionId", promotionId);
        }
        return APIHelper.postForMap(AppConfig.BASE_URL + "/bookings", request);
    }

    public Map<String, Object> kiemTraThanhToan(String bookingId) throws IOException {
        return APIHelper.getForMap(AppConfig.BASE_URL + "/payments/status/" + bookingId);
    }

    public Map<String, Object> taoThanhToanVNPay(String bookingId, long amount, String orderInfo) throws IOException {
        Map<String, Object> request = new HashMap<>();
        request.put("referenceId", bookingId);
        request.put("amount", amount);
        request.put("orderInfo", orderInfo);
        request.put("locale", "vn");
        return APIHelper.postForMap(AppConfig.BASE_URL + "/payments/vnpay/create", request);
    }

    public Map<String, Object> taoThanhToanMoMo(String bookingId, long amount, String orderInfo) throws IOException {
        Map<String, Object> request = new HashMap<>();
        request.put("referenceId", bookingId);
        request.put("amount", amount);
        request.put("orderInfo", orderInfo);
        return APIHelper.postForMap(AppConfig.BASE_URL + "/payments/momo/create", request);
    }

    public Map<String, Object> getTatCaLoaiXe() throws IOException {
        return APIHelper.getForMap(AppConfig.BASE_URL + "/vehicle-types");
    }

    public Map<String, Object> getActivePromotions() throws IOException {
        return APIHelper.getForMap(AppConfig.BASE_URL + "/promotions/active");
    }

    public Map<String, Object> estimatePrice(String vehicleTypeId, double distance, String promotionCode) throws IOException {
        Map<String, Object> request = new HashMap<>();
        request.put("vehicleTypeId", vehicleTypeId);
        request.put("distance", distance);
        if (promotionCode != null && !promotionCode.isBlank()) {
            request.put("promotionCode", promotionCode);
        }
        return APIHelper.postForMap(AppConfig.BASE_URL + "/bookings/estimate-price", request);
    }
}
