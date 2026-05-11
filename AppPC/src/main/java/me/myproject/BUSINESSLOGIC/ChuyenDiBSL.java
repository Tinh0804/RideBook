package me.myproject.BUSINESSLOGIC;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.myproject.MODEL.DatXe;
import me.myproject.Utilities.APIHelper;
import me.myproject.Utilities.Enum.BookingStatus;

public class ChuyenDiBSL {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static List<DatXe> chuyenDiList = new ArrayList<>(); 

    public ChuyenDiBSL() {
    }

    public static String formatDate(Timestamp timestamp) {
        if (timestamp == null) 
            return "";
        return dateFormatter.format(new Date(timestamp.getTime()));
    }

    public List<DatXe> getChuyenDiTuURL(String url) throws Exception {
        Map<String, Object> response;
        try {
            response = APIHelper.getForMap(url);
        } catch (Exception e) {
            throw new Exception("Lỗi khi gọi API: " + e.getMessage());
        }
        System.out.println("Phản hồi API: " + response);
        Number status = (Number) response.get("status");
        if (status == null || status.intValue() < 200 || status.intValue() >= 300) {
            throw new Exception((String) response.getOrDefault("message", "Lỗi khi lấy lịch sử chuyến đi!"));
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> chuyenDiData = (List<Map<String, Object>>) response.get("result");
        List<DatXe> allChuyenDiList  = new ArrayList<>();
        if (chuyenDiData == null) {
            return allChuyenDiList;
        }
        
        for (Map<String, Object> item : chuyenDiData) {
            DatXe chuyenDi = new DatXe();
            chuyenDi.setBookingId((String) item.get("bookingId"));
            chuyenDi.setDriverId((String) item.get("driverId"));
            chuyenDi.setCustomerId((String) item.get("customerId"));
            chuyenDi.setBookingStatus(BookingStatus.valueOf((String) item.get("bookingStatus")));
            chuyenDi.setTotalPrice(item.get("totalPrice") != null ? ((Number) item.get("totalPrice")).doubleValue() : 0.0);
            chuyenDi.setPickupLocation((String) item.get("pickupLocation"));
            chuyenDi.setDropoffLocation((String) item.get("dropoffLocation"));
            chuyenDi.setDistance(item.get("distance") != null ? ((Number) item.get("distance")).doubleValue() : 0.0);
            chuyenDi.setPaymentMethod((String) item.get("paymentMethod"));
            chuyenDi.setPromotionCode((String) item.get("promotionCode"));
            Object bookingTime = item.get("bookingTime");
            if (bookingTime instanceof String) {
                chuyenDi.setBookingTime(parseTimestamp((String) bookingTime));
            }
            Object pickupTime = item.get("pickupTime");
            if (pickupTime instanceof String) {
                chuyenDi.setPickupTime(parseTimestamp((String) pickupTime));
            }
            Object arrivalTime = item.get("arrivalTime");
            if (arrivalTime instanceof String) {
                chuyenDi.setArrivalTime(parseTimestamp((String) arrivalTime));
            }
            allChuyenDiList.add(chuyenDi);
        }
        return allChuyenDiList;
    }

    public List<DatXe> getChuyenDi() throws Exception {
        return getChuyenDiTuURL(me.myproject.Utilities.AppConfig.BASE_URL + "/bookings");
    }

    private Timestamp parseTimestamp(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Timestamp.valueOf(value.replace("T", " ").replace("Z", ""));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
    
    public List<DatXe> getLichSuChuyenDiTheoKH(String maKh) throws Exception{
        List<DatXe> allChuyenDiList = getChuyenDiTuURL(me.myproject.Utilities.AppConfig.BASE_URL + "/bookings/customer/" + maKh);
        chuyenDiList.clear();
        for (DatXe chuyenDi : allChuyenDiList) 
            if (chuyenDi.getBookingStatus() != BookingStatus.PENDING) 
                chuyenDiList.add(chuyenDi);
    	return chuyenDiList;
    }

    public List<DatXe> getLichSuChuyenDiTheoTX(String driverId) throws Exception{
        List<DatXe> allChuyenDiList = getChuyenDiTuURL(me.myproject.Utilities.AppConfig.BASE_URL + "/bookings/driver/" + driverId);
        chuyenDiList.clear();
        for (DatXe chuyenDi : allChuyenDiList) 
            if (chuyenDi.getBookingStatus() != BookingStatus.PENDING) 
                chuyenDiList.add(chuyenDi);
    	return chuyenDiList;
    }

    public List<DatXe> getChuyenDiDangChoTheoKH(String maKh) throws Exception{
        List<DatXe> allChuyenDiList = getChuyenDiTuURL(me.myproject.Utilities.AppConfig.BASE_URL + "/bookings/customer/" + maKh);
        chuyenDiList.clear();
        for (DatXe chuyenDi : allChuyenDiList) 
            if (chuyenDi.getBookingStatus() == BookingStatus.PENDING) 
                chuyenDiList.add(chuyenDi);
    	return chuyenDiList;
    }
    public void HuyChuyenDi(String maDX, String lyDoHuy) throws Exception {
        Map<String, Object> response;
        try {
            response = APIHelper.deleteForMap(me.myproject.Utilities.AppConfig.BASE_URL + "/bookings/" + maDX);
            if (response == null || response.isEmpty()) {
                throw new Exception("Phản hồi API rỗng hoặc không hợp lệ");
            }
        } catch (Exception e) {
            throw new Exception("Lỗi khi gọi API hủy chuyến: " + e.getMessage());
        }
        System.out.println("Phản hồi API hủy chuyến: " + response);
        Number status = (Number) response.get("status");
        if (status == null || status.intValue() < 200 || status.intValue() >= 300) {
            throw new Exception((String) response.getOrDefault("message", "Lỗi khi hủy chuyến đi!"));
        }
    }
    
    public List<DatXe> locChuyenDi(String trangThai, Timestamp tuNgay, Timestamp denNgay) throws Exception{
    	List<DatXe> chuyenXeLoc = new ArrayList<>();
        for (DatXe chuyenDi : chuyenDiList) {
            boolean matches = true;
           if (trangThai != null && !trangThai.isEmpty() && !chuyenDi.getBookingStatus().toString().equals(trangThai)) 
               matches = false;
            if (matches && tuNgay != null && denNgay != null) {
                Timestamp thoiGianDat = chuyenDi.getBookingTime();
                if (thoiGianDat.before(tuNgay) || thoiGianDat.after(denNgay)) 
                    matches = false;
            }
            if (matches) 
                chuyenXeLoc.add(chuyenDi);
        }
    	return chuyenXeLoc;
    }

    public boolean danhGiaChuyenDi(String currentSelectedMaChuyen, int currentSelectedRating,String feedback) {
        try {
            Map<String, Object> requestBody = Map.of(
                "rating", currentSelectedRating, 
                "feedback", feedback,
                "bookingId", currentSelectedMaChuyen);
            Map<String, Object> response = APIHelper.postForMap(me.myproject.Utilities.AppConfig.BASE_URL + "/ratings", requestBody);
            Number status = (Number) response.get("status");
            if (status != null && status.intValue() >= 200 && status.intValue() < 300) {
                return true;
            } else {
                System.err.println("Lỗi khi đánh giá chuyến đi: " + response.get("message"));
                return false;
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi API đánh giá chuyến đi: " + e.getMessage());
            return false;
        }
    }

    // Bổ sung vào DriverChuyenDiBSL.java
 

    
}
