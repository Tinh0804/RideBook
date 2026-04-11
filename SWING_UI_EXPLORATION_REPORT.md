# Java Swing Frontend Project Exploration Report

## Project Overview
After comprehensive exploration of the workspace, **NO dedicated Java Swing UI frontend project was found**. 

The project consists of:
1. **Spring Boot REST API Backend** (`src/` directory)
2. **Legacy Servlet-based Backend** (`NHOM/` and `servlet-legacy/` directories)

### Search Results
- Ran: `find . -name '*.java' | xargs grep -l 'JFrame\|JPanel\|Swing\|swing'`
- Result: Only `RideBookRepository.java` matched (contains `import javax.swing.text.html.Option;` but is NOT a UI file)
- Additional searches for `*.form`, `JFrame`, `JPanel`, `Dialog`, etc. yielded no results

## Architecture: REST API Based (NOT Swing Desktop)

This is a **ride-sharing/taxi booking application** with a REST API architecture, likely consumed by:
- Web frontend (React/Vue/Angular)
- Mobile apps (Android/iOS)
- NOT a Swing desktop application

---

## Driver Booking Acceptance Flow

### Key Files Related to Driver Booking:

### 1. **BookingController.java**
**Location:** `src/main/java/com/project/BookCarOnline/Controller/BookingController.java`

**Key Endpoints:**
- `GET /bookings` - List all bookings
- `GET /bookings/{bookingId}` - Get booking details
- `GET /bookings/available` - Get available rides (for drivers to accept)
- `PUT /bookings/{bookingId}/assign-driver` - **Driver accepts booking**
- `PUT /bookings/{bookingId}/complete` - Mark booking as complete
- `DELETE /bookings/{bookingId}` - Cancel booking

**Driver Acceptance Implementation:**
```java
@PutMapping("/{bookingId}/assign-driver")
public APIResponse<BookingDetailResponse> assignDriver(
        @PathVariable String bookingId,
        @RequestParam String driverId)
```

### 2. **BookingService.java**
**Location:** `src/main/java/com/project/BookCarOnline/Service/BookingService.java`

**Driver Assignment Logic (Lines 135-166):**
- Retrieves booking by ID
- Checks if booking status is "Đang chờ" (Waiting)
- Validates driver availability (not on another CONFIRMED ride)
- Updates booking status to CONFIRMED
- Sets pickup time to current timestamp
- Returns updated booking details

**Key Method:**
```java
@Transactional
public BookingDetailResponse assignDriver(String bookingId, String driverId)
```

**Validation Checks:**
1. Booking exists and is in PENDING state
2. Driver exists
3. Driver is not already on another ride with CONFIRMED status

### 3. **Booking Entity**
**Location:** `src/main/java/com/project/BookCarOnline/Entity/Booking.java`

**Booking Status Values:**
- `PENDING` - Waiting for driver
- `CONFIRMED` - Driver assigned
- `COMPLETED` - Ride finished
- `CANCELLED` - Ride cancelled

**Key Fields:**
- `bookingId` - Unique booking identifier
- `customerNo` - Customer reference
- `driverNo` - Driver reference (null until assigned)
- `pickupLocation` - Pickup address
- `dropoffLocation` - Dropoff address
- `totalPrice` - Trip fare with surge pricing
- `bookingStatus` - Current status
- `bookingTime` - When customer booked
- `pickupTime` - When driver started pickup

### 4. **Driver Entity**
**Location:** `src/main/java/com/project/BookCarOnline/Entity/Driver.java`

**Key Fields:**
- `driverId` - Unique identifier
- `driverName` - Name
- `phone` - Contact number
- `email` - Email address
- `vehicleType` - Type of vehicle
- `licensePlate` - Vehicle registration
- `activityStatus` - Online/offline status
- `area` - Operating area/region
- `currentLat`, `currentLng` - Current GPS location

### 5. **DriverController.java**
**Location:** `src/main/java/com/project/BookCarOnline/Controller/DriverController.java`

**Key Endpoints:**
- `GET /drivers/my-info` - Get current driver's info
- `GET /drivers/active` - List active drivers
- `GET /drivers/area/{area}` - Get drivers by area
- `POST /drivers/register` - Driver registration
- `PUT /drivers/{driverId}` - Update driver info

### 6. **RideBookRepository.java**
**Location:** `src/main/java/com/project/BookCarOnline/Repository/RideBookRepository.java`

**Key Query Methods:**
- `findByBookingStatusAndDriverNoIsNull()` - Available rides (no driver yet)
- `findByDriverNo_DriverIdAndBookingStatus()` - Driver's current ride
- `findByBookingStatusOrderByBookingTimeDesc()` - Bookings by status

---

## Legacy Servlet APIs (Older Implementation)

### 1. **API_KiemTraNhanChuyen.java**
**Location:** `NHOM/src/main/java/controller/Driver/API_KiemTraNhanChuyen.java`

**Purpose:** Check if driver has accepted any booking
**Endpoint:** `/api/driver/kiemTraNhanChuyen?idTX={driverId}`
**HTTP Method:** GET
**Response:** JSON with status and list of accepted trips

```java
public class API_KiemTraNhanChuyen extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String idTX = req.getParameter("idTX");
        mapper.writeValue(resp.getWriter(), Map.of(
            "status", 200,
            "data", chuyenXeService.kiemTraNhanChuyen(idTX)
        ));
    }
}
```

### 2. **API_HoanThanhChuyenXe.java**
**Location:** `NHOM/src/main/java/controller/Driver/API_HoanThanhChuyenXe.java`

**Purpose:** Mark trip as completed by driver
**Endpoint:** `/api/driver/hoanThanhChuyenXe`
**HTTP Method:** PUT
**Request Body:** `{ "id_chuyenXe": "booking_id" }`

**Implementation:**
```java
protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
    String id = body.get("id_chuyenXe").getAsString();
    String message = chuyenXeService.doiTrangThai(id.trim(), "Hoàn thành");
}
```

### 3. **API_DSTaiXeGanKhachHang.java**
**Location:** `NHOM/src/main/java/controller/Driver/API_DSTaiXeGanKhachHang.java`

**Purpose:** Get available drivers near customer location
**Endpoint:** `/api/driver/dsGanKhachHang?diaChiKH={customer_address}`
**HTTP Method:** GET

---

## Booking Flow Diagram

```
1. Customer Books Ride
   → POST /bookings (CreateBookingRequest)
   → Create PENDING booking
   → Calculate surge pricing
   → Start dispatcher for available drivers

2. Driver Views Available Rides
   → GET /bookings/available?driverArea={area}
   → List PENDING bookings in driver's area
   → OR Legacy: GET /api/driver/dsGanKhachHang

3. Driver Accepts Booking
   → PUT /bookings/{bookingId}/assign-driver?driverId={driverId}
   → Check driver availability
   → Update booking: PENDING → CONFIRMED
   → Set pickupTime

4. Driver Completes Ride
   → PUT /bookings/{bookingId}/complete
   → Update booking: CONFIRMED → COMPLETED
   → Set arrivalTime
   → OR Legacy: PUT /api/driver/hoanThanhChuyenXe
```

---

## Technology Stack

**Backend:**
- Spring Boot 3.x
- Spring Data JPA
- Spring Security with JWT
- Jakarta Servlet API
- Hibernate ORM

**Database:**
- MySQL (via JPA/Hibernate)
- Stored procedures for complex operations

**External Services:**
- Google Maps API (geocoding, distance)
- Firebase Storage
- Payment APIs (VNPay, MoMo)

**No Swing Components Found:**
- No `JFrame`, `JPanel`, `JButton`
- No GUI builders or `.form` files
- No desktop application main classes

---

## Conclusion

This is a **modern REST API-based ride-sharing platform** with:
✅ Spring Boot microservices backend
✅ REST/JSON APIs for all operations
✅ Legacy servlet APIs for compatibility
✅ Real-time location tracking
✅ Dynamic surge pricing
✅ Payment integration

❌ NO Java Swing desktop frontend found
❌ This application is consumed by web/mobile clients, not a desktop application

**The UI would be implemented separately in:**
- React/Vue.js web application
- Android/iOS mobile apps
- Web portals for admin/drivers
