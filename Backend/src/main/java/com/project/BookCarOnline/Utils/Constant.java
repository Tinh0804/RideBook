package com.project.BookCarOnline.Utils;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class Constant {
    // === Trọng số tính điểm ưu tiên tài xế ===
    public final double W_DISTANCE = 0.4;
    public final double W_RATING   = 0.2;
    public final double W_IDLE     = 0.2;
    public final double W_REJECT   = 0.15;
    public final double W_IGNORE   = 0.05;

    // === Ngưỡng tính điểm ===
    public final double MAX_DISTANCE_KM = 5.0;
    public final double MAX_IDLE_MIN    = 30.0;

    // === Dispatcher settings ===
    /** Thời gian chờ tài xế phản hồi (giây) trước khi chuyển sang tài xế tiếp theo */
    public final int    DISPATCH_TIMEOUT_SECONDS = 20;

    /** Bán kính tìm tài xế gần khách (km) */
    public final double SEARCH_RADIUS_KM = 3.0;
}
