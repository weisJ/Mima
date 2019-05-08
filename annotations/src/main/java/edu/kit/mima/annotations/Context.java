package edu.kit.mima.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to signal the class is a provider for a context menu.
 *
 * @author Jannis Weis
 * @since 2019
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Context {
    /**
     * The class this Context provides for.
     *
     * @return the class this context supports.
     */
    Class[] provides();
}
