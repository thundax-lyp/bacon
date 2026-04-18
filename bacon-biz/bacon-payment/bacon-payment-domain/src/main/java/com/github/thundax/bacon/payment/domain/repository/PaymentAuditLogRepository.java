package com.github.thundax.bacon.payment.domain.repository;

import com.github.thundax.bacon.payment.domain.model.entity.PaymentAuditLog;
import java.util.List;

public interface PaymentAuditLogRepository {

    void insert(PaymentAuditLog auditLog);

    List<PaymentAuditLog> listLogsByPaymentNo(String paymentNo);
}
