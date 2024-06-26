/*
 * SteVe - SteckdosenVerwaltung - https://github.com/steve-community/steve
 * Copyright (C) 2013-2019 RWTH Aachen University - Information Systems - Intelligent Distributed Systems Group (IDSG).
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
package de.rwth.idsg.steve.repository.dto;


import lombok.Builder;
import lombok.Getter;
import net.parkl.ocpp.entities.TransactionStopEventActor;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;

/**
 *
 * @author Sevket Goekay <sevketgokay@gmail.com>
 *
 */
@Getter
@Builder
@ToString
public final class Transaction {
    private final int id, connectorId, chargeBoxPk, ocppTagPk;
    private final String chargeBoxId, ocppIdTag, startTimestampFormatted, startValue;
    private final DateTime startTimestamp;

    @Nullable private final String stopTimestampFormatted;
    @Nullable private final String stopValue;
    @Nullable private final String stopReason; // new in OCPP 1.6
    @Nullable private final DateTime stopTimestamp;
    @Nullable private final TransactionStopEventActor stopEventActor;
}
