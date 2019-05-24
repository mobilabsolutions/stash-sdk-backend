package com.mobilabsolutions.payment.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.NotNull;
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
@NotNull
@ReportAsSingleViolation
public @interface KeyTypeEnumValidator {

    Class<? extends Enum<?>> KeyType();

    String message() default "Invalid key type.";

    Class<?>[]groups() default {};

    Class<? extends Payload>[]payload() default {};
}
