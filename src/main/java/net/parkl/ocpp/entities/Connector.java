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
package net.parkl.ocpp.entities;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ocpp_connector", uniqueConstraints =
@UniqueConstraint(name = "connector_cbid_cid_UNIQUE", columnNames = {"charge_box_id", "connector_id"}))
@Getter
@Setter
public class Connector implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "connector_pk")
    private int connectorPk;

    @Column(name = "charge_box_id", nullable = false)
    private String chargeBoxId;

    @Column(name = "connector_id", nullable = false)
    private int connectorId;

}