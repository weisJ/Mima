package edu.kit.mima.gui.persist;

import org.jetbrains.annotations.Contract;

import java.util.HashMap;
import java.util.Map;

/**
 * Node for keeping persistence information.
 *
 * @author Jannis Weis
 * @since 2019
 */
public final class InfoNode {
    private final String key;
    private final Map<String, InfoNode> children;
    private Object value;
    private boolean keyEnd;

    @Contract(pure = true)
    InfoNode(final String key, final Object value, final boolean keyEnd) {
        this.value = value;
        this.key = key;
        this.keyEnd = keyEnd;
        children = new HashMap<>();
    }

    /**
     * Get the associated key.
     *
     * @return the key.
     */
    @Contract(pure = true)
    public String getKey() {
        return key;
    }

    /**
     * Get the child nodes.
     *
     * @return map of child nodes.
     */
    @Contract(pure = true)
    public Map<String, InfoNode> getChildren() {
        return children;
    }

    /**
     * Get the value.
     *
     * @return the value of the node.
     */
    @Contract(pure = true)
    public Object getValue() {
        return value;
    }

    /**
     * Set the value of the node.
     *
     * @param value the new value.
     */
    public void setValue(final Object value) {
        this.value = value;
    }

    /**
     * If true represents the end of a key and indicates that the value in the node should be saved.
     *
     * @return true if key end.
     */
    @Contract(pure = true)
    public boolean isKeyEnd() {
        return keyEnd;
    }

    /**
     * Set whether a key ends at this node.
     *
     * @param keyEnd true if key ends at this node.
     */
    public void setKeyEnd(final boolean keyEnd) {
        this.keyEnd = keyEnd;
    }
}
