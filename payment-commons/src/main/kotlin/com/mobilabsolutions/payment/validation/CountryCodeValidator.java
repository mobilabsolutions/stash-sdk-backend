package com.mobilabsolutions.payment.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
public final class CountryCodeValidator implements ConstraintValidator<CountryCode, String> {

    private static Set<String> ISO_COUNTRY_CODES = Arrays.stream(Locale.getISOCountries()).map(String::toLowerCase)
            .collect(Collectors.toSet());

    @Override
    public void initialize(CountryCode constraintAnnotation) {
        // do nothing
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || ISO_COUNTRY_CODES.contains(value.toLowerCase());
    }

}
