package edu.kit.mima.gui.menu;

import javax.swing.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public final class MenuBuilder {

    private final JMenuBar menuBar;

    /**
     * Create new MenuBuilder
     */
    public MenuBuilder() {
        menuBar = new JMenuBar();
    }

    /**
     * Add a menu to the MenuBar
     *
     * @param title Label
     * @return SubMenuBuilder
     */
    public SubMenuBuilder addMenu(final String title) {
        return new SubMenuBuilder(title, this);
    }

    public static final class SubMenuBuilder {

        private final JMenu menu;
        private final MenuBuilder parent;

        private SubMenuBuilder(final String title, final MenuBuilder parent) {
            super();
            menu = new JMenu(title);
            this.parent = parent;
        }

        /**
         * Add new MenuItem
         *
         * @param title       label
         * @param action      action to perform when pressed
         * @param accelerator key combination to trigger clicked event
         * @return this
         */
        public SubMenuBuilder addItem(final String title, final Runnable action, final String accelerator) {
            final JMenuItem item = new JMenuItem(title);
            item.addActionListener(e -> action.run());
            item.setAccelerator(KeyStroke.getKeyStroke(accelerator));
            menu.add(item);
            return this;
        }

        /**
         * Add new MenuItem
         *
         * @param title  label
         * @param action action to perform when pressed
         * @return this
         */
        public SubMenuBuilder addItem(final String title, final Runnable action) {
            final JMenuItem item = new JMenuItem(title);
            item.addActionListener(e -> action.run());
            menu.add(item);
            return this;
        }

        /**
         * Add new MenuItem
         *
         * @param title label
         * @return this
         */
        public SubMenuBuilder addItem(final String title) {
            final JMenuItem item = new JMenuItem(title);
            menu.add(item);
            return this;
        }

        /**
         * Add a separator
         *
         * @return this
         */
        public SubMenuBuilder separator() {
            menu.addSeparator();
            return this;
        }

        /**
         * Add a new Menu to the MenuBar
         *
         * @param title label
         * @return SubMenuBuilder
         */
        public SubMenuBuilder addMenu(final String title) {
            parent.menuBar.add(menu);
            return parent.addMenu(title);
        }

        /**
         * Construct the MenuBar and add all components
         *
         * @return JMenuBar
         */
        public JMenuBar get() {
            parent.menuBar.add(menu);
            return parent.menuBar;
        }
    }
}
