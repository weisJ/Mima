package edu.kit.mima.gui.context;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache for ContextProviders using the PopupListener for their implementation.
 *
 * @author Jannis Weis
 * @since 2019
 */
@SuppressWarnings("NonFinalUtilityClass")
public class CachedContextProvider {

    private static final Map<Object, Object> cache = new HashMap<>();

    protected static void cache(final Object target, final Object context) {
        cache.put(target, context);
    }

    @SuppressWarnings("unchecked")
    protected static <T> T get(final Object target) {
        return (T)cache.get(target);
    }
}
