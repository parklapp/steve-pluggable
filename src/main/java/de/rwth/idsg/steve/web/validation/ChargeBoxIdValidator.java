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
package de.rwth.idsg.steve.web.validation;

import com.google.common.base.Strings;
import de.rwth.idsg.steve.SteveConfiguration;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.regex.Pattern;

/**
 * @author Sevket Goekay <sevketgokay@gmail.com>
 * @since 21.01.2016
 */
@RequiredArgsConstructor
public class ChargeBoxIdValidator implements ConstraintValidator<ChargeBoxId, String> {

    private static final String REGEX = "[^=/()<>]*";
    private static Pattern pattern = null;

    private static Pattern getPattern(String regex) {
        if (pattern == null) {
            pattern = Pattern.compile(regex);
        }
        return pattern;
    }

    private final SteveConfiguration config;

    @Override
    public void initialize(ChargeBoxId idTag) {
        // No-op
    }

    @Override
    public boolean isValid(String string, ConstraintValidatorContext constraintValidatorContext) {
        if (string == null) {
            return true; // null is valid, because it is another constraint's responsibility
        }
        return isValid(string);
    }

    public boolean isValid(String str) {
        if (Strings.isNullOrEmpty(str)) {
            return false;
        }

        String str1 = str.strip();
        if (!str1.equals(str)) {
            return false;
        }
        return getPattern(getRegexToUse()).matcher(str).matches();
    }

    private String getRegexToUse() {
        String regexFromConfig = config.getOcpp().getChargeBoxIdValidationRegex();
        return Strings.isNullOrEmpty(regexFromConfig) ? REGEX : regexFromConfig;
    }
}
