package org.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation générique pour mapper une méthode de contrôleur à une URL.
 * 
 * Note: Cette annotation est conservée pour la rétrocompatibilité.
 * Il est recommandé d'utiliser @GET ou @POST pour plus de clarté.
 *
 * Exemple d'utilisation :
 * <pre>
 * {@code @AnnotationMethode("/user/list")}
 * public ModelView listUsers() { ... }
 * </pre>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AnnotationMethode {
    /**
     * URL de la route (ex: "/user/list")
     */
    String value() default "";
}
