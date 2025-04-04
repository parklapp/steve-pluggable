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
package net.parkl.ocpp.service.cs.factory;

import com.google.common.base.Throwables;
import de.rwth.idsg.steve.repository.dto.InsertTransactionParams;
import de.rwth.idsg.steve.repository.dto.UpdateTransactionParams;
import net.parkl.ocpp.entities.*;
import org.joda.time.DateTime;

import java.time.ZoneId;

public class TransactionFactory {

    public static TransactionStop createTransactionStop(UpdateTransactionParams params) {
        TransactionStop stop = new TransactionStop();
        decorateTransactionStop(stop, params);
        return stop;
    }

    public static TransactionStop createTransactionStopForCleanup(int transactionId, DateTime eventTimeStamp) {
        TransactionStop stop = new TransactionStop();
        stop.setTransactionStopId(createTransactionStopId(transactionId, eventTimeStamp));
        stop.setEventActor(TransactionStopEventActor.manual);
        stop.setStopTimestamp(eventTimeStamp.toDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        stop.setStopReason("Cleanup");
        return stop;
    }

    public static TransactionStopFailed createTransactionStopFailed(UpdateTransactionParams params, Exception exception) {
        TransactionStopFailed failed = new TransactionStopFailed();
        decorateTransactionStop(failed, params);
        failed.setFailReason(Throwables.getStackTraceAsString(exception));
        return failed;
    }

    public static TransactionStopFailed createTransactionStopFailedForCleanup(int transactionId, Exception exception) {
        TransactionStopFailed failed = new TransactionStopFailed();

        DateTime eventTimeStamp = DateTime.now();
        failed.setTransactionStopId(createTransactionStopId(transactionId, eventTimeStamp));
        failed.setEventActor(TransactionStopEventActor.manual);
        failed.setStopReason("Cleanup");
        failed.setFailReason(Throwables.getStackTraceAsString(exception));
        return failed;
    }

    public static TransactionStopId createTransactionStopId(int transactionId, DateTime stop) {
        TransactionStopId transactionStopId = new TransactionStopId();
        transactionStopId.setTransactionPk(transactionId);
        transactionStopId.setEventTimestamp(stop.toDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime());
        return transactionStopId;
    }

    public static TransactionStart createTransactionStart(Connector connector, InsertTransactionParams params) {
        TransactionStart start = new TransactionStart();
        start.setConnector(connector);
        start.setOcppTag(params.getIdTag());
        if (params.getStartTimestamp() != null) {
            start.setStartTimestamp(params.getStartTimestamp().toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        start.setStartValue(params.getStartMeterValue());
        if (params.getEventTimestamp() != null) {
            start.setEventTimestamp(params.getEventTimestamp().toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        return start;
    }

    private static void decorateTransactionStop(TransactionStop transactionStop, UpdateTransactionParams params) {
        transactionStop.setTransactionStopId(createTransactionStopId(params.getTransactionId(), params.getEventTimestamp()));
        transactionStop.setEventActor(params.getEventActor());
        if (params.getStopTimestamp() != null) {
            transactionStop.setStopTimestamp(params.getStopTimestamp().toDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
        }
        transactionStop.setStopValue(params.getStopMeterValue());
        transactionStop.setStopReason(params.getStopReason());
    }
}
