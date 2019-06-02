package edu.kit.mima.gui.persist;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Map that masks the contained values by matching the string representation of the key against a mask.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class MaskedMap<K, V> implements Map<K, V> {

    private final String mask;
    private final Map<K, V> map;

    @Contract(pure = true)
    public MaskedMap(final Map<K, V> map, final String mask) {
        this.map = map;
        this.mask = mask;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Contract(pure = true)
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Contract(pure = true)
    @Override
    public boolean containsKey(final Object key) {
        return key.toString().startsWith(mask) && map.containsKey(key);
    }

    @Contract(pure = true)
    @Override
    public boolean containsValue(final Object value) {
        for (var entry : map.entrySet()) {
            if (entry.toString().startsWith(mask) && entry.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(@NotNull final Object key) {
        return key.toString().startsWith(mask) ? map.get(key) : null;
    }

    @Nullable
    @Override
    public V put(final K key, final V value) {
        return map.put(key, value);
    }

    @Override
    public V remove(final Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(@NotNull final Map<? extends K, ? extends V> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return map.values();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }
}
