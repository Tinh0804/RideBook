package com.project.BookCarOnline.catalog.service;

import com.project.BookCarOnline.catalog.dto.VehicleTypeSummary;
import com.project.BookCarOnline.catalog.entity.TimeSlot;
import com.project.BookCarOnline.catalog.entity.VehicleType;
import com.project.BookCarOnline.catalog.entity.VehicleTypePricing;
import com.project.BookCarOnline.catalog.repository.TimeSlotRepository;
import com.project.BookCarOnline.catalog.repository.VehicleTypePricingRepository;
import com.project.BookCarOnline.catalog.repository.VehicleTypeRepository;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleTypeServiceTest {

    @Mock
    VehicleTypeRepository vehicleTypeRepository;

    @Mock
    TimeSlotRepository timeSlotRepository;

    @Mock
    VehicleTypePricingRepository vehicleTypePricingRepository;

    @InjectMocks
    VehicleTypeService vehicleTypeService;

    VehicleType car4Seats;

    @BeforeEach
    void setUp() {
        car4Seats = new VehicleType();
        car4Seats.setVehicleTypeId("vt-4seats");
        car4Seats.setVehicleTypeName("Car 4 Seats");
        car4Seats.setPricePerKm(12000.0);
        car4Seats.setMaxPassengers(4);
        car4Seats.setIcon("car4.png");
    }

    @Test
    void getAllVehicleTypes_ReturnsList() {
        when(vehicleTypeRepository.findAll()).thenReturn(List.of(car4Seats));

        List<VehicleType> list = vehicleTypeService.getAllVehicleTypes();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Car 4 Seats", list.get(0).getVehicleTypeName());
    }

    @Test
    void getVehicleTypeSummary_Found_ReturnsSummary() {
        when(vehicleTypeRepository.findById("vt-4seats")).thenReturn(Optional.of(car4Seats));

        VehicleTypeSummary summary = vehicleTypeService.getVehicleTypeSummary("vt-4seats");

        assertNotNull(summary);
        assertEquals("vt-4seats", summary.vehicleTypeId());
        assertEquals("Car 4 Seats", summary.vehicleTypeName());
        assertEquals(12000.0, summary.pricePerKm());
        assertEquals(4, summary.maxPassengers());
    }

    @Test
    void getVehicleTypeSummary_NotFound_ThrowsAppException() {
        when(vehicleTypeRepository.findById("non-existent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(
                AppException.class,
                () -> vehicleTypeService.getVehicleTypeSummary("non-existent"));

        assertEquals(ErrorCode.VEHICLE_TYPE_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void createVehicleType_SavesAndReturns() {
        when(vehicleTypeRepository.save(car4Seats)).thenReturn(car4Seats);

        VehicleType created = vehicleTypeService.createVehicleType(car4Seats);

        assertNotNull(created);
        assertEquals("Car 4 Seats", created.getVehicleTypeName());
        verify(vehicleTypeRepository).save(car4Seats);
    }

    @Test
    void updateVehicleType_Existing_UpdatesAndSaves() {
        when(vehicleTypeRepository.findById("vt-4seats")).thenReturn(Optional.of(car4Seats));

        VehicleType updatedData = new VehicleType();
        updatedData.setVehicleTypeName("Car 4 Seats Premium");
        updatedData.setPricePerKm(15000.0);
        updatedData.setMaxPassengers(4);
        updatedData.setIcon("car4-prem.png");

        when(vehicleTypeRepository.save(car4Seats)).thenReturn(car4Seats);

        VehicleType result = vehicleTypeService.updateVehicleType("vt-4seats", updatedData);

        assertEquals("Car 4 Seats Premium", result.getVehicleTypeName());
        assertEquals(15000.0, result.getPricePerKm());
        verify(vehicleTypeRepository).save(car4Seats);
    }

    @Test
    void deleteVehicleType_DeletesPricingAndVehicleType() {
        vehicleTypeService.deleteVehicleType("vt-4seats");

        verify(vehicleTypePricingRepository).deleteByVehicleType_VehicleTypeId("vt-4seats");
        verify(vehicleTypeRepository).deleteById("vt-4seats");
    }

    @Test
    void getCurrentSurcharge_MatchingTimeSlot_ReturnsSurcharge() {
        TimeSlot allDaySlot = new TimeSlot();
        allDaySlot.setTimeId("slot-1");
        allDaySlot.setStartTime(LocalTime.MIN);
        allDaySlot.setEndTime(LocalTime.MAX);

        VehicleTypePricing pricing = new VehicleTypePricing();
        pricing.setVehicleType(car4Seats);
        pricing.setTime(allDaySlot);
        pricing.setSurcharge(1.5);

        double surcharge = vehicleTypeService.getCurrentSurcharge(
                "vt-4seats",
                List.of(allDaySlot),
                List.of(pricing)
        );

        assertEquals(1.5, surcharge);
    }

    @Test
    void getCurrentSurcharge_NoMatchingPricing_ReturnsDefaultOne() {
        double surcharge = vehicleTypeService.getCurrentSurcharge(
                "vt-4seats",
                List.of(),
                List.of()
        );

        assertEquals(1.0, surcharge);
    }
}
