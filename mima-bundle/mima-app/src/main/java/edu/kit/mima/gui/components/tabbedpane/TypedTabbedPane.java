package edu.kit.mima.gui.components.tabbedpane;

import javax.swing.*;

/**
 * {@link JTabbedPane} with restricted component type.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class TypedTabbedPane<T extends JComponent> extends JTabbedPane {

    public void insertTab(final String title, final Icon icon, final T component,
                          final String tip, final int index) {
        super.insertTab(title, icon, component, tip, index);
    }

    public void addTab(final String title, final Icon icon, final T component, final String tip) {
        super.addTab(title, icon, component, tip);
    }

    public void addTab(final String title, final Icon icon, final T component) {
        super.addTab(title, icon, component);
    }

    public void addTab(final String title, final T component) {
        super.addTab(title, component);
    }

    @SuppressWarnings("unchecked")
    public T getComponentAt(final int index) {
        return (T) super.getComponentAt(index);
    }

    public void setComponentAt(final int index, final T component) {
        super.setComponentAt(index, component);
    }

    public int indexOfComponent(final T component) {
        return super.indexOfComponent(component);
    }
}
