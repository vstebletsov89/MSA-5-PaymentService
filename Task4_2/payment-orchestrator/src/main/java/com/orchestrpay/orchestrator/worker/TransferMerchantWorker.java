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
public class TransferMerchantWorker {

    private static final Logger log = LoggerFactory.getLogger(TransferMerchantWorker.class);

    @JobWorker(type = "transfer-to-merchant", autoComplete = true)
    public Map<String, Object> handle(ActivatedJob job, @Variable String paymentId) {
        // Эмуляция: 90% — успех, 10% — ошибка (для демо компенсации)
        boolean success = ThreadLocalRandom.current().nextInt(10) > 0;
        log.info("[TRANSFER_MERCHANT] Перевод контрагенту. paymentId={}, success={}", paymentId, success);
        return Map.of("transferSuccess", success, "paymentStatus", success ? "MERCHANT_TRANSFERRED" : "FAILED");
    }
}