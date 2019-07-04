/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.ZoneId;

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
public final class TimeZoneValidatorImpl implements ConstraintValidator<TimeZoneValidator, String> {

    @Override
    public void initialize(final TimeZoneValidator constraintAnnotation) {}

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        return value == null || ZoneId.getAvailableZoneIds().contains(value);
    }
}
