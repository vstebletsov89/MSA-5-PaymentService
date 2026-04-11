package com.orchestrpay.orchestrator.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ReserveFundsWorker {

    private static final Logger log = LoggerFactory.getLogger(ReserveFundsWorker.class);

    @JobWorker(type = "reserve-funds", autoComplete = true)
    public Map<String, Object> handle(ActivatedJob job, @Variable String paymentId) {
        log.info("[RESERVE_FUNDS] Списание/резервирование средств. paymentId={}", paymentId);
        return Map.of("paymentStatus", "FUNDS_RESERVED");
    }
}