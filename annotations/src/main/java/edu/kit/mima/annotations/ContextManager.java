package edu.kit.mima.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class ContextManager {

    private static final Map<Class<?>, Class<?>> menuMap = new HashMap<>();

    static {
        init();
    }

    public static void registerProvider(final Class<?> provider, final Class<?> providesFor) {
        menuMap.put(providesFor, provider);
    }

    /**
     * Create a context for the target. Will find the corresponding context menu.
     *
     * @param target the target object.
     * @throws IllegalStateException if no appropriate context could be found.
     */
    public static void createContext(@NotNull final Object target) throws IllegalStateException {
        var targetClass = target.getClass();
        if (menuMap.containsKey(targetClass)) {
            createContext(menuMap.get(targetClass), targetClass, target);
            return;
        } else {
            for (var menuClass : menuMap.keySet()) {
                if (menuClass.isAssignableFrom(targetClass)) {
                    createContext(menuMap.get(menuClass), menuClass, target);
                    return;
                }
            }
        }
        throw new IllegalStateException("Found no provider for type " + targetClass);
    }

    private static void createContext(
            @NotNull final Class<?> supplier, final Class<?> targetType, final Object target) {
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
