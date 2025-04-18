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
package net.parkl.ocpp.service.cs;


import de.rwth.idsg.steve.SteveException;
import de.rwth.idsg.steve.ocpp.OcppProtocol;
import de.rwth.idsg.steve.ocpp.OcppTransport;
import de.rwth.idsg.steve.repository.dto.ChargePoint;
import de.rwth.idsg.steve.repository.dto.ChargePoint.Overview;
import de.rwth.idsg.steve.repository.dto.ChargePointSelect;
import de.rwth.idsg.steve.repository.dto.ConnectorStatus;
import de.rwth.idsg.steve.repository.dto.UpdateChargeboxParams;
import de.rwth.idsg.steve.utils.DateTimeUtils;
import de.rwth.idsg.steve.web.dto.ChargePointForm;
import de.rwth.idsg.steve.web.dto.ChargePointQueryForm;
import de.rwth.idsg.steve.web.dto.ConnectorStatusForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.parkl.ocpp.entities.ConnectorLastStatus;
import net.parkl.ocpp.entities.OcppAddress;
import net.parkl.ocpp.entities.OcppChargeBox;
import net.parkl.ocpp.repositories.*;
import ocpp.cs._2010._08.RegistrationStatus;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 14.08.2014
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChargePointServiceImpl implements ChargePointService {

    private final OcppChargeBoxRepository chargeBoxRepository;
    private final OcppAddressRepository addressRepository;
    private final ConnectorRepository connectorRepository;
    private final ConnectorLastStatusRepository connectorLastStatusRepository;
    private final ChargePointCriteriaRepository chargePointCriteriaRepository;
    private final AddressService addressService;


    @Override
    public Optional<String> getRegistrationStatus(String chargeBoxId) {
        String status = chargeBoxRepository.findChargeBoxRegistrationStatus(chargeBoxId);

        return Optional.ofNullable(status);
    }

    @Override
    public List<ChargePointSelect> getChargePointSelect(OcppProtocol protocol, List<String> inStatusFilter, List<String> chargeBoxIdFilter) {
        final OcppTransport transport = protocol.getTransport();

        List<OcppChargeBox> result = null;
        if (chargeBoxIdFilter != null && !chargeBoxIdFilter.isEmpty()) {
            result = chargeBoxRepository.findByOcppProtocolAndRegistrationStatusesAndChargeBoxIdIn(protocol.getCompositeValue(),
                    inStatusFilter, chargeBoxIdFilter);
        } else {
            result = chargeBoxRepository.findByOcppProtocolAndRegistrationStatuses(protocol.getCompositeValue(),
                    inStatusFilter);
        }
        List<ChargePointSelect> ret = new ArrayList<>();
        for (OcppChargeBox r : result) {
            ret.add(new ChargePointSelect(transport, r.getChargeBoxId(), r.getEndpointAddress()));
        }
        return ret;
    }

    @Override
    public List<String> getChargeBoxIds() {
        return chargeBoxRepository.findAllChargeBoxIds();
    }

    @Override
    public Map<String, Integer> getChargeBoxIdPkPair(List<String> chargeBoxIdList) {
        List<OcppChargeBox> result = chargeBoxRepository.findByChargeBoxIdIn(chargeBoxIdList);
        Map<String, Integer> ret = new HashMap<>();
        for (OcppChargeBox r : result) {
            ret.put(r.getChargeBoxId(), r.getChargeBoxPk());
        }
        return ret;
    }

    @Override
    public List<ChargePoint.Overview> getOverview(ChargePointQueryForm form) {
        return toChargePointOverviewList(chargePointCriteriaRepository.getOverviewInternal(form));
    }

    private List<Overview> toChargePointOverviewList(List<OcppChargeBox> list) {
        List<Overview> ret = new ArrayList<>();
        for (OcppChargeBox r : list) {
            ret.add(ChargePoint.Overview.builder()
                    .chargeBoxPk(r.getChargeBoxPk())
                    .chargeBoxId(r.getChargeBoxId())
                    .description(r.getDescription())
                    .ocppProtocol(r.getOcppProtocol())
                    .lastHeartbeatTimestampDT(r.getLastHeartbeatTimestamp() != null ? new DateTime(r.getLastHeartbeatTimestamp()) : null)
                    .lastHeartbeatTimestamp(DateTimeUtils.humanize(r.getLastHeartbeatTimestamp() != null ? new DateTime(r.getLastHeartbeatTimestamp()) : null))
                    .build());
        }
        return ret;
    }

    @Override
    public ChargePoint.Details getDetails(int chargeBoxPk) {
        OcppChargeBox cbr = chargeBoxRepository.findById(chargeBoxPk).
                orElseThrow(() -> new SteveException("Charge point not found"));


        return new ChargePoint.Details(cbr, cbr.getAddress());
    }

    @Override
    public List<ConnectorStatus> getChargePointConnectorStatus(ConnectorStatusForm form) {
        Iterable<ConnectorLastStatus> result = connectorLastStatusRepository.findAll();


        Map<String, OcppChargeBox> chargeBoxMap = new HashMap<>();
        Iterable<OcppChargeBox> cbAll = chargeBoxRepository.findAll();
        for (OcppChargeBox c : cbAll) {
            chargeBoxMap.put(c.getChargeBoxId(), c);
        }

        List<ConnectorStatus> statusList = new ArrayList<>();

        for (ConnectorLastStatus r : result) {
            OcppChargeBox cb = chargeBoxMap.get(r.getChargeBoxId());
            if (cb != null) {
                // https://github.com/steve-community/steve/issues/691
                boolean chargeBoxStatusCondition = cb.getRegistrationStatus().equals(RegistrationStatus.ACCEPTED.value());

                boolean statusCondition = form == null || form.getStatus() == null ||
                        form.getStatus().equals(r.getStatus());

                boolean chargeBoxCondition = form == null || form.getChargeBoxId() == null ||
                        form.getChargeBoxId().equals(r.getChargeBoxId());
                if (chargeBoxStatusCondition && statusCondition && chargeBoxCondition) {
                    ConnectorStatus s = ConnectorStatus.builder()
                            .chargeBoxPk(cb.getChargeBoxPk())
                            .chargeBoxId(r.getChargeBoxId())
                            .connectorId(r.getConnectorId())
                            .timeStamp(r.getStatusTimestamp() != null ? DateTimeUtils.humanize(new DateTime(r.getStatusTimestamp())) : null)
                            .statusTimestamp(r.getStatusTimestamp() != null ? new DateTime(r.getStatusTimestamp()) : null)
                            .status(r.getStatus())
                            .errorCode(r.getErrorCode())
                            .build();
                    statusList.add(s);
                }
            } else {
                log.warn("Invalid charge box id: {}" + r.getChargeBoxId());
            }
        }

        return statusList;
    }

    @Override
    public List<Integer> getNonZeroConnectorIds(String chargeBoxId) {
        return connectorRepository.findNonZeroConnectorIdsByChargeBoxId(chargeBoxId);
    }

    @Override
    @Transactional
    public void addChargePointList(List<String> chargeBoxIdList) {
        for (String cbId : chargeBoxIdList) {
            OcppChargeBox cb = new OcppChargeBox();
            cb.setChargeBoxId(cbId);
            cb.setInsertConnectorStatusAfterTransactionMsg(false);
            chargeBoxRepository.save(cb);
        }
    }

    @Override
    @Transactional
    public void addChargePoint(ChargePointForm form) {
        OcppAddress addr = addressService.saveAddress(form.getAddress());

        OcppChargeBox cb = new OcppChargeBox();
        fillChargeBox(cb, form, addr);
        chargeBoxRepository.save(cb);

    }

    @Override
    @Transactional
    public void updateChargePoint(ChargePointForm form) {
        OcppChargeBox cb = chargeBoxRepository.findById(form.getChargeBoxPk()).
                orElseThrow(() -> new IllegalArgumentException("Invalid charge box PK: " + form.getChargeBoxPk()));

        try {
            OcppAddress addr = addressService.saveAddress(form.getAddress());
            fillChargeBox(cb, form, addr);
            chargeBoxRepository.save(cb);
        } catch (Exception e) {

            throw new SteveException("Failed to update the charge point with chargeBoxId '%s'",
                    form.getChargeBoxId(), e);
        }


    }

    private void fillChargeBox(OcppChargeBox cb, ChargePointForm form, OcppAddress addr) {
        cb.setAddress(addr);
        cb.setChargeBoxId(form.getChargeBoxId());
        cb.setDescription(form.getDescription());
        cb.setLocationLatitude(form.getLocationLatitude());
        cb.setLocationLongitude(form.getLocationLongitude());
        cb.setNote(form.getNote());
        cb.setRegistrationStatus(form.getRegistrationStatus());
    }

    @Override
    @Transactional
    public void deleteChargePoint(int chargeBoxPk) {
        OcppChargeBox cb = chargeBoxRepository.findById(chargeBoxPk).
                orElseThrow(() -> new IllegalArgumentException("Invalid charge box PK: " + chargeBoxPk));


        try {
            OcppAddress addr = cb.getAddress();


            chargeBoxRepository.delete(cb);
            addressRepository.delete(addr);
        } catch (Exception e) {
            throw new SteveException("Failed to delete the charge point", e);
        }

    }

    @Override
    @Transactional
    public void updateChargeboxHeartbeat(String chargeBoxId, DateTime now) {
        chargeBoxRepository.updateChargeBoxLastHeartbeat(chargeBoxId, now.toDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
    }

    @Override
    @Transactional
    public void updateEndpointAddress(String chargeBoxId, String endpointAddress) {
        OcppChargeBox cb = chargeBoxRepository.findByChargeBoxId(chargeBoxId);
        if (cb == null) {
            throw new IllegalArgumentException("Invalid charge box id: " + chargeBoxId);
        }
        cb.setEndpointAddress(endpointAddress);
        chargeBoxRepository.save(cb);

    }

    @Override
    @Transactional
    public void updateChargebox(UpdateChargeboxParams p) {
        OcppChargeBox cb = chargeBoxRepository.findByChargeBoxId(p.getChargeBoxId());
        if (cb == null) {
            log.error("The chargebox '{}' is NOT registered and its boot NOT acknowledged.", p.getChargeBoxId());
            return;
        }
        cb.setOcppProtocol(p.getOcppProtocol().getCompositeValue());
        cb.setChargePointVendor(p.getVendor());
        cb.setChargePointModel(p.getModel());
        cb.setChargePointSerialNumber(p.getPointSerial());
        cb.setChargeBoxSerialNumber(p.getBoxSerial());
        cb.setFwVersion(p.getFwVersion());
        cb.setIccid(p.getIccid());
        cb.setImsi(p.getImsi());
        cb.setMeterType(p.getMeterType());
        cb.setMeterSerialNumber(p.getMeterSerial());
        if (p.getHeartbeatTimestamp() != null) {
            cb.setLastHeartbeatTimestamp(p.getHeartbeatTimestamp().toDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
        }

        chargeBoxRepository.save(cb);
        log.info("The chargebox '{}' is registered and its boot acknowledged.", p.getChargeBoxId());
    }

    @Override
    @Transactional
    public void updateChargeboxFirmwareStatus(String chargeBoxId, String status) {
        OcppChargeBox cb = chargeBoxRepository.findByChargeBoxId(chargeBoxId);
        if (cb == null) {
            throw new IllegalArgumentException("Invalid charge box id: " + chargeBoxId);
        }
        cb.setFwUpdateStatus(status);
        cb.setFwUpdateTimestamp(LocalDateTime.now());
        chargeBoxRepository.save(cb);

    }


    @Override
    @Transactional
    public void updateChargeboxDiagnosticsStatus(String chargeBoxId, String status) {
        OcppChargeBox cb = chargeBoxRepository.findByChargeBoxId(chargeBoxId);
        if (cb == null) {
            throw new IllegalArgumentException("Invalid charge box id: " + chargeBoxId);
        }
        cb.setDiagnosticsStatus(status);
        cb.setDiagnosticsTimestamp(LocalDateTime.now());
        chargeBoxRepository.save(cb);
    }

    @Override
    public List<OcppChargeBox> findAllChargePoints() {
        return chargeBoxRepository.findAll();
    }

    @Override
    public OcppChargeBox findByChargeBoxId(String chargeBoxId) {
        return chargeBoxRepository.findByChargeBoxId(chargeBoxId);
    }

    public boolean shouldInsertConnectorStatusAfterTransactionMsg(String chargeBoxId) {
        OcppChargeBox cb = chargeBoxRepository.findByChargeBoxId(chargeBoxId);
        return cb != null && cb.insertConnectorStatusAfterTransactionMsg();
    }

    @Override
    public Map<String, OcppChargeBox> getIdChargeBoxMap() {
        Iterable<OcppChargeBox> boxesAll = chargeBoxRepository.findAll();
        Map<String, OcppChargeBox> boxMap = new HashMap<>();
        for (OcppChargeBox box : boxesAll) {
            boxMap.put(box.getChargeBoxId(), box);
        }
        return boxMap;
    }

    @Override
    public List<OcppChargeBox> getAllChargeBoxes() {
        return chargeBoxRepository.findAll();
    }

    @Override
    @Transactional
    public void updateOcppProtocol(String chargeBoxId, OcppProtocol protocol) {
        chargeBoxRepository.updateOcppProtocol(chargeBoxId, protocol.getCompositeValue());
    }
}
