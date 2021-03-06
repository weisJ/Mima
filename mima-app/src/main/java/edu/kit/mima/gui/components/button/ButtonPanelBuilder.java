package edu.kit.mima.gui.components.button;

import edu.kit.mima.gui.components.tooltip.DefaultTooltipWindow;
import edu.kit.mima.gui.components.tooltip.TooltipUtil;
import edu.kit.mima.gui.components.tooltip.TooltipWindow;
import edu.kit.mima.util.HSLColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Builder for creating a button panel.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class ButtonPanelBuilder {

    @NotNull
    private final JPanel panel;
    @NotNull
    private final Queue<JButton> buttons;
    @NotNull
    private final LayoutManager layoutManager;

    /**
     * Create new ButtonPanelBuilder.
     */
    public ButtonPanelBuilder() {
        panel = new JPanel();
        buttons = new LinkedList<>();
        this.layoutManager = new BoxLayout(panel, BoxLayout.X_AXIS);
    }

    @NotNull
    @Contract(" -> new")
    public static Separator createSeparator() {
        return new Separator();
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

    public static class Separator extends Spacer {
        @Override
        protected void paintComponent(@NotNull final Graphics g) {
            super.paintComponent(g);
            g.setColor(new HSLColor(UIManager.getColor("Button.light")).adjustShade(20).getRGB());
            g.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
        }
    }

    private static class Spacer extends JButton {
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
         * Set tooltip component.
         *
         * @param component component for tooltip
         * @param <T>       DefaultTooltipWindow type.
         * @return this
         */
        @Contract("_ -> this")
        @NotNull
        public <T extends TooltipWindow> ButtonBuilder setTooltip(@NotNull final T component) {
            TooltipUtil.createDefaultTooltip(button, component);
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
            return setTooltip(new DefaultTooltipWindow(text));
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

        @NotNull
        @Contract("_ -> new")
        public ButtonBuilder addSeparator(final Separator separator) {
            parent.buttons.offer(button);
            return new ButtonBuilder(separator, parent);
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
}
