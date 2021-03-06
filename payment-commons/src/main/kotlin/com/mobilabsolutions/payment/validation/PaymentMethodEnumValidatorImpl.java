/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
public class PaymentMethodEnumValidatorImpl implements ConstraintValidator<PaymentMethodEnumValidator, String> {

    private List<String> valueList = new ArrayList<>();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || valueList.contains(value.toUpperCase());
    }

    @Override
    public void initialize(PaymentMethodEnumValidator constraintAnnotation) {
        Class<? extends Enum<?>> enumClass = constraintAnnotation.PaymentMethod();

        @SuppressWarnings("rawtypes")
        Enum[] enumValArr = enumClass.getEnumConstants();

        for(@SuppressWarnings("rawtypes")
            Enum enumVal : enumValArr) {
            valueList.add(enumVal.toString().toUpperCase());
        }
    }
}
