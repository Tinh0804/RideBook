package com.project.BookCarOnline.Entity.Enum;

public enum PaymentMethod {
    VNPAY("VNPay"),
    MOMO("MoMo"),
    CASH("Cash");

    private final String method;

    PaymentMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
