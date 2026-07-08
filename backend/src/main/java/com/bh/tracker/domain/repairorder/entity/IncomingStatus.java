package com.bh.tracker.domain.repairorder.entity;

public enum IncomingStatus {
    ORDER_PLACED,   // 주문중
    RECEIVED,       // 입고완료
    RETURN_PENDING, // 반품예정
    RETURNED        // 반품완료
}
