package edu.kit.mima.api.observing;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

/**
 * Utility class for binding function execution with property changes.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class BindingUtil {

    @Contract(" -> fail")
    private BindingUtil() {
        assert false : "utility class constructor.";
    }

    /**
     * Bind function execution to property change.
     *
     * @param observed observed object.
     * @param binding  binding function
     * @param property properties to check
     * @param <T>      observed object must implement the {@link Observable} interface.
     */
    public static <T extends JComponent> void bind(@NotNull final T observed,
                                                   @NotNull final Runnable binding,
                                                   @NotNull final String... property) {
        for (final var s : property) {
            observed.addPropertyChangeListener(s, p -> binding.run());
        }
    }

    /**
     * Bind function execution to property change.
     *
     * @param observed observed object.
     * @param binding  binding function
     * @param property properties to check
     * @param <T>      observed object must implement the {@link Observable} interface.
     */
    public static <T extends Observable> void bind(@NotNull final T observed,
                                                   @NotNull final Runnable binding,
                                                   @NotNull final String... property) {
        for (final var s : property) {
            observed.addPropertyChangeListener(s, p -> binding.run());
        }
    }

    /**
     * Bind function execution to property change.
     *
     * @param clazz    class to observe
     * @param binding  binding function
     * @param property properties to check
     * @param <T>      observed class type.
     */
    @SuppressWarnings("unused")
    public static <T extends ClassObservable> void bindClass(final Class<T> clazz,
                                                             @NotNull final Runnable binding,
                                                             @NotNull final String... property) {
        for (final var s : property) {
            T.addStaticPropertyChangeListener(s, p -> binding.run());
        }
    }
}
