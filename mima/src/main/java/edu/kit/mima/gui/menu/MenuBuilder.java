package edu.kit.mima.gui.menu;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.MenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * Builder for creating simple {@link MenuBar}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class MenuBuilder {

    @NotNull
    private final JMenuBar menuBar;

    /**
     * Create new MenuBuilder.
     */
    public MenuBuilder() {
        menuBar = new JMenuBar();
    }

    /**
     * Add a menu to the MenuBar.
     *
     * @param title Label
     * @return SubMenuBuilder
     */
    @NotNull
    @Contract("_ -> new")
    public SubMenuBuilder addMenu(final String title) {
        return new SubMenuBuilder(title, this);
    }

    public static final class SubMenuBuilder {

        @NotNull
        private final JMenu menu;
        private final MenuBuilder parent;

        private SubMenuBuilder(final String title, final MenuBuilder parent) {
            super();
            menu = new JMenu(title);
            this.parent = parent;
        }

        /**
         * Add new MenuItem.
         *
         * @param title       label
         * @param action      action to perform when pressed
         * @param accelerator key combination to trigger clicked event
         * @return this
         */
        @Contract("_, _, _ -> this")
        @NotNull
        public SubMenuBuilder addItem(final String title,
                                      @NotNull final Runnable action,
                                      final String accelerator) {
            final JMenuItem item = new JMenuItem(title);
            item.addActionListener(e -> action.run());
            item.setAccelerator(KeyStroke.getKeyStroke(accelerator));
            menu.add(item);
            return this;
        }

        /**
         * Add new MenuItem.
         *
         * @param title  label
         * @param action action to perform when pressed
         * @return this
         */
        @Contract("_, _ -> this")
        @NotNull
        public SubMenuBuilder addItem(final String title, @NotNull final Runnable action) {
            final JMenuItem item = new JMenuItem(title);
            item.addActionListener(e -> action.run());
            menu.add(item);
            return this;
        }

        /**
         * Add new MenuItem.
         *
         * @param title label
         * @return this
         */
        @Contract("_ -> this")
        @NotNull
        public SubMenuBuilder addItem(final String title) {
            final JMenuItem item = new JMenuItem(title);
            menu.add(item);
            return this;
        }

        /**
         * Add external JMenuItem to menu.
         *
         * @param item   item to add
         * @param action action to perform when clicked
         * @return this
         */
        @Contract("_, _ -> this")
        @NotNull
        public SubMenuBuilder addItem(@NotNull final JMenuItem item,
                                      @NotNull final Runnable action) {
            menu.add(item);
            item.addActionListener(e -> action.run());
            return this;
        }

        /**
         * Add an external JMenuItem to menu.
         *
         * @param item item to add
         * @return this
         */
        @Contract("_ -> this")
        @NotNull
        public SubMenuBuilder addItem(final JMenuItem item) {
            menu.add(item);
            return this;
        }

        /**
         * Add a separator.
         *
         * @return this
         */
        @Contract(" -> this")
        @NotNull
        public SubMenuBuilder separator() {
            menu.addSeparator();
            return this;
        }

        /**
         * Set the Mnemonic for this menu.
         *
         * @param mnemonic mnemonic char
         * @return this
         */
        @Contract("_ -> this")
        @NotNull
        public SubMenuBuilder setMnemonic(final int mnemonic) {
            menu.setMnemonic(mnemonic);
            return this;
        }

        /**
         * Add a new Menu to the MenuBar.
         *
         * @param title label
         * @return SubMenuBuilder
         */
        @NotNull
        public SubMenuBuilder addMenu(final String title) {
            parent.menuBar.add(menu);
            return parent.addMenu(title);
        }

        /**
         * Construct the MenuBar and add all components.
         *
         * @return JMenuBar
         */
        @NotNull
        public JMenuBar get() {
            parent.menuBar.add(menu);
            return parent.menuBar;
        }
    }
}
