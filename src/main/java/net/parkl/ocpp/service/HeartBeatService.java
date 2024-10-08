package net.parkl.ocpp.service;

import de.rwth.idsg.steve.ocpp.*;
import de.rwth.idsg.steve.ocpp.task.ChangeConfigurationTask;
import de.rwth.idsg.steve.repository.dto.ChargePointSelect;
import de.rwth.idsg.steve.service.ChargePointHelperService;
import de.rwth.idsg.steve.web.dto.ocpp.ChangeConfigurationParams;
import de.rwth.idsg.steve.web.dto.ocpp.ConfigurationKeyEnum;
import lombok.extern.slf4j.Slf4j;
import net.parkl.ocpp.service.cluster.PersistentTaskService;
import net.parkl.ocpp.service.cluster.PersistentTaskServiceImpl;
import net.parkl.ocpp.service.config.AdvancedChargeBoxConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class HeartBeatService {

    private final ChargePointService15_InvokerImpl service15Invoker;
    private final ChargePointService16_InvokerImpl service16Invoker;
    private final ChargePointHelperService chargePointHelperService;
    private final AdvancedChargeBoxConfiguration chargeBoxConfiguration;

    private final PersistentTaskService persistentTaskService;
    private final int heartBeatIntervalInSecs;

    @Autowired
    public HeartBeatService(ChargePointService15_InvokerImpl chargePointService15_InvokerImpl,
                            ChargePointService16_InvokerImpl chargePointService16_InvokerImpl,
                            ChargePointHelperService chargePointHelperService,
                            AdvancedChargeBoxConfiguration chargeBoxConfiguration,
                            PersistentTaskService persistentTaskService,
                            @Value("${heartbeat.interval.secs:60}") int heartBeatIntervalInSecs) {

        this.service15Invoker = chargePointService15_InvokerImpl;
        this.service16Invoker = chargePointService16_InvokerImpl;
        this.chargePointHelperService = chargePointHelperService;
        this.chargeBoxConfiguration = chargeBoxConfiguration;
        this.persistentTaskService = persistentTaskService;
        this.heartBeatIntervalInSecs = heartBeatIntervalInSecs;
    }

    public void changeConfig(OcppProtocol ocppProtocol, String chargeBoxId) {
        if (chargeBoxConfiguration.skipHeartBeatConfig(chargeBoxId)) {
            log.warn("HeartBeat change config skipped for charge box: {}", chargeBoxId);
            return;
        }
        List<ChargePointSelect> chargePoints;
        ChargePointSelect chargePointSelect;
        ChangeConfigurationTask task;

        if (ocppProtocol.getVersion() == OcppVersion.V_16) {
            log.info("Setting heartbeat interval secs to {} on OCPPv16", heartBeatIntervalInSecs);
            chargePoints = chargePointHelperService.getChargePoints(OcppVersion.V_16);
            chargePointSelect = filter(chargePoints, chargeBoxId);
            task = new ChangeConfigurationTask(persistentTaskService, OcppVersion.V_16, getParams(chargePointSelect));
            service16Invoker.changeConfiguration(chargePointSelect, task);
            log.info("Successfully changed heartbeat interval on OCPPv16");

        } else if (ocppProtocol.getVersion() == OcppVersion.V_15) {
            log.info("Setting heartbeat interval secs to {} on OCPPv15", heartBeatIntervalInSecs);
            chargePoints = chargePointHelperService.getChargePoints(OcppVersion.V_15);
            chargePointSelect = filter(chargePoints, chargeBoxId);
            task = new ChangeConfigurationTask(persistentTaskService, OcppVersion.V_15, getParams(chargePointSelect));
            service15Invoker.changeConfiguration(chargePointSelect, task);
            log.info("Successfully changed heartbeat interval on OCPPv15");
        }
    }

    private ChargePointSelect filter(List<ChargePointSelect> list, String chargeBoxId) {
        return list.stream().filter(cp -> cp.getChargeBoxId().equals(chargeBoxId)).findAny().orElse(null);
    }

    private ChangeConfigurationParams getParams(ChargePointSelect chargePointSelect) {
        ChangeConfigurationParams params = new ChangeConfigurationParams();
        params.setConfKey(ConfigurationKeyEnum.HeartBeatInterval.value());
        params.setValue(String.valueOf(heartBeatIntervalInSecs));
        params.setChargePointSelectList(Collections.singletonList(chargePointSelect));
        return params;
    }
}
