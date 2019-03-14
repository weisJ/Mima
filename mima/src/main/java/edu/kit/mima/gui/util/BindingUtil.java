package edu.kit.mima.gui.util;

import edu.kit.mima.gui.observing.ClassObservable;
import edu.kit.mima.gui.observing.Observable;
import edu.kit.mima.gui.observing.SharedInstance;

import javax.swing.JComponent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author Jannis Weis
 * @since 2018
 */
public final class BindingUtil {

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
    public static <T extends JComponent> void bind(T observed, Runnable binding, String... property) {
        for (var s : property) {
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
    public static <T extends Observable> void bind(T observed, Runnable binding, String... property) {
        for (var s : property) {
            observed.addPropertyChangeListener(s, p -> binding.run());
        }
    }

    /**
     * Bind function execution to property change.
     *
     * @param observed observed class
     * @param binding  binding function
     * @param property properties to check
     * @param <T>      observed class type.
     */
    public static <T extends ClassObservable> void bindClass(Class<T> observed, Runnable binding, String... property) {
        for (Field field : observed.getDeclaredFields()) {
            if (field.getAnnotation(SharedInstance.class) != null) {
                if (field.getType() == observed && Modifier.isStatic(field.getModifiers())) {
                    try {
                        field.setAccessible(true);
                        //noinspection unchecked
                        T instance = ((T) field.get(null));
                        for (var s : property) {
                            instance.addStaticPropertyChangeListener(s, p -> binding.run());
                        }
                        return;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        throw new IllegalStateException("Shared instance is not defined");
    }
}
