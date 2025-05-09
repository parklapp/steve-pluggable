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
package net.parkl.ocpp.repositories;

import net.parkl.ocpp.entities.Connector;
import net.parkl.ocpp.entities.TransactionStart;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;

import java.util.List;

public interface TransactionStartRepository extends CrudRepository<TransactionStart, Integer> {
    @Query("SELECT OBJECT(t) FROM TransactionStart AS t WHERE t.connector.chargeBoxId=?1 AND t.connector.connectorId=?2 AND t.startTimestamp>?3 ORDER BY t.startTimestamp")
    List<TransactionStart> findNextTransactions(String chargeBoxId, int connectorId, LocalDateTime startTimestamp, Pageable pageable);

    @Query("SELECT OBJECT(t) FROM TransactionStart AS t WHERE t.connector=?1 AND t.ocppTag=?2 AND t.startTimestamp=?3 AND t.startValue=?4")
    TransactionStart findByConnectorAndIdTagAndStartValues(Connector c, String idTag, LocalDateTime startDate, String startValue);

    TransactionStart findFirstByConnectorAndOcppTagOrderByStartTimestampDesc(Connector c, String idTag);

}
