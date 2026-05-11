package me.myproject.BUSINESSLOGIC;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.myproject.MODEL.DatXe;
import me.myproject.Utilities.Enum.BookingStatus;
public class QuanLyChuyenDiBSL {
    private static ChuyenDiBSL chuyenDiBSL;
    private List<DatXe> chuyenDiList;

    public QuanLyChuyenDiBSL() throws Exception {
        chuyenDiBSL = new ChuyenDiBSL();
        chuyenDiList = new ArrayList<>();
    }

    public void fetchChuyenDi() throws Exception {
        chuyenDiList = chuyenDiBSL.getChuyenDi();
    }

    public int calculateTotalTrips() {
        return chuyenDiList.size();
    }

    public int calculateCompletedTrips() {
        int completed = 0;
        for (DatXe chuyenDi : chuyenDiList) 
            if (BookingStatus.COMPLETED.equals(chuyenDi.getBookingStatus())) 
                completed++;
        return completed;
    }

    public int calculateCanceledTrips() {
        int canceled = 0;
        for (DatXe chuyenDi : chuyenDiList) 
            if (BookingStatus.CANCELLED.equals(chuyenDi.getBookingStatus())) 
                canceled++;
        return canceled;
    }

    public double calculateTotalRevenue() {
        double totalRevenue = 0.0;
        for (DatXe chuyenDi : chuyenDiList) 
            totalRevenue += chuyenDi.getTotalPrice();
        return totalRevenue;
    }

    public Object[][] getTableData() {
        if (chuyenDiList == null) {
            return new Object[0][5]; // Trả về mảng rỗng nếu danh sách null
        }
        Object[][] data = new Object[chuyenDiList.size()][5]; // 5 cột
        for (int i = 0; i < chuyenDiList.size(); i++) {
            DatXe chuyenDi = chuyenDiList.get(i);
            data[i][0] = chuyenDi.getBookingId(); // ID
            data[i][1] = ChuyenDiBSL.formatDate(chuyenDi.getBookingTime()); // Ngày
            data[i][2] = chuyenDi.getCustomerId(); // Khách hàng
            data[i][3] = chuyenDi.getDriverId(); // Tài xế (giả sử có phương thức này)
            data[i][4] = chuyenDi.getBookingStatus(); // Trạng thái
        }
        return data;
    }

    public DatXe getChuyenDi(String maDX){
        for(DatXe x:chuyenDiList)
            if(x.getBookingId().equals(maDX))
                return x;
        return null;
    }

    public List<DatXe> getChuyenDiList() {
        return chuyenDiList;
    }

    public List<DatXe> TimKiemChuyen(String searchText) {
        List<DatXe> chuyenDi = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (DatXe x : chuyenDiList) {
            boolean match = true; 
            if (!searchText.isEmpty()) {
                match = false; 
                if (x.getBookingId() != null && x.getBookingId().toLowerCase().contains(searchText)) 
                    match = true;
                else if (x.getCustomerId() != null && x.getCustomerId().toLowerCase().contains(searchText))
                    match = true;
                else if (x.getDriverId() != null && x.getDriverId().toLowerCase().contains(searchText))
                    match = true;
                else if (x.getPickupLocation() != null && x.getPickupLocation().toLowerCase().contains(searchText)) 
                    match = true;
                else if (x.getDropoffLocation() != null && x.getDropoffLocation().toLowerCase().contains(searchText)) 
                    match = true;
            }
            if (match) {
                String formattedDate = x.getBookingTime() != null ? 
                    dateFormat.format(x.getBookingTime()) : "N/A";
                chuyenDi.add(getChuyenDi(x.getBookingId()));
            }
        }
        return chuyenDi;
    }

    public List<DatXe> LocChuyenDi(String trangThai, Timestamp tuNgay, Timestamp denNgay) throws Exception{
        System.out.println(trangThai);
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
    
    // Phương thức tính số chuyến đi theo ngày
    public Map<Date, Integer> getTripCountByDay(Timestamp start, Timestamp end) {
        Map<Date, Integer> tripCountByDay = new HashMap<>();
        LocalDate startDate = new java.sql.Date(start.getTime()).toLocalDate();
        LocalDate endDate = new java.sql.Date(end.getTime()).toLocalDate();

        for (DatXe chuyenDi : chuyenDiList) {
            Timestamp thoiGianDat = chuyenDi.getBookingTime();
            if (thoiGianDat != null) {
                LocalDate tripDate = new java.sql.Date(thoiGianDat.getTime()).toLocalDate();
                if (!tripDate.isBefore(startDate) && !tripDate.isAfter(endDate)) {
                    Date date = new java.sql.Date(thoiGianDat.getTime());
                    tripCountByDay.put(date, tripCountByDay.getOrDefault(date, 0) + 1);
                }
            }
        }

        return tripCountByDay;
    }
    // Phương thức tính số chuyến đi theo tuần
    public Map<String, Integer> getTripCountByWeek(Timestamp start, Timestamp end) {
        Map<String, Integer> tripCountByWeek = new HashMap<>();
        LocalDate startDate = new java.sql.Date(start.getTime()).toLocalDate();
        LocalDate endDate = new java.sql.Date(end.getTime()).toLocalDate();

        for (DatXe chuyenDi : chuyenDiList) {
            Timestamp ngayKhoiHanh = chuyenDi.getBookingTime();
            if (ngayKhoiHanh != null) {
                LocalDate tripDate = new java.sql.Date(ngayKhoiHanh.getTime()).toLocalDate();
                if (!tripDate.isBefore(startDate) && !tripDate.isAfter(endDate)) {
                    int weekNumber = tripDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                    int year = tripDate.getYear();
                    String weekKey = "Tuần " + weekNumber + " - " + year;
                    tripCountByWeek.put(weekKey, tripCountByWeek.getOrDefault(weekKey, 0) + 1);
                }
            }
        }

        return tripCountByWeek;
    }
    // Phương thức tính số chuyến đi theo tháng
    public Map<String, Integer> getTripCountByMonth(Timestamp start, Timestamp end) {
        Map<String, Integer> tripCountByMonth = new HashMap<>();
        LocalDate startDate = new java.sql.Date(start.getTime()).toLocalDate();
        LocalDate endDate = new java.sql.Date(end.getTime()).toLocalDate();

        for (DatXe chuyenDi : chuyenDiList) {
            Timestamp ngayKhoiHanh = chuyenDi.getBookingTime();
            if (ngayKhoiHanh != null) {
                LocalDate tripDate = new java.sql.Date(ngayKhoiHanh.getTime()).toLocalDate();
                if (!tripDate.isBefore(startDate) && !tripDate.isAfter(endDate)) {
                    String monthKey = tripDate.getMonthValue() + "/" + tripDate.getYear();
                    tripCountByMonth.put(monthKey, tripCountByMonth.getOrDefault(monthKey, 0) + 1);
                }
            }
        }

        return tripCountByMonth;
    }
    // Phương thức tính số chuyến đi theo năm
    public Map<String, Integer> getTripCountByYear(Timestamp start, Timestamp end) {
        Map<String, Integer> tripCountByYear = new HashMap<>();
        LocalDate startDate = new java.sql.Date(start.getTime()).toLocalDate();
        LocalDate endDate = new java.sql.Date(end.getTime()).toLocalDate();

        for (DatXe chuyenDi : chuyenDiList) {
            Timestamp ngayKhoiHanh = chuyenDi.getBookingTime();
            if (ngayKhoiHanh != null) {
                LocalDate tripDate = new java.sql.Date(ngayKhoiHanh.getTime()).toLocalDate();
                if (!tripDate.isBefore(startDate) && !tripDate.isAfter(endDate)) {
                    String yearKey = String.valueOf(tripDate.getYear());
                    tripCountByYear.put(yearKey, tripCountByYear.getOrDefault(yearKey, 0) + 1);
                }
            }
        }

        return tripCountByYear;
    }

    // Lấy doanh thu theo ngày
    public Map<java.util.Date, Double> getRevenueByDay(Timestamp startDate, Timestamp endDate) {
        Map<java.util.Date, Double> revenueByDay = new HashMap<>();
        // Chuyển đổi startDate và endDate thành Date để dễ xử lý
        Calendar calStart = Calendar.getInstance();
        calStart.setTime(startDate);
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(endDate);
        calEnd.set(Calendar.HOUR_OF_DAY, 23);
        calEnd.set(Calendar.MINUTE, 59);
        calEnd.set(Calendar.SECOND, 59);
        calEnd.set(Calendar.MILLISECOND, 999);

        // Khởi tạo doanh thu cho từng ngày trong khoảng thời gian
        Calendar cal = (Calendar) calStart.clone();
        while (!cal.after(calEnd)) {
            revenueByDay.put(cal.getTime(), 0.0);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Tính tổng doanh thu theo ngày
        for (DatXe chuyenDi : chuyenDiList) {
            Timestamp thoiGianDat = chuyenDi.getBookingTime();
            if (thoiGianDat != null && !thoiGianDat.before(startDate) && !thoiGianDat.after(endDate)) {
                // Làm tròn thời gian đặt về đầu ngày
                Calendar calChuyenDi = Calendar.getInstance();
                calChuyenDi.setTime(thoiGianDat);
                calChuyenDi.set(Calendar.HOUR_OF_DAY, 0);
                calChuyenDi.set(Calendar.MINUTE, 0);
                calChuyenDi.set(Calendar.SECOND, 0);
                calChuyenDi.set(Calendar.MILLISECOND, 0);

                java.util.Date dayKey = calChuyenDi.getTime(); // Khai báo dayKey
                double giaTien = chuyenDi.getTotalPrice(); // Khai báo giaTien
                revenueByDay.put(dayKey, Double.valueOf(revenueByDay.getOrDefault(dayKey, 0.0) + giaTien));
            }
        }
        System.out.println(revenueByDay);
        return revenueByDay;
    }
    // Phương thức tính doanh thu theo tuần
    public Map<String, Double> getRevenueByWeek(Timestamp start, Timestamp end) {
        Map<String, Double> revenueByWeek = new HashMap<>();
        LocalDate startDate = new java.sql.Date(start.getTime()).toLocalDate();
        LocalDate endDate = new java.sql.Date(end.getTime()).toLocalDate();

        for (DatXe chuyenDi : chuyenDiList) {
            Timestamp ngayKhoiHanh = chuyenDi.getBookingTime();
            if (ngayKhoiHanh != null) {
                LocalDate tripDate = new java.sql.Date(ngayKhoiHanh.getTime()).toLocalDate();
                if (!tripDate.isBefore(startDate) && !tripDate.isAfter(endDate)) {
                    int weekNumber = tripDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                    int year = tripDate.getYear();
                    String weekKey = "Tuần " + weekNumber + " - " + year;
                    revenueByWeek.put(weekKey, revenueByWeek.getOrDefault(weekKey, 0.0) + chuyenDi.getTotalPrice());
                }
            }
        }

        return revenueByWeek;
    }

    public Map<String, Double> getRevenueByMonth(Timestamp startDate, Timestamp endDate) {
        Map<String, Double> revenueByMonth = new HashMap<>();

        // Chuyển đổi startDate và endDate thành Date để dễ xử lý
        Calendar calStart = Calendar.getInstance();
        calStart.setTime(startDate);
        calStart.set(Calendar.DAY_OF_MONTH, 1);
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);

        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(endDate);
        calEnd.set(Calendar.DAY_OF_MONTH, calEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
        calEnd.set(Calendar.HOUR_OF_DAY, 23);
        calEnd.set(Calendar.MINUTE, 59);
        calEnd.set(Calendar.SECOND, 59);
        calEnd.set(Calendar.MILLISECOND, 999);

        // Khởi tạo doanh thu cho từng tháng trong khoảng thời gian
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM/yyyy");
        Calendar cal = (Calendar) calStart.clone();
        while (!cal.after(calEnd)) {
            String monthKey = monthFormat.format(cal.getTime());
            revenueByMonth.put(monthKey, 0.0);
            cal.add(Calendar.MONTH, 1);
        }

        // Tính tổng doanh thu theo tháng
        for (DatXe chuyenDi : chuyenDiList) {
            Timestamp thoiGianDat = chuyenDi.getBookingTime();
            if (thoiGianDat != null && !thoiGianDat.before(startDate) && !thoiGianDat.after(endDate)) {
                String monthKey = monthFormat.format(thoiGianDat);
                double giaTien = chuyenDi.getTotalPrice(); // Khai báo giaTien
                revenueByMonth.put(monthKey, Double.valueOf(revenueByMonth.getOrDefault(monthKey, 0.0) + giaTien));
            }
        }

        return revenueByMonth;
    }
    
    public Map<String, Double> getRevenueByYear(Timestamp start, Timestamp end) {
        Map<String, Double> revenueByYear = new HashMap<>();
        LocalDate startDate = new java.sql.Date(start.getTime()).toLocalDate();
        LocalDate endDate = new java.sql.Date(end.getTime()).toLocalDate();

        for (DatXe chuyenDi : chuyenDiList) {
            Timestamp ngayKhoiHanh = chuyenDi.getBookingTime();
            if (ngayKhoiHanh != null) {
                LocalDate tripDate = new java.sql.Date(ngayKhoiHanh.getTime()).toLocalDate();
                if (!tripDate.isBefore(startDate) && !tripDate.isAfter(endDate)) {
                    int year = tripDate.getYear();
                    String yearKey = String.valueOf(year);
                    revenueByYear.put(yearKey, revenueByYear.getOrDefault(yearKey, 0.0) + chuyenDi.getTotalPrice());
                }
            }
        }

        return revenueByYear;
    }
}