package me.myproject.Utilities;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class GsonUtil {
    
    // Khởi tạo một instance Gson duy nhất (Static Singleton)
    private static final Gson gson = new GsonBuilder()
            // Cấu hình định dạng ngày tháng chuẩn (ISO 8601) khớp với API của bạn
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") 
            // Nếu bạn muốn Gson in ra JSON đẹp (có thụt lề) để debug thì mở dòng dưới
            // .setPrettyPrinting() 
            .create();

    /**
     * Lấy trực tiếp đối tượng Gson dùng chung (nếu cần dùng các hàm phức tạp).
     */
    public static Gson getInstance() {
        return gson;
    }

    /**
     * Chuyển một Object (Java) thành chuỗi JSON.
     */
    public static String toJson(Object src) {
        return gson.toJson(src);
    }

    /**
     * Chuyển chuỗi JSON thành một Object theo Class cụ thể.
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    /**
     * Chuyển chuỗi JSON thành một Object phức tạp (như Map<String, Object>, List<T>).
     * Dùng kết hợp với TypeToken của Gson.
     */
    public static <T> T fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }
    public static <T> T convert(Object fromValue, Class<T> toValueType) {
        if (fromValue == null) return null;
        // Chuyển object nguồn thành JsonTree rồi mới parse sang class đích
        JsonElement jsonElement = gson.toJsonTree(fromValue);
        return gson.fromJson(jsonElement, toValueType);
    }
    
}