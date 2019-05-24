package com.mobilabsolutions.payment.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */

public class PaymentServiceProviderEnumValidatorImpl implements ConstraintValidator<PaymentServiceProviderEnumValidator, String> {

    List<String> valueList = null;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(!valueList.contains(value.toUpperCase())) {
            return false;
        }
        return true;
    }

    @Override
    public void initialize(PaymentServiceProviderEnumValidator constraintAnnotation) {
        valueList = new ArrayList<String>();
        Class<? extends Enum<?>> enumClass = constraintAnnotation.PaymentServiceProvider();

        @SuppressWarnings("rawtypes")
        Enum[] enumValArr = enumClass.getEnumConstants();

        for(@SuppressWarnings("rawtypes")
            Enum enumVal : enumValArr) {
            valueList.add(enumVal.toString().toUpperCase());
        }

    }
}
