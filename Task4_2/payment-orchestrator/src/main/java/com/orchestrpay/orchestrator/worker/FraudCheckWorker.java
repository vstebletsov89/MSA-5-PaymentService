package com.orchestrpay.orchestrator.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class FraudCheckWorker {

    private static final Logger log = LoggerFactory.getLogger(FraudCheckWorker.class);

    private static final String[] DECISIONS = {"APPROVED", "REJECTED", "MANUAL_REVIEW"};

    @JobWorker(type = "fraud-check", autoComplete = true)
    public Map<String, Object> handle(ActivatedJob job, @Variable String paymentId) {
        // Эмуляция: случайно выбираем один из трёх сценариев
        String decision = DECISIONS[ThreadLocalRandom.current().nextInt(DECISIONS.length)];
        log.info("[FRAUD_CHECK] Антифрод-проверка. paymentId={}, result={}", paymentId, decision);
        return Map.of("fraudResult", decision, "paymentStatus", "FRAUD_CHECK_PENDING");
    }
}