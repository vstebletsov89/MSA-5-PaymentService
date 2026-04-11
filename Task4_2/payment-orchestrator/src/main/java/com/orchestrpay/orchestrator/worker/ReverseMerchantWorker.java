package com.orchestrpay.orchestrator.worker;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ReverseMerchantWorker {

    private static final Logger log = LoggerFactory.getLogger(ReverseMerchantWorker.class);

    @JobWorker(type = "reverse-merchant-transfer", autoComplete = true)
    public Map<String, Object> handle(ActivatedJob job, @Variable String paymentId) {
        log.info("[REVERSE_MERCHANT] Отмена перевода контрагенту. paymentId={}", paymentId);
        return Map.of("paymentStatus", "REVERSAL_COMPLETED");
    }
}