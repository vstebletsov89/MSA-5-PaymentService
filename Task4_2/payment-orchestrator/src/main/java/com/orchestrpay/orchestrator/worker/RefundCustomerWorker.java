package com.orchestrpay.orchestrator.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RefundCustomerWorker {

    private static final Logger log = LoggerFactory.getLogger(RefundCustomerWorker.class);

    @JobWorker(type = "refund-customer", autoComplete = true)
    public Map<String, Object> handle(ActivatedJob job, @Variable String paymentId) {
        log.info("[REFUND_CUSTOMER] Возврат средств клиенту. paymentId={}", paymentId);
        return Map.of("paymentStatus", "REFUNDED");
    }
}