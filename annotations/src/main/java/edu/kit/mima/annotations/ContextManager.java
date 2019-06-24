package edu.kit.mima.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Manager for creating application contexts.
 *
 * @author Jannis Weis
 * @since 2019
 */
public final class ContextManager {

    private static final Map<Class<?>, Class<?>> menuMap = new HashMap<>();

    static {
        init();
    }

    public static void registerProvider(final Class<?> provider, final Class<?> providesFor) {
        System.out.println("Registering " + provider + " for " + providesFor);
        menuMap.put(providesFor, provider);
    }

    /**
     * Create a context for the target. Will find the corresponding context menu.
     *
     * @param target the target object.
     */
    public static void createContext(@NotNull final Object target) {
        var targetClass = target.getClass();
        if (menuMap.containsKey(targetClass)) {
            createContext(menuMap.get(targetClass), targetClass, target);
        } else {
            for (var menuClass : menuMap.keySet()) {
                if (menuClass.isAssignableFrom(targetClass)) {
                    createContext(menuMap.get(menuClass), menuClass, target);
                    return;
                }
            }
        }
    }

    private static void createContext(@NotNull final Class<?> supplier, final Class<?> targetType,
                                      final Object target) {
        try {
            var creator = supplier.getDeclaredMethod("createContextMenu", targetType);
            creator.setAccessible(true);
            creator.invoke(null, target);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Found no provider for type " + target.getClass());
        }
    }

    /**
     * Initialize the context manager.
     */
    private static void init() {
        try {
            Class.forName("ContextBuilder");
        } catch (ClassNotFoundException ignore) {
            /* There hasn't been any created contexts.*/
        }
    }
}
