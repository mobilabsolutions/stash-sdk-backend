package com.mobilabsolutions.payment.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * @author <a href="mailto:mohamed.osman@mobilabsolutions.com">Mohamed Osman</a>
 */

@Documented
@Constraint(validatedBy = KeyTypeEnumValidatorImpl.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(FIELD)
public @interface KeyTypeEnumValidator {

    Class<? extends Enum<?>> KeyType();

    String message() default "Invalid key type.";

    Class<?>[]groups() default {};

    Class<? extends Payload>[]payload() default {};
}
