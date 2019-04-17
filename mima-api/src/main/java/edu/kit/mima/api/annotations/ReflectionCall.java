package edu.kit.mima.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for methods called by the reflection api.
 *
 * @author Jannis Weis
 * @since 2018
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
@SuppressWarnings("unused")
public @interface ReflectionCall {
}
