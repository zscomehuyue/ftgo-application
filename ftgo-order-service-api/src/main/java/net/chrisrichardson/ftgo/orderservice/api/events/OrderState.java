package net.chrisrichardson.ftgo.orderservice.api.events;

/**
 * approval_pending,
 * approved,
 * rejected,
 * cancel_pending,
 * cancelled,
 * revision_pending,
 * 完成之后的状态？
 */
public enum OrderState {
    APPROVAL_PENDING,
    APPROVED,
    REJECTED,
    CANCEL_PENDING,
    CANCELLED,
    REVISION_PENDING,
}
