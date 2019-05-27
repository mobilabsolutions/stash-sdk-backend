package com.mobilabsolutions.payment.validation;

import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
public class DateValidatorImpl implements ConstraintValidator<DateValidator, String> {

    private String pattern;

    @Override
    public void initialize(DateValidator constraintAnnotation) {
        pattern = constraintAnnotation.pattern();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        else {
            SimpleDateFormat date = new SimpleDateFormat(pattern);
            date.setLenient(false);
            try {
                date.parse(value);
                return true;
            } catch (ParseException e) {
                return false;
            }
        }
    }
}
