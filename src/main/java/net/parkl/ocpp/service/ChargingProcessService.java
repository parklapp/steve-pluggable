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
import net.parkl.ocpp.entities.Connector;
import net.parkl.ocpp.entities.OcppChargingProcess;
import net.parkl.ocpp.entities.TransactionStart;
import net.parkl.ocpp.repositories.ConnectorRepository;
import net.parkl.ocpp.repositories.OcppChargingProcessRepository;
import net.parkl.ocpp.repositories.TransactionStartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static net.parkl.ocpp.service.ErrorMessages.INVALID_CHARGE_BOX_ID_CONNECTOR_ID;
import static net.parkl.ocpp.service.ErrorMessages.INVALID_OCPP_CHARGING_PROCESS_ID;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChargingProcessService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChargingProcessService.class);

    private final OcppChargingProcessRepository chargingProcessRepo;

    private final ConnectorRepository connectorRepo;


    private final TransactionStartRepository transactionStartRepository;



    public OcppChargingProcess findOpenChargingProcessWithoutTransaction(String chargeBoxId, int connectorId) {
        Connector c = connectorRepo.findByChargeBoxIdAndConnectorId(chargeBoxId, connectorId);
        if (c == null) {
            throw new IllegalStateException(INVALID_CHARGE_BOX_ID_CONNECTOR_ID + chargeBoxId + "/" + connectorId);
        }
        return chargingProcessRepo.findByConnectorAndTransactionStartIsNullAndEndDateIsNull(c);
    }

    public OcppChargingProcess findOpenChargingProcess(String chargeBoxId, int connectorId) {
        Connector c = connectorRepo.findByChargeBoxIdAndConnectorId(chargeBoxId, connectorId);
        if (c == null) {
            throw new IllegalStateException(INVALID_CHARGE_BOX_ID_CONNECTOR_ID + chargeBoxId + "/" + connectorId);
        }
        return chargingProcessRepo.findByConnectorAndEndDateIsNull(c);
    }

    @Transactional
    public OcppChargingProcess createChargingProcess(String chargeBoxId,
                                                     int connectorId,
                                                     String idTag,
                                                     String licensePlate,
                                                     Float limitKwh,
                                                     Integer limitMinute) {
        Connector c = connectorRepo.findByChargeBoxIdAndConnectorId(chargeBoxId, connectorId);
        if (c == null) {
            throw new IllegalStateException(INVALID_CHARGE_BOX_ID_CONNECTOR_ID + chargeBoxId + "/" + connectorId);
        }
        LOGGER.info("Creating or updating OcppChargingProcess on {}/{} with id tag {} for: {}...", chargeBoxId, connectorId,
                idTag, licensePlate);
        OcppChargingProcess occupied = chargingProcessRepo.findByConnectorAndTransactionStartIsNullAndEndDateIsNull(c);
        if (occupied != null) {
            throw new IllegalStateException("Connector occupied: " + c.getConnectorId());
        }


        log.info("Creating new process for id tag {}...", idTag);
        OcppChargingProcess p = new OcppChargingProcess();
        p.setOcppChargingProcessId(UUID.randomUUID().toString());
        p.setConnector(c);
        p.setOcppTag(idTag);

        p.setLicensePlate(licensePlate);
        p.setLimitKwh(limitKwh);
        p.setLimitMinute(limitMinute);

        return chargingProcessRepo.save(p);
    }

    public OcppChargingProcess findOcppChargingProcess(String processId) {
        return chargingProcessRepo.findById(processId).orElse(null);
    }

    @Transactional
    public OcppChargingProcess stopChargingProcess(String processId, String error) {
        OcppChargingProcess cp = chargingProcessRepo.findById(processId).
                orElseThrow(() -> new IllegalStateException(INVALID_OCPP_CHARGING_PROCESS_ID + processId));

        if (cp.getEndDate() != null) {
            log.warn("OcppChargingProcess already ended: " + processId);
        } else {
            cp.setEndDate(LocalDateTime.now());
        }
        cp.setErrorCode(error);

        return chargingProcessRepo.save(cp);
    }


    @Transactional
    public OcppChargingProcess stopRequested(String processId) {
        OcppChargingProcess cp = chargingProcessRepo.findById(processId).
                orElseThrow(() -> new IllegalStateException(INVALID_OCPP_CHARGING_PROCESS_ID + processId));

        if (cp.getStopRequestDate() != null) {
            LOGGER.warn("OcppChargingProcess stop already requested: {}", processId);
            return cp;
        }

        cp.setStopRequestDate(LocalDateTime.now());
        return chargingProcessRepo.save(cp);
    }


    @Transactional
    public void stopRequestCancelled(String processId) {
        OcppChargingProcess cp = chargingProcessRepo.findById(processId).
                orElseThrow(() -> new IllegalStateException(INVALID_OCPP_CHARGING_PROCESS_ID + processId));

        cp.setStopRequestDate(null);
        chargingProcessRepo.save(cp);
    }

    public List<OcppChargingProcess> getActiveProcessesByChargeBox(String chargeBoxId) {
        return chargingProcessRepo.findActiveByChargeBoxId(chargeBoxId);
    }

    public List<OcppChargingProcess> findOpenChargingProcessesWithoutTransaction() {
        return chargingProcessRepo.findAllByTransactionStartIsNullAndEndDateIsNull();
    }


    public List<OcppChargingProcess> findOpenChargingProcessesWithLimitKwh() {
        return chargingProcessRepo.findAllByTransactionStartIsNotNullAndLimitKwhIsNotNullAndEndDateIsNull();
    }

    public List<OcppChargingProcess> findOpenChargingProcessesWithLimitMinute() {
        return chargingProcessRepo.findAllByTransactionStartIsNotNullAndLimitMinuteIsNotNullAndEndDateIsNull();
    }

    public OcppChargingProcess findByOcppTagAndConnectorAndEndDateIsNullAndTransactionIsNotNull(String rfidTag,
                                                                                                int connectorId,
                                                                                                String chargeBoxId) {

        Connector connector = connectorRepo.findByChargeBoxIdAndConnectorId(chargeBoxId, connectorId);
        if (connector == null) {
            throw new IllegalStateException("Invalid charge box id/connector id: " + chargeBoxId + "/" + connectorId);
        }
        return chargingProcessRepo.findByOcppTagAndConnectorAndEndDateIsNullAndTransactionStartIsNotNull(rfidTag, connector);
    }



    public OcppChargingProcess findByTransactionId(int transactionId) {
        TransactionStart transaction = transactionStartRepository
                .findById(transactionId).orElseThrow(() -> new IllegalStateException("Invalid transaction id"));
        return chargingProcessRepo.findByTransactionStart(transaction);
    }

    public OcppChargingProcess save(OcppChargingProcess process) {
        return chargingProcessRepo.save(process);
    }


    @Transactional
    public List<OcppChargingProcess> findWithoutTransactionForCleanup(int hoursBefore) {
        LocalDateTime dateTimeBefore = LocalDateTime.now().minusHours(hoursBefore);
        return chargingProcessRepo.findWithoutTransactionBefore(dateTimeBefore);

    }

    @Transactional
    public void deleteByChargingProcessId(String ocppChargingProcessId) {
        chargingProcessRepo.deleteById(ocppChargingProcessId);

    }

    @Transactional
    public void updateStopOnlyWhenCableRemoved(String externalChargeId, boolean value) {
        chargingProcessRepo.updateStopOnlyWhenCableRemoved(externalChargeId, value);
    }

    public List<OcppChargingProcess> findListByTransactionId(int transactionId) {
        TransactionStart transaction = transactionStartRepository
                .findById(transactionId).orElseThrow(() -> new IllegalStateException("Invalid transaction id"));
        return chargingProcessRepo.findListByTransactionStart(transaction);
    }

    @Transactional
    public OcppChargingProcess updateChargingProcessWithTransaction(String chargeBoxId, int connectorId, String idTag,
                                                                    TransactionStart transactionStart) {
        Connector c = connectorRepo.findByChargeBoxIdAndConnectorId(chargeBoxId, connectorId);
        if (c == null) {
            throw new IllegalStateException(INVALID_CHARGE_BOX_ID_CONNECTOR_ID + chargeBoxId + "/" + connectorId);
        }
        OcppChargingProcess existing = chargingProcessRepo.findByConnectorAndOcppTagAndEndDateIsNull(c, idTag);
        if (existing != null) {
            log.info("Setting transaction start for existing process: " + existing.getOcppChargingProcessId());
            existing.setTransactionStart(transactionStart);
            return chargingProcessRepo.save(existing);
        } else {
            throw new IllegalStateException("Charging process not found for transaction start: " + transactionStart.getTransactionPk());
        }
    }
}
