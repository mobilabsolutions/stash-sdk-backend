package com.mobilabsolutions.payment.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author <a href="mailto:jovana@mobilabsolutions.com">Jovana Veskovic</a>
 */
@Target({PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { DateValidatorImpl.class })
public @interface DateValidator {

    String pattern() default "yyyy-MM-dd HH:mm:ss";

    String message() default "Invalid date format, should be 'yyyy-MM-dd HH:mm:ss'";

    Class<?>[]groups() default {};

    Class<? extends Payload>[]payload() default {};
}
