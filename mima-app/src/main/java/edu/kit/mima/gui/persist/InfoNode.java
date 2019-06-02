package edu.kit.mima.gui.persist;

import org.jetbrains.annotations.Contract;

import java.util.HashMap;
import java.util.Map;

/**
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

    @Contract(pure = true)
    public String getKey() {
        return key;
    }

    @Contract(pure = true)
    public Map<String, InfoNode> getChildren() {
        return children;
    }

    @Contract(pure = true)
    public Object getValue() {
        return value;
    }

    public void setValue(final Object value) {
        this.value = value;
    }

    @Contract(pure = true)
    public boolean isKeyEnd() {
        return keyEnd;
    }

    public void setKeyEnd(final boolean keyEnd) {
        this.keyEnd = keyEnd;
    }
}
