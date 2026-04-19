package com.orchestrpay.orchestrator.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class CreatePaymentWorker {

    private static final Logger log = LoggerFactory.getLogger(CreatePaymentWorker.class);

    @PostConstruct
    public void init() {
        log.info("[CREATE_PAYMENT] Worker bean initialized");
    }

    @JobWorker(type = "create-payment", autoComplete = true)
    public Map<String, Object> handle(ActivatedJob job) {
        String paymentId = UUID.randomUUID().toString();
        log.info("[CREATE_PAYMENT] Создание платежа. paymentId={}, processInstanceKey={}",
                paymentId, job.getProcessInstanceKey());
        return Map.of("paymentId", paymentId, "paymentStatus", "NEW");
    }
}