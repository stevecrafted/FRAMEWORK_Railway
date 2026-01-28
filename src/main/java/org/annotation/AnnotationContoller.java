package org.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generic annotation dans org.
 * 
 * Annotation controlleur personnalisé
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface AnnotationContoller {
    /**
     * Optional value for the annotation.
     */
    String value() default "";
}
