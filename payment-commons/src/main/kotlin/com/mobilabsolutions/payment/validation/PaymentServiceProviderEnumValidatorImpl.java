package com.mobilabsolutions.payment.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */

public class PaymentServiceProviderEnumValidatorImpl implements ConstraintValidator<PaymentServiceProviderEnumValidator, String> {

    private List<String> valueList = new ArrayList<>();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return valueList.contains(value.toUpperCase());
    }

    @Override
    public void initialize(PaymentServiceProviderEnumValidator constraintAnnotation) {
        Class<? extends Enum<?>> enumClass = constraintAnnotation.PaymentServiceProvider();

        @SuppressWarnings("rawtypes")
        Enum[] enumValArr = enumClass.getEnumConstants();

        for(@SuppressWarnings("rawtypes")
            Enum enumVal : enumValArr) {
            valueList.add(enumVal.toString().toUpperCase());
        }

    }
}
