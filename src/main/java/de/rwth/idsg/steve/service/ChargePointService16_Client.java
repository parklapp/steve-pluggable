/*
 * SteVe - SteckdosenVerwaltung - https://github.com/steve-community/steve
 * Copyright (C) 2013-2024 SteVe Community Team
 * All Rights Reserved.
 *
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
package de.rwth.idsg.steve.service;

import de.rwth.idsg.steve.SteveException;
import de.rwth.idsg.steve.ocpp.*;
import de.rwth.idsg.steve.ocpp.task.ClearChargingProfileTask;
import de.rwth.idsg.steve.ocpp.task.GetCompositeScheduleTask;
import de.rwth.idsg.steve.ocpp.task.SetChargingProfileTask;
import de.rwth.idsg.steve.ocpp.task.TriggerMessageTask;
import de.rwth.idsg.steve.repository.dto.ChargingProfile;
import de.rwth.idsg.steve.service.dto.EnhancedSetChargingProfileParams;
import de.rwth.idsg.steve.web.dto.ocpp.ClearChargingProfileParams;
import de.rwth.idsg.steve.web.dto.ocpp.GetCompositeScheduleParams;
import de.rwth.idsg.steve.web.dto.ocpp.SetChargingProfileParams;
import de.rwth.idsg.steve.web.dto.ocpp.TriggerMessageParams;
import lombok.extern.slf4j.Slf4j;
import net.parkl.ocpp.service.cs.ChargingProfileService;
import ocpp.cp._2015._10.ChargingProfilePurposeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author Sevket Goekay <sevketgokay@gmail.com>
 * @since 13.03.2018
 */
@Slf4j
@Service
@Qualifier("ChargePointService16_Client")
public class ChargePointService16_Client extends ChargePointService15_Client implements IChargePointService16_Client {

    @Autowired private ChargePointService16_InvokerImpl invoker16;
    @Autowired private ChargingProfileService chargingProfileService;

    @Override
    protected OcppVersion getVersion() {
        return OcppVersion.V_16;
    }

    @Override
    protected ChargePointService12_Invoker getOcpp12Invoker() {
        return invoker16;
    }

    @Override
    protected ChargePointService15_Invoker getOcpp15Invoker() {
        return invoker16;
    }

    protected ChargePointService16_Invoker getOcpp16Invoker() {
        return invoker16;
    }

    // -------------------------------------------------------------------------
    // Multiple Execution - since OCPP 1.6
    // -------------------------------------------------------------------------

    public int triggerMessage(TriggerMessageParams params) {
        TriggerMessageTask task = new TriggerMessageTask(persistentTaskService, getVersion(), params);

        Integer taskId = taskStore.add(task);

        BackgroundService.with(executorService)
                         .forEach(task.getParams().getChargePointSelectList())
                         .execute(c -> getOcpp16Invoker().triggerMessage(c, task));

        return taskId;
    }

    public int setChargingProfile(SetChargingProfileParams params) {
        ChargingProfile.Details details = chargingProfileService.getDetails(params.getChargingProfilePk());

        checkAdditionalConstraints(params, details);

        EnhancedSetChargingProfileParams enhancedParams = new EnhancedSetChargingProfileParams(params, details);
        SetChargingProfileTask task = new SetChargingProfileTask(persistentTaskService,
                getVersion(), enhancedParams, chargingProfileService);

        Integer taskId = taskStore.add(task);

        BackgroundService.with(executorService)
                         .forEach(task.getParams().getChargePointSelectList())
                         .execute(c -> getOcpp16Invoker().setChargingProfile(c, task));

        return taskId;
    }

    public int clearChargingProfile(ClearChargingProfileParams params) {
        ClearChargingProfileTask task = new ClearChargingProfileTask(persistentTaskService,
                getVersion(), params, chargingProfileService);

        Integer taskId = taskStore.add(task);

        BackgroundService.with(executorService)
                         .forEach(task.getParams().getChargePointSelectList())
                         .execute(c -> getOcpp16Invoker().clearChargingProfile(c, task));

        return taskId;
    }

    public int getCompositeSchedule(GetCompositeScheduleParams params) {
        GetCompositeScheduleTask task = new GetCompositeScheduleTask(persistentTaskService,
                getVersion(), params);

        Integer taskId = taskStore.add(task);

        BackgroundService.with(executorService)
                         .forEach(task.getParams().getChargePointSelectList())
                         .execute(c -> getOcpp16Invoker().getCompositeSchedule(c, task));

        return taskId;
    }

    /**
     * Do some additional checks defined by OCPP spec, which cannot be captured with javax.validation
     */
    private static void checkAdditionalConstraints(SetChargingProfileParams params, ChargingProfile.Details details) {
        ChargingProfilePurposeType purpose = ChargingProfilePurposeType.fromValue(details.getProfile().getChargingProfilePurpose());

        if (ChargingProfilePurposeType.CHARGE_POINT_MAX_PROFILE == purpose
                && params.getConnectorId() != null
                && params.getConnectorId() != 0) {
            throw new SteveException("ChargePointMaxProfile can only be set at Charge Point ConnectorId 0");
        }

        if (ChargingProfilePurposeType.TX_PROFILE == purpose
                && params.getConnectorId() != null
                && params.getConnectorId() < 1) {
            throw new SteveException("TxProfile should only be set at Charge Point ConnectorId > 0");
        }

    }
}
