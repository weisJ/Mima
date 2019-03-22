package edu.kit.mima.gui.components.button;

import edu.kit.mima.api.observing.ClassObservable;
import edu.kit.mima.api.observing.Observable;
import edu.kit.mima.gui.components.tooltip.ITooltip;
import edu.kit.mima.gui.components.tooltip.Tooltip;
import edu.kit.mima.gui.components.tooltip.TooltipComponent;
import edu.kit.mima.util.BindingUtil;
import edu.kit.mima.util.HSLColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Supplier;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

/**
 * Builder for creating a button panel.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class ButtonPanelBuilder {

    private static final int BUTTON_DELAY = 2000;
    private static final int BUTTON_VANISH_DELAY = 2000;

    @NotNull private final JPanel panel;
    @NotNull private final Queue<JButton> buttons;
    private final LayoutManager layoutManager;

    /**
     * Create new ButtonPanelBuilder.
     */
    public ButtonPanelBuilder() {
        panel = new JPanel();
        buttons = new LinkedList<>();
        this.layoutManager = new BoxLayout(panel, BoxLayout.X_AXIS);
    }

    /**
     * Add Button to the ButtonPanel.
     *
     * @param label Label
     * @return ButtonBuilder
     */
    @NotNull
    @Contract("_ -> new")
    public ButtonBuilder addButton(final String label) {
        return new ButtonBuilder(label, this);
    }

    /**
     * Add external Button to the ButtonPanel.
     *
     * @param button button to add
     * @return ButtonBuilder
     */
    @NotNull
    @Contract("_ -> new")
    public ButtonBuilder addButton(final JButton button) {
        return new ButtonBuilder(button, this);
    }

    public final class ButtonBuilder {

        private final JButton button;
        private final ButtonPanelBuilder parent;

        private ButtonBuilder(final String label, final ButtonPanelBuilder parent) {
            button = new JButton(label);
            this.parent = parent;
        }

        private ButtonBuilder(final JButton button, final ButtonPanelBuilder parent) {
            this.button = button;
            this.parent = parent;
        }

        /**
         * Set the accelerator for the current button.
         *
         * @param accelerator key combination to trigger button event
         * @return this
         */
        @Contract("_ -> this")
        @NotNull
        public ButtonBuilder addAccelerator(final String accelerator) {
            setAccelerator(accelerator);
            return this;
        }

        /**
         * Set the action for the current button.
         *
         * @param action action to perform at button press
         * @return this
         */
        @Contract("_ -> this")
        @NotNull
        public ButtonBuilder addAction(@NotNull final Runnable action) {
            setAction(action);
            return this;
        }

        /**
         * Set whether the button is enabled.
         *
         * @param enabled boolean
         * @return this
         */
        @Contract("_ -> this")
        @NotNull
        public ButtonBuilder setEnabled(final boolean enabled) {
            button.setEnabled(enabled);
            return this;
        }

        /**
         * Set whether the button should be visible.
         *
         * @param visible true if visible
         * @return this
         */
        @Contract("_ -> this")
        @NotNull
        public ButtonBuilder setVisible(final boolean visible) {
            button.setVisible(visible);
            return this;
        }

        /**
         * Bind an action to an property event occurring in the observed instance.
         *
         * @param observed observed object
         * @param binding  binding method to run
         * @param property properties to listen to
         * @param <T>      observed object must extend {@link JComponent}
         * @return this
         */
        @Contract("_, _, _ -> this")
        @NotNull
        public <T extends JComponent> ButtonBuilder bind(@NotNull final T observed,
                                                         @NotNull final Runnable binding,
                                                         @NotNull final String... property) {
            BindingUtil.bind(observed, binding, property);
            return this;
        }

        /**
         * Bind an action to an property event occurring in the observed instance.
         *
         * @param observed observed object
         * @param binding  binding method to run
         * @param property properties to listen to
         * @param <T>      observed object must extend {@link Observable}
         * @return this
         */
        @Contract("_, _, _ -> this")
        @NotNull
        public <T extends Observable> ButtonBuilder bind(@NotNull final T observed,
                                                         @NotNull final Runnable binding,
                                                         @NotNull final String... property) {
            BindingUtil.bind(observed, binding, property);
            return this;
        }

        /**
         * Bind an action to an property event occurring in the observed class.
         *
         * @param clazz    class to observe
         * @param binding  binding method to run
         * @param property properties to listen to
         * @param <T>      observed class must extend {@link ClassObservable}
         * @return this
         */
        @Contract("_, _, _ -> this")
        @NotNull
        public <T extends ClassObservable> ButtonBuilder bindClass(
                final Class<T> clazz,
                @NotNull final Runnable binding,
                @NotNull final String... property) {
            BindingUtil.bindClass(clazz, binding, property);
            return this;
        }

        /**
         * Bind the visibility to an property event occurring in the observed instance.
         *
         * @param observed observed object
         * @param binding  binding method to determine status
         * @param property properties to listen to
         * @param <T>      observed object must extend {@link JComponent}
         * @return this
         */
        @Contract("_, _, _ -> this")
        @NotNull
        public <T extends JComponent> ButtonBuilder bindVisible(
                @NotNull final T observed,
                @NotNull final Supplier<Boolean> binding,
                @NotNull final String... property) {
            BindingUtil.bind(observed, () -> button.setVisible(binding.get()), property);
            return this;
        }

        /**
         * Bind the visibility to an property event occurring in the observed instance.
         *
         * @param observed observed object
         * @param binding  binding method to determine status
         * @param property properties to listen to
         * @param <T>      observed object must extend {@link Observable}
         * @return this
         */
        @Contract("_, _, _ -> this")
        @NotNull
        public <T extends Observable> ButtonBuilder bindVisible(
                @NotNull final T observed,
                @NotNull final Supplier<Boolean> binding,
                @NotNull final String... property) {
            BindingUtil.bind(observed, () -> button.setVisible(binding.get()), property);
            return this;
        }

        /**
         * Bind the enabled to an property event occurring in the observed instance.
         *
         * @param observed observed object
         * @param binding  binding method to determine status
         * @param property properties to listen to
         * @param <T>      observed object must extend {@link JComponent}
         * @return this
         */
        @Contract("_, _, _ -> this")
        @NotNull
        public <T extends JComponent> ButtonBuilder bindEnabled(
                @NotNull final T observed,
                @NotNull final Supplier<Boolean> binding,
                @NotNull final String... property) {
            BindingUtil.bind(observed, () -> button.setEnabled(binding.get()), property);
            return this;
        }

        /**
         * Bind the enabled to an property event occurring in the observed instance.
         *
         * @param observed observed object
         * @param binding  binding method to determine status
         * @param property properties to listen to
         * @param <T>      observed object must extend {@link Observable}
         * @return this
         */
        @Contract("_, _, _ -> this")
        @NotNull
        public <T extends Observable> ButtonBuilder bindEnabled(
                @NotNull final T observed,
                @NotNull final Supplier<Boolean> binding,
                @NotNull final String... property) {
            BindingUtil.bind(observed, () -> button.setEnabled(binding.get()), property);
            return this;
        }

        /**
         * Bind the visibility status to an property event occurring in the observed class.
         *
         * @param clazz    class to observe
         * @param binding  binding method to determine status
         * @param property properties to listen to
         * @param <T>      observed class must extend {@link ClassObservable}
         * @return this
         */
        @Contract("_, _, _ -> this")
        @NotNull
        public <T extends ClassObservable> ButtonBuilder bindClassVisible(
                final Class<T> clazz,
                @NotNull final Supplier<Boolean> binding,
                @NotNull final String... property) {
            BindingUtil.bindClass(clazz, () -> button.setVisible(binding.get()), property);
            return this;
        }

        /**
         * Bind the enabled status to an property event occurring in the observed class.
         *
         * @param clazz    class to observe
         * @param binding  binding method to determine status
         * @param property properties to listen to
         * @param <T>      observed class must extend {@link ClassObservable}
         * @return this
         */
        @Contract("_, _, _ -> this")
        @NotNull
        public <T extends ClassObservable> ButtonBuilder bindClassEnabled(
                final Class<T> clazz,
                @NotNull final Supplier<Boolean> binding,
                @NotNull final String... property) {
            BindingUtil.bindClass(clazz, () -> button.setEnabled(binding.get()), property);
            return this;
        }


        /**
         * Set tooltip component.
         *
         * @param component component for tooltip
         * @param <T>       component bust be of type {@link JComponent} and {@link ITooltip}.
         * @return this
         */
        @Contract("_ -> this")
        @NotNull
        public <T extends JComponent & ITooltip> ButtonBuilder setTooltip(
                @NotNull final T component) {
            new TooltipComponent<>(button, component, BUTTON_DELAY, BUTTON_VANISH_DELAY,
                                   TooltipComponent.COMPONENT_BOTH).setActive(true);
            button.setToolTipText(null);
            return this;
        }

        /**
         * Set the text for the default tooltip.
         *
         * @param text tooltip text
         * @return this
         */
        @NotNull
        public ButtonBuilder setTooltip(@NotNull final String text) {
            return setTooltip(new Tooltip(text));
        }

        /**
         * Add next Button.
         *
         * @param label Label
         * @return ButtonBuilder
         */
        @NotNull
        @Contract("_ -> new")
        public ButtonBuilder addButton(final String label) {
            parent.buttons.offer(button);
            return new ButtonBuilder(label, parent);
        }

        /**
         * Add external Button to the ButtonPanel.
         *
         * @param button button to add
         * @return ButtonBuilder
         */
        @NotNull
        @Contract("_ -> new")
        public ButtonBuilder addButton(final JButton button) {
            parent.buttons.offer(this.button);
            return new ButtonBuilder(button, parent);
        }

        @NotNull
        @Contract(" -> new")
        public ButtonBuilder addSpace() {
            parent.buttons.offer(button);
            return new ButtonBuilder(new Spacer(), parent);
        }

        @NotNull
        @Contract(" -> new")
        public ButtonBuilder addSeparator() {
            parent.buttons.offer(button);
            return new ButtonBuilder(new Separator(), parent);
        }

        /**
         * Construct the ButtonPanel. Buttons are added in the order they were configured.
         *
         * @return JPanel containing the buttons next to each other
         */
        @NotNull
        public JPanel get() {
            parent.buttons.offer(button);
            LayoutManager lm = parent.layoutManager;
            if (lm instanceof GridLayout) {
                lm = new GridLayout(1, parent.buttons.size());
            }
            panel.setLayout(lm);
            while (!parent.buttons.isEmpty()) {
                panel.add(parent.buttons.poll());
            }
            return panel;
        }

        private void setAction(@NotNull final Runnable action) {
            button.addActionListener(e -> action.run());
        }

        private void setAccelerator(final String accelerator) {
            final Action clickAction = new ClickAction(button);
            button.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(accelerator), accelerator);
            button.getActionMap().put(accelerator, clickAction);
        }
    }

    private class Separator extends Spacer {
        @Override
        protected void paintComponent(@NotNull final Graphics g) {
            super.paintComponent(g);
            g.setColor(new HSLColor(UIManager.getColor("Button.light")).adjustShade(20).getRGB());
            g.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
        }
    }

    private class Spacer extends JButton {
        private Spacer() {
            setOpaque(false);
            setEnabled(false);
            setFocusable(false);
            setBorderPainted(false);
        }

        @NotNull
        @Override
        public Dimension getPreferredSize() {
            final var size = super.getPreferredSize();
            return new Dimension(size.width / 2, size.height);
        }

        @Override
        protected void paintComponent(final Graphics g) {
        }
    }
}
