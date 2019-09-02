package com.mobilabsolutions.payment.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
public class ReportTypeValidatorImpl implements ConstraintValidator<ReportTypeValidator, String> {

    private List<String> valueList = new ArrayList<>();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || valueList.contains(value.toUpperCase());
    }

    @Override
    public void initialize(ReportTypeValidator constraintAnnotation) {
        Class<? extends Enum<?>> enumClass = constraintAnnotation.ReportType();

        @SuppressWarnings("rawtypes")
        Enum[] enumValArr = enumClass.getEnumConstants();

        for(@SuppressWarnings("rawtypes")
            Enum enumVal : enumValArr) {
            valueList.add(enumVal.toString().toUpperCase());
        }
    }
}
