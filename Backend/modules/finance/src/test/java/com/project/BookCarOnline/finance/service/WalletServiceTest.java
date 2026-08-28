package com.project.BookCarOnline.finance.service;

import com.project.BookCarOnline.finance.dto.WalletTransactionResult;
import com.project.BookCarOnline.finance.dto.response.WalletResponse;
import com.project.BookCarOnline.finance.dto.response.WalletTransactionResponse;
import com.project.BookCarOnline.finance.entity.Wallet;
import com.project.BookCarOnline.finance.entity.WalletTransaction;
import com.project.BookCarOnline.finance.entity.enums.TransactionStatus;
import com.project.BookCarOnline.finance.entity.enums.TransactionType;
import com.project.BookCarOnline.finance.entity.enums.WalletStatus;
import com.project.BookCarOnline.finance.mapper.WalletMapper;
import com.project.BookCarOnline.finance.repository.WalletRepository;
import com.project.BookCarOnline.finance.repository.WalletTransactionRepository;
import com.project.BookCarOnline.identity.service.IdentityQueryService;
import com.project.BookCarOnline.shared.exception.AppException;
import com.project.BookCarOnline.shared.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    WalletRepository walletRepository;

    @Mock
    WalletTransactionRepository transactionRepository;

    @Mock
    IdentityQueryService identityQueryService;

    @Mock
    WalletMapper mapper;

    @InjectMocks
    WalletService walletService;

    Wallet sampleWallet;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(walletService, "platformRate", 20.0);

        sampleWallet = new Wallet();
        sampleWallet.setWalletId("wallet-1");
        sampleWallet.setDriverId("driver-1");
        sampleWallet.setBalance(100000.0);
        sampleWallet.setStatus(WalletStatus.ACTIVE);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedDriver(String driverId) {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("profile_id", driverId)
                .claim("sub", "account-1")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @Test
    void getMyBalance_Authenticated_ReturnsWalletResponse() {
        mockAuthenticatedDriver("driver-1");
        when(walletRepository.findByDriverId("driver-1")).thenReturn(Optional.of(sampleWallet));
        WalletResponse expectedResponse = new WalletResponse("wallet-1", 100000.0, WalletStatus.ACTIVE);
        when(mapper.toWalletResponse(sampleWallet)).thenReturn(expectedResponse);

        WalletResponse actualResponse = walletService.getMyBlance();

        assertNotNull(actualResponse);
        assertEquals("wallet-1", actualResponse.getWalletId());
        assertEquals(100000.0, actualResponse.getBalance());
        assertEquals(WalletStatus.ACTIVE, actualResponse.getStatus());
    }

    @Test
    void getMyBalance_Unauthenticated_ThrowsAppException() {
        AppException exception = assertThrows(AppException.class, () -> walletService.getMyBlance());
        assertEquals(ErrorCode.EXCHANGE_TOKEN_FAIL, exception.getErrorCode());
    }

    @Test
    void addBalance_IncreasesBalanceAndSavesCompletedTransaction() {
        when(walletRepository.findByDriverId("driver-1")).thenReturn(Optional.of(sampleWallet));

        walletService.addBalance("driver-1", 50000.0);

        assertEquals(150000.0, sampleWallet.getBalance());
        verify(walletRepository).save(sampleWallet);

        ArgumentCaptor<WalletTransaction> txnCaptor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(transactionRepository).save(txnCaptor.capture());

        WalletTransaction capturedTxn = txnCaptor.getValue();
        assertEquals(50000.0, capturedTxn.getAmount());
        assertEquals(TransactionType.TRIP_INCOME, capturedTxn.getType());
        assertEquals(TransactionStatus.COMPLETED, capturedTxn.getStatus());
        assertEquals(sampleWallet, capturedTxn.getWallet());
    }

    @Test
    void deductBalance_SufficientBalance_DecreasesBalance() {
        when(walletRepository.findByDriverId("driver-1")).thenReturn(Optional.of(sampleWallet));

        walletService.deductBalance("driver-1", 30000.0, "WITHDRAW");

        assertEquals(70000.0, sampleWallet.getBalance());
        verify(walletRepository).save(sampleWallet);

        ArgumentCaptor<WalletTransaction> txnCaptor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(transactionRepository).save(txnCaptor.capture());
        assertEquals(TransactionType.TRIP_FEE, txnCaptor.getValue().getType());
        assertEquals(TransactionStatus.COMPLETED, txnCaptor.getValue().getStatus());
    }

    @Test
    void deductBalance_InsufficientBalance_ThrowsIllegalStateException() {
        when(walletRepository.findByDriverId("driver-1")).thenReturn(Optional.of(sampleWallet));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> walletService.deductBalance("driver-1", 200000.0, "WITHDRAW"));

        assertEquals("Số dư không đủ để thực hiện giao dịch", exception.getMessage());
    }

    @Test
    void createDepositRequest_ValidAmount_ReturnsPendingResult() {
        when(walletRepository.findByDriverId("driver-1")).thenReturn(Optional.of(sampleWallet));

        WalletTransaction savedTxn = new WalletTransaction();
        savedTxn.setTransactionId("txn-123");
        savedTxn.setAmount(50000.0);
        savedTxn.setType(TransactionType.DEPOSIT);
        savedTxn.setStatus(TransactionStatus.PENDING);
        when(transactionRepository.save(any(WalletTransaction.class))).thenReturn(savedTxn);

        WalletTransactionResult result = walletService.createDepositRequest("driver-1", 50000.0);

        assertNotNull(result);
        assertEquals("txn-123", result.transactionId());
        assertEquals(50000.0, result.amount());
        assertEquals(100000.0, result.newBalance());
    }

    @Test
    void createDepositRequest_NegativeOrZeroAmount_ThrowsIllegalStateException() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> walletService.createDepositRequest("driver-1", 0.0));

        assertEquals("Số tiền nạp phải lớn hơn 0", exception.getMessage());
    }

    @Test
    void processPaymentCallback_Success_UpdatesWalletAndTransaction() {
        WalletTransaction txn = new WalletTransaction();
        txn.setTransactionId("txn-123");
        txn.setWallet(sampleWallet);
        txn.setAmount(50000.0);
        txn.setStatus(TransactionStatus.PENDING);

        when(transactionRepository.findById("txn-123")).thenReturn(Optional.of(txn));

        boolean processed = walletService.processPaymentCallback("txn-123", true, "VNPAY-REF-999");

        assertTrue(processed);
        assertEquals(150000.0, sampleWallet.getBalance());
        assertEquals(TransactionStatus.COMPLETED, txn.getStatus());
        assertEquals("VNPAY-REF-999", txn.getReferenceId());
        verify(walletRepository).save(sampleWallet);
        verify(transactionRepository).save(txn);
    }

    @Test
    void processPaymentCallback_DoubleSpendingPrevention_ReturnsTrueWithoutApplyingTwice() {
        WalletTransaction txn = new WalletTransaction();
        txn.setTransactionId("txn-123");
        txn.setWallet(sampleWallet);
        txn.setAmount(50000.0);
        txn.setStatus(TransactionStatus.COMPLETED);

        when(transactionRepository.findById("txn-123")).thenReturn(Optional.of(txn));

        boolean processed = walletService.processPaymentCallback("txn-123", true, "VNPAY-REF-999");

        assertTrue(processed);
        assertEquals(100000.0, sampleWallet.getBalance());
    }

    @Test
    void processPaymentCallback_NotFound_ReturnsFalse() {
        when(transactionRepository.findById("non-existent")).thenReturn(Optional.empty());

        boolean processed = walletService.processPaymentCallback("non-existent", true, "REF");

        assertFalse(processed);
    }

    @Test
    void autoWithdraw_SufficientBalance_CompletesWithdrawal() {
        when(walletRepository.findByDriverId("driver-1")).thenReturn(Optional.of(sampleWallet));

        WalletTransaction savedTxn = new WalletTransaction();
        savedTxn.setTransactionId("txn-withdraw-1");
        savedTxn.setAmount(40000.0);
        savedTxn.setType(TransactionType.WITHDRAWAL);
        savedTxn.setStatus(TransactionStatus.COMPLETED);
        when(transactionRepository.save(any(WalletTransaction.class))).thenReturn(savedTxn);

        WalletTransactionResult result = walletService.autoWithdraw("driver-1", 40000.0);

        assertNotNull(result);
        assertEquals(60000.0, sampleWallet.getBalance());
        assertEquals("txn-withdraw-1", result.transactionId());
        verify(walletRepository).save(sampleWallet);
    }

    @Test
    void deductPlatformFee_CalculatesCorrectFeeAndDeductsFromWallet() {
        when(walletRepository.findByDriverId("driver-1")).thenReturn(Optional.of(sampleWallet));

        // trip price = 100,000, platform rate = 20% -> fee = 20,000
        walletService.deductPlatformFee("driver-1", 100000.0, "booking-101");

        assertEquals(80000.0, sampleWallet.getBalance());
        verify(walletRepository).save(sampleWallet);

        ArgumentCaptor<WalletTransaction> txnCaptor = ArgumentCaptor.forClass(WalletTransaction.class);
        verify(transactionRepository).save(txnCaptor.capture());

        WalletTransaction txn = txnCaptor.getValue();
        assertEquals(20000.0, txn.getAmount());
        assertEquals(TransactionType.TRIP_FEE, txn.getType());
        assertEquals(TransactionStatus.COMPLETED, txn.getStatus());
        assertEquals("BOOKING_booking-101", txn.getReferenceId());
    }

    @Test
    void adjustBalanceAdmin_PositiveAmount_IncreasesBalanceAndCreatesDepositTxn() {
        when(walletRepository.findByDriverId("driver-1")).thenReturn(Optional.of(sampleWallet));

        WalletTransactionResponse response = WalletTransactionResponse.builder()
                .transactionId("adj-1")
                .amount(50000.0)
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.COMPLETED)
                .build();
        when(mapper.toTransactionHistoryResponse(any())).thenReturn(List.of(response));

        WalletTransactionResponse result = walletService.adjustBalanceAdmin("driver-1", 50000.0, "Bonus reward");

        assertNotNull(result);
        assertEquals(150000.0, sampleWallet.getBalance());
        verify(walletRepository).save(sampleWallet);
        verify(transactionRepository).save(any(WalletTransaction.class));
    }
}
