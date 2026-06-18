export const BookingStatus = Object.freeze({
    PENDING: 'PENDING',
    ACCEPTED: 'ACCEPTED',
    ARRIVED: 'ARRIVED',
    IN_PROGRESS: 'IN_PROGRESS',
    CANCELLED: 'CANCELLED',
    COMPLETED: 'COMPLETED'
});

export const CustomerPromotionStatus = Object.freeze({
    SAVED: 'SAVED',
    USED: 'USED',
    EXPIRED: 'EXPIRED'
});

export const DiscountType = Object.freeze({
    PERCENTAGE: 'PERCENTAGE',
    FIXED_AMOUNT: 'FIXED_AMOUNT'
});

export const PaymentMethod = Object.freeze({
    VNPAY: 'VNPAY',
    MOMO: 'MOMO',
    CASH: 'CASH',
    ONLINE: 'ONLINE'
});

export const PredefinedRole = Object.freeze({
    ADMIN: 'ADMIN',
    CUSTOMER: 'CUSTOMER',
    DRIVER: 'DRIVER'
});

export const Provider = Object.freeze({
    LOCAL: 'LOCAL',
    GOOGLE: 'GOOGLE',
    FACEBOOK: 'FACEBOOK'
});

export const RejectionType = Object.freeze({
    REJECTED: 'REJECTED',
    IGNORED: 'IGNORED'
});

export const TransactionStatus = Object.freeze({
    PENDING: 'PENDING',
    COMPLETED: 'COMPLETED',
    FAILED: 'FAILED',
    CANCELLED: 'CANCELLED'
});

export const TransactionType = Object.freeze({
    DEPOSIT: 'DEPOSIT',
    WITHDRAWAL: 'WITHDRAWAL',
    TRIP_FEE: 'TRIP_FEE',
    TRIP_INCOME: 'TRIP_INCOME',
    PAYMENT: 'PAYMENT'
});

export const WalletStatus = Object.freeze({
    ACTIVE: 'ACTIVE',
    INACTIVE: 'INACTIVE',
    BLOCKED: 'BLOCKED'
});
