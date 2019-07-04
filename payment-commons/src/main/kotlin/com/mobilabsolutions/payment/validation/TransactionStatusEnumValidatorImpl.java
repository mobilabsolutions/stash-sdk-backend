/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

public class TransactionStatusEnumValidatorImpl implements ConstraintValidator<TransactionStatusEnumValidator, String> {

    private List<String> valueList = new ArrayList<>();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || valueList.contains(value.toUpperCase());
    }

    @Override
    public void initialize(TransactionStatusEnumValidator constraintAnnotation) {
        Class<? extends Enum<?>> enumClass = constraintAnnotation.TransactionStatus();

        @SuppressWarnings("rawtypes")
        Enum[] enumValArr = enumClass.getEnumConstants();

        for(@SuppressWarnings("rawtypes")
            Enum enumVal : enumValArr) {
            valueList.add(enumVal.toString().toUpperCase());
        }
    }
}
