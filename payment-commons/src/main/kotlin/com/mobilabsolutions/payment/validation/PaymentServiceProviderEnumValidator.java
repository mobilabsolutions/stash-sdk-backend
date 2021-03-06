/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Documented
@Constraint(validatedBy = PaymentServiceProviderEnumValidatorImpl.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, PARAMETER})
public @interface PaymentServiceProviderEnumValidator {

    Class<? extends Enum<?>> PaymentServiceProvider();

    String message() default "Invalid payment service provider.";

    Class<?>[]groups() default {};

    Class<? extends Payload>[]payload() default {};
}
