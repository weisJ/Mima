package edu.kit.mima.gui.persist;

import edu.kit.mima.api.util.Tuple;
import edu.kit.mima.api.util.ValueTuple;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Persistence Info Object.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class PersistenceInfo {

    private final InfoNode root;
    private final Map<String, String> valueMap;

    @Contract(pure = true)
    public PersistenceInfo() {
        root = new InfoNode("", "", false);
        valueMap = new HashMap<>();
    }

    @Contract(pure = true)
    private PersistenceInfo(final InfoNode node, final Map<String, String> valueMap) {
        this.root = node;
        this.valueMap = valueMap;
    }

    public PersistenceInfo getSubTree(@NotNull final String prefix) {
        String[] keys = prefix.isEmpty() ? new String[0] : prefix.split("\\.");
        InfoNode node = traverseToSub(keys, root, keys.length).getFirst();
        return new PersistenceInfo(node, valueMap);
    }

    @NotNull
    @Contract("_, _, _ -> new")
    private Tuple<InfoNode, Integer> traverseToSub(@NotNull final String[] keys,
                                                   final InfoNode start, final int maxIndex) {
        InfoNode curr = start;
        InfoNode next = start;
        int i = 0;
        while (i < maxIndex && next != null) {
            curr = next;
            next = next.getChildren().get(keys[i]);
            i++;
        }
        if (next == null) {
            return new ValueTuple<>(curr, i - 1);
        } else {
            return new ValueTuple<>(next, i);
        }
    }

    /**
     * Store a value.
     *
     * @param key   the key.
     * @param value the value.
     */
    public void putValue(@NotNull final String key, final Object value) {
        InfoNode node = createNodes(key);
        node.setKeyEnd(true);
        node.setValue(value);
        valueMap.put(key, value.toString());
    }

    private InfoNode createNodes(@NotNull final String key) {
        String[] keys = key.isEmpty() ? new String[0] : key.split("\\.");
        var traverse = traverseToSub(keys, root, keys.length);
        StringBuilder subKey = new StringBuilder(traverse.getFirst().getKey());
        InfoNode curr = traverse.getFirst();
        for (int i = traverse.getSecond(); i < keys.length; i++) {
            if (subKey.length() > 0) {
                subKey.append('.');
            }
            subKey.append(keys[i]);
            var node = new InfoNode(subKey.toString(), null, false);
            curr.getChildren().put(keys[i], node);
            curr = node;
        }
        return curr;
    }

    @Nullable
    private Object get(@NotNull final String key) {
        String k = root.getKey();
        k = k.isEmpty() ? key : k + "." + key;
        return valueMap.get(k);
    }

    public void remove(@NotNull final PersistenceInfo info, @NotNull final String prefix) {
        String[] keys = prefix.isEmpty() ? new String[0] : prefix.split("\\.");
        InfoNode subTree = traverseToSub(keys, root, keys.length).getFirst();
        remove(subTree, info.root);
    }

    private void remove(final InfoNode target, @NotNull final InfoNode remove) {
        if (target == null) {
            return;
        }
        for (var entry : remove.getChildren().entrySet()) {
            InfoNode n = entry.getValue();
            String key = entry.getKey();
            InfoNode tarNode = target.getChildren().get(key);
            remove(tarNode, n);
            if (n.isKeyEnd()) {
                valueMap.remove(n.getKey());
                if (tarNode != null) {
                    if (tarNode.getChildren().isEmpty()) {
                        target.getChildren().remove(key);
                    } else {
                        tarNode.setKeyEnd(false);
                    }
                }
            }
        }
    }

    /**
     * Get the value associated with the key.
     *
     * @param key          the kay.
     * @param defaultValue default value to use when value could not be loaded.
     * @return the value.
     */
    public String getString(final String key, final String defaultValue) {
        String value = (String) get(key);
        return value != null ? value : defaultValue;
    }

    public double getDouble(final String key, final double defaultValue) {
        String value = (String) get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public int getInt(final String key, final int defaultValue) {
        String value = (String) get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(final String key, final boolean defaultValue) {
        String value = (String) get(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    public void merge(@NotNull final PersistenceInfo saveState) {
        merge(saveState, saveState.root.getKey());
    }

    private void merge(@NotNull final InfoNode source, @NotNull final InfoNode dest) {
        dest.setKeyEnd(source.isKeyEnd());
        String pref = dest.getKey().isEmpty() ? "" : dest.getKey() + '.';
        for (var entry : source.getChildren().entrySet()) {
            InfoNode n = entry.getValue();
            String key = entry.getKey();
            if (n.isKeyEnd()) {
                valueMap.put(pref + key, n.getValue() == null ? "" : n.getValue().toString());
            }
            if (dest.getChildren().containsKey(key)) {
                merge(n, dest.getChildren().get(key));
            } else {
                InfoNode newNode = new InfoNode(pref + n.getKey(), n.getValue(), n.isKeyEnd());
                dest.getChildren().put(entry.getKey(), newNode);
                merge(n, newNode);
            }
        }
    }

    public void merge(@NotNull final PersistenceInfo saveState, @NotNull final String prefix) {
        InfoNode node = createNodes(prefix);
        merge(saveState.root, node);
    }

    public Map<String, String> directMap() {
        return new MaskedMap<>(valueMap, root.getKey());
    }

}
