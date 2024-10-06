package net.parkl.ocpp.service.cs.cleanup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.parkl.ocpp.entities.OcppChargingProcess;
import net.parkl.ocpp.entities.Transaction;
import net.parkl.ocpp.service.ChargingProcessService;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AutomaticCleanupChecker implements TransactionCleanupChecker {
    private final ChargingProcessService chargingProcessService;
    private final EmobilityServiceProviderChecker emobilityServiceProviderChecker;
    private final TransactionCleanupConfig config;

    @Override
    public boolean checkTransactionForCleanup(Transaction transaction) {
        OcppChargingProcess chargingProcess = chargingProcessService.findByTransactionId(transaction.getTransactionPk());
        boolean cleanup = false;
        if (chargingProcess != null) {
            cleanup = emobilityServiceProviderChecker.checkEmobilityServiceProvider(transaction, chargingProcess);
        } else {
            log.info("Charging process was null for transaction: {}", transaction.getTransactionPk());
            cleanup = config.checkNonExistentThreshold(transaction.getStartEventTimestamp());
        }
        return cleanup;
    }
}