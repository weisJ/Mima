package edu.kit.mima.gui.button;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.GridLayout;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Builder for creating a button panel
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class ButtonPanelBuilder {

    private final Queue<JButton> buttons;

    /**
     * Create new ButtonPanelBuilder
     */
    public ButtonPanelBuilder() {
        buttons = new LinkedList<>();
    }

    /**
     * Add Button to the ButtonPanel
     *
     * @param label       Label
     * @param action      Action to perform when clicked
     * @param accelerator Key combination to trigger button event
     * @return ButtonBuilder
     */
    public ButtonBuilder addButton(final String label, final Runnable action, final String accelerator) {
        return new ButtonBuilder(label, action, accelerator, this);
    }

    /**
     * Add Button to the ButtonPanel
     *
     * @param label  Label
     * @param action Action to perform when clicked
     * @return ButtonBuilder
     */
    public ButtonBuilder addButton(final String label, final Runnable action) {
        return new ButtonBuilder(label, action, this);
    }

    /**
     * Add Button to the ButtonPanel
     *
     * @param label Label
     * @return ButtonBuilder
     */
    public ButtonBuilder addButton(final String label) {
        return new ButtonBuilder(label, this);
    }

    /**
     * Add external Button to the ButtonPanel
     *
     * @param button button to add
     * @return ButtonBuilder
     */
    public ButtonBuilder addButton(final JButton button) {
        return new ButtonBuilder(button, this);
    }

    public static final class ButtonBuilder {

        private final JButton button;
        private final ButtonPanelBuilder parent;

        private ButtonBuilder(final String label, final Runnable action, final String accelerator,
                              final ButtonPanelBuilder parent) {
            button = new JButton(label);
            this.parent = parent;
            setAction(action);
            setAccelerator(accelerator);
        }

        private ButtonBuilder(final String label, final Runnable action, final ButtonPanelBuilder parent) {
            button = new JButton(label);
            this.parent = parent;
            setAction(action);
        }

        private ButtonBuilder(final String label, final ButtonPanelBuilder parent) {
            button = new JButton(label);
            this.parent = parent;
        }

        private ButtonBuilder(final JButton button, final ButtonPanelBuilder parent) {
            this.button = button;
            this.parent = parent;
        }

        /**
         * Set the accelerator for the current button
         *
         * @param accelerator key combination to trigger button event
         * @return this
         */
        public ButtonBuilder addAccelerator(final String accelerator) {
            setAccelerator(accelerator);
            return this;
        }

        /**
         * Set the action for the current button
         *
         * @param action action to perform at button press
         * @return this
         */
        public ButtonBuilder addAction(final Runnable action) {
            setAction(action);
            return this;
        }

        /**
         * Set whether the button is enabled
         *
         * @param enabled boolean
         * @return this
         */
        public ButtonBuilder setEnabled(final boolean enabled) {
            button.setEnabled(enabled);
            return this;
        }

        /**
         * Add next Button
         *
         * @param label       Label
         * @param action      action to perform at button press
         * @param accelerator key combination to trigger button action
         * @return ButtonBuilder
         */
        public ButtonBuilder addButton(final String label, final Runnable action, final String accelerator) {
            parent.buttons.offer(button);
            return new ButtonBuilder(label, action, accelerator, parent);
        }

        /**
         * Add next Button
         *
         * @param label  Label
         * @param action action to perform at button press
         * @return ButtonBuilder
         */
        public ButtonBuilder addButton(final String label, final Runnable action) {
            parent.buttons.offer(button);
            return new ButtonBuilder(label, action, parent);
        }

        /**
         * Add next Button
         *
         * @param label Label
         * @return ButtonBuilder
         */
        public ButtonBuilder addButton(final String label) {
            parent.buttons.offer(button);
            return new ButtonBuilder(label, parent);
        }

        /**
         * Add external Button to the ButtonPanel
         *
         * @param button button to add
         * @return ButtonBuilder
         */
        public ButtonBuilder addButton(final JButton button) {
            parent.buttons.offer(this.button);
            return new ButtonBuilder(button, parent);
        }

        /**
         * Construct the ButtonPanel. Buttons are added in the order they were configured
         *
         * @return JPanel containing the buttons next to each other
         */
        public JPanel get() {
            parent.buttons.offer(button);
            final JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(1, parent.buttons.size()));
            while (!parent.buttons.isEmpty()) {
                panel.add(parent.buttons.poll());
            }
            return panel;
        }

        private void setAction(final Runnable action) {
            button.addActionListener(e -> action.run());
        }

        private void setAccelerator(final String accelerator) {
            final Action clickAction = new ClickAction(button);
            button.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(accelerator), accelerator);
            button.getActionMap().put(accelerator, clickAction);
            button.setToolTipText(" ");
        }
    }
}
