package com.mobilabsolutions.payment.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.ZoneId;

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
public final class CCMaskValidatorImpl implements ConstraintValidator<CCMaskValidator, String> {

    @Override
    public void initialize(final CCMaskValidator constraintAnnotation) {}

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        int count = 0;
        for (int i = 0, len = value.length(); i < len; i++) {
            if (Character.isDigit(value.charAt(i))) {
                count++;
            }
        }
        return (count == 4);
    }
}
