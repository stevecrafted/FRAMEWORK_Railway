package org.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.Entity.HttpMethod;

/**
 * Annotation générique pour mapper une méthode à une URL avec spécification de la méthode HTTP.
 *  
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RequestMapping { 
    String value() default "";
    HttpMethod method() default HttpMethod.GET;
}
