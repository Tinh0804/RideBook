package com.project.BookCarOnline.identity.service;

import com.project.BookCarOnline.identity.dto.summary.AccountSummary;
import com.project.BookCarOnline.identity.dto.summary.CustomerSummary;
import com.project.BookCarOnline.identity.dto.summary.DriverSummary;
import com.project.BookCarOnline.identity.entity.Account;
import com.project.BookCarOnline.identity.entity.Customer;
import com.project.BookCarOnline.identity.entity.Driver;
import com.project.BookCarOnline.identity.repository.AccountRepository;
import com.project.BookCarOnline.identity.repository.CustomerRepository;
import com.project.BookCarOnline.identity.repository.DriverRepository;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IdentityQueryService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final DriverRepository driverRepository;

    public CustomerSummary getCustomer(String customerId) {
        return toSummary(customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND)));
    }

    public CustomerSummary resolveCustomer(String customerOrAccountId) {
        return customerRepository.findById(customerOrAccountId)
                .or(() -> customerRepository.findByAccountId(customerOrAccountId))
                .map(this::toSummary)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
    }

    public DriverSummary getDriver(String driverId) {
        return toSummary(driverRepository.findById(driverId)
                .orElseThrow(() -> new AppException(ErrorCode.DRIVER_NOT_FOUND)));
    }

    public boolean driverExists(String driverId) {
        return driverRepository.existsById(driverId);
    }

    public AccountSummary getAccount(String accountId) {
        return toSummary(accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED)));
    }

    public AccountSummary getAccountByUsername(String username) {
        return toSummary(accountRepository.findByUserName(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED)));
    }

    public List<AccountSummary> getAccounts() {
        return accountRepository.findAll().stream().map(this::toSummary).toList();
    }

    public List<AccountSummary> getAccountsByRole(String roleId) {
        return accountRepository.findByRoleNo_RoleId(roleId).stream().map(this::toSummary).toList();
    }

    public List<DriverSummary> getActiveDrivers() {
        return driverRepository.findByActivityStatusTrue().stream().map(this::toSummary).toList();
    }

    public List<DriverSummary> getDrivers(Iterable<String> driverIds) {
        return driverRepository.findAllById(driverIds).stream().map(this::toSummary).toList();
    }

    public List<String> searchCustomerIds(String search) {
        return customerRepository.searchCustomers(search, Pageable.unpaged()).stream()
                .map(Customer::getCustomerId).toList();
    }

    public List<String> searchDriverIds(String search) {
        return driverRepository.searchDrivers(search, Pageable.unpaged()).stream()
                .map(Driver::getDriverId).toList();
    }

    public List<DriverSummary> getActiveDriversByArea(String area) {
        return driverRepository.findByAreaAndActivityStatusTrue(area).stream().map(this::toSummary).toList();
    }

    public List<DriverSummary> getNearbyDrivers(double lat, double lng, double radius, String vehicleTypeId) {
        return driverRepository.findTrulyAvailableDriversNearby(lat, lng, radius, vehicleTypeId)
                .stream().map(this::toSummary).toList();
    }

    public void updateLastTripTime(String driverId, LocalDateTime time) {
        driverRepository.updateLastTripTime(driverId, time);
    }

    public void updateFcmToken(String accountId, String fcmToken) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXITED));
        account.setFcmToken(fcmToken);
        accountRepository.save(account);
    }

    public long countCustomers() {
        return customerRepository.count();
    }

    public long countDrivers() {
        return driverRepository.count();
    }

    private CustomerSummary toSummary(Customer customer) {
        return new CustomerSummary(
                customer.getCustomerId(),
                customer.getCustomerName(),
                customer.getPhone(),
                toSummary(customer.getAccount()));
    }

    private DriverSummary toSummary(Driver driver) {
        return new DriverSummary(
                driver.getDriverId(),
                driver.getDriverName(),
                driver.getPhone(),
                driver.getVehicleTypeId(),
                driver.getLicensePlate(),
                driver.getArea(),
                driver.getCurrentLat(),
                driver.getCurrentLng(),
                driver.getLastTripTime(),
                driver.getScore(),
                driver.getActivityStatus(),
                toSummary(driver.getAccount()));
    }

    private AccountSummary toSummary(Account account) {
        if (account == null) {
            return null;
        }
        return new AccountSummary(
                account.getAccountId(),
                account.getUserName(),
                account.getRoleNo() != null ? account.getRoleNo().getRoleName().name() : null,
                account.getFcmToken(),
                account.getAccountStatus());
    }
}
