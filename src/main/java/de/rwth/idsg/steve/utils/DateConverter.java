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
package de.rwth.idsg.steve.utils;

import org.joda.time.LocalDate;

import java.sql.Date;
import java.time.LocalDateTime;

/**
 * @author Sevket Goekay <sevketgokay@gmail.com>
 * @since 25.11.2015
 */
public class DateConverter {
    public static LocalDate from(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        } else {
            return new LocalDate(dateTime.getYear(), dateTime.getMonthValue(), dateTime.getDayOfMonth());
        }
    }


    public static LocalDate from(Date sqlDate) {
        if (sqlDate == null) {
            return null;
        } else {
            return new LocalDate(sqlDate.getTime());
        }
    }

    public static Date to(LocalDate jodaDate) {
        if (jodaDate == null) {
            return null;
        } else {
            return new Date(jodaDate.toDateTimeAtStartOfDay().getMillis());
        }
    }


}
