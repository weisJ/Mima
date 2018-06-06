package edu.kit.mima.gui.menu;

import javax.swing.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class MenuBuilder {

    private JMenuBar menuBar;

    /**
     * Create new MenuBuilder
     */
    public MenuBuilder() {
        this.menuBar = new JMenuBar();
    }

    /**
     * Add a menu to the MenuBar
     *
     * @param title Label
     * @return SubMenuBuilder
     */
    public final SubMenuBuilder addMenu(String title) {
        return new SubMenuBuilder(title, this);
    }

    public class SubMenuBuilder {

        private JMenu menu;
        private MenuBuilder parent;

        private SubMenuBuilder(String title, MenuBuilder parent) {
            this.menu = new JMenu(title);
            this.parent = parent;
        }

        /**
         * Add new MenuItem
         *
         * @param title label
         * @param action action to perform when pressed
         * @param accelerator key combination to trigger clicked event
         * @return this
         */
        public SubMenuBuilder addItem(String title, Runnable action, String accelerator) {
            JMenuItem item = new JMenuItem(title);
            item.addActionListener(e -> action.run());
            item.setAccelerator(KeyStroke.getKeyStroke(accelerator));
            menu.add(item);
            return this;
        }

        /**
         * Add new MenuItem
         *
         * @param title label
         * @param action action to perform when pressed
         * @return this
         */
        public SubMenuBuilder addItem(String title, Runnable action) {
            JMenuItem item = new JMenuItem(title);
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
        public SubMenuBuilder addItem(String title) {
            JMenuItem item = new JMenuItem(title);
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
        public SubMenuBuilder addMenu(String title) {
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
