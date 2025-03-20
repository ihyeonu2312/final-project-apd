package site.unoeyhi.apd.eums;

public enum OrderStatus {
    READY,       // 주문 준비 완료
    PROCESSING,  // 주문 처리 중
    SHIPPED,     // 배송됨
    DELIVERED,   // 고객에게 도착
    PAID,        // 결제 완료 상태
    COMPLETED    // ✅ 주문 확정 (결제 + 배송 완료)
}
