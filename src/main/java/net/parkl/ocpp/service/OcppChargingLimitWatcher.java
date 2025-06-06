/*
 * Parkl Digital Technologies
 * Copyright (C) 2020-2021
 * All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.parkl.ocpp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.parkl.ocpp.entities.OcppChargingProcess;
import net.parkl.ocpp.service.middleware.OcppChargingMiddleware;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static net.parkl.ocpp.service.OcppConsumptionHelper.getKwhValue;

@Component
@RequiredArgsConstructor
@Slf4j
public class OcppChargingLimitWatcher {
    private final ChargingProcessService proxyService;
    private final OcppChargingMiddleware chargingMiddleware;
    private final TaskExecutor taskExecutor;


    public void checkKwhChargingLimit() {
        log.debug("Checking kwh charging limit...");
        List<OcppChargingProcess> kwhLimitProcesses = proxyService.findOpenChargingProcessesWithLimitKwh();
        for (OcppChargingProcess cp : kwhLimitProcesses) {
            if (cp.getTransactionStart() != null) {
                log.info("Checking charging process with kwh limit: {}...", cp.getOcppChargingProcessId());
                PowerValue pw = chargingMiddleware.getPowerValue(cp.getTransactionStart().getTransactionPk());
                float kWh = getKwhValue(pw.getValue(), pw.getUnit());
                log.info("Current consumption = {} limit = {} for charging process id = {}",
                        kWh, cp.getLimitKwh(), cp.getOcppChargingProcessId());
                if (kWh >= cp.getLimitKwh()) {
                    log.info("Limit {} exceeded ({}) for charging process, stopping: {}...", cp.getLimitKwh(), kWh, cp.getOcppChargingProcessId());

                    taskExecutor.execute(() -> chargingMiddleware.stopChargingWithLimit(cp.getOcppChargingProcessId(), cp.getLimitKwh()));
                }
            }
        }
    }

    public void checkMinuteChargingLimit() {
        log.debug("Checking minute charging limit...");

        List<OcppChargingProcess> minuteLimitProcesses = proxyService.findOpenChargingProcessesWithLimitMinute();
        for (OcppChargingProcess cp : minuteLimitProcesses) {
            if (cp.getTransactionStart() != null) {
                log.info("Checking charging process with minute limit: {}...", cp.getOcppChargingProcessId());
                int duration = (int) ChronoUnit.MINUTES.between(cp.getStartDate(), LocalDateTime.now());
                log.info("Current duration = {} limit = {} for charging process id = {}",
                        duration, cp.getLimitMinute(), cp.getOcppChargingProcessId());
                if (duration >= cp.getLimitMinute()) {
                    log.info("Limit {} exceeded ({}) for charging process, stopping: {}...", cp.getLimitMinute(), duration, cp.getOcppChargingProcessId());

                    PowerValue pw = chargingMiddleware.getPowerValue(cp.getTransactionStart().getTransactionPk());
                    float kWh = getKwhValue(pw.getValue(), pw.getUnit());

                    taskExecutor.execute(() -> chargingMiddleware.stopChargingWithLimit(cp.getOcppChargingProcessId(), kWh));
                }
            }
        }
    }
}
