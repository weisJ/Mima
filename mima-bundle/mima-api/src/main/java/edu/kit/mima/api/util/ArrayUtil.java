package edu.kit.mima.api.util;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * Array utility functions.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class ArrayUtil {

    /**
     * Convert collection to array.
     *
     * @param c   the collection.
     * @param a   the target array.
     * @param <T> the type of the collection.
     * @return array from collection.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> T[] toArray(@NotNull final Collection<T> c, @NotNull final T[] a) {
        return c.size() > a.length
               ? c.toArray((T[]) Array.newInstance(a.getClass().getComponentType(), c.size()))
               : c.toArray(a);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> T[] toArray(@NotNull final Collection<T> c, final Class<?> klass) {
        return toArray(c, (T[]) Array.newInstance(klass, c.size()));
    }

    @NotNull
    public static <T> T[] toArray(@NotNull final Collection<T> c) {
        return toArray(c, c.iterator().next().getClass());
    }
}
