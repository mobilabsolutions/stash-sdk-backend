/*
 * Copyright © MobiLab Solutions GmbH
 */

package com.mobilabsolutions.payment.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */
@Documented
@Constraint(validatedBy = TransactionActionEnumValidatorImpl.class)
@Retention(RUNTIME)
@Target({FIELD, PARAMETER})
public @interface TransactionActionEnumValidator {

    Class<? extends Enum<?>> TransactionAction();

    String message() default "Invalid transaction action.";

    Class<?>[]groups() default {};

    Class<? extends Payload>[]payload() default {};
}
