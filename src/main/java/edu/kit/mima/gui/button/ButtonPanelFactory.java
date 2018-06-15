package edu.kit.mima.gui.button;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Jannis Weis
 * @since 2018
 */
public final class ButtonPanelFactory {

    private final Queue<JButton> buttons;

    /**
     * Create new ButtonPanelFactory
     */
    public ButtonPanelFactory() {
        buttons = new LinkedList<>();
    }

    /**
     * Add Button to the ButtonPanel
     *
     * @param label       Label
     * @param action      Action to perform when clicked
     * @param accelerator Key combination to trigger button event
     * @return ButtonFactory
     */
    public ButtonFactory addButton(final String label, final Runnable action, final String accelerator) {
        return new ButtonFactory(label, action, accelerator, this);
    }

    /**
     * Add Button to the ButtonPanel
     *
     * @param label  Label
     * @param action Action to perform when clicked
     * @return ButtonFactory
     */
    public ButtonFactory addButton(final String label, final Runnable action) {
        return new ButtonFactory(label, action, this);
    }

    /**
     * Add Button to the ButtonPanel
     *
     * @param label Label
     * @return ButtonFactory
     */
    public ButtonFactory addButton(final String label) {
        return new ButtonFactory(label, this);
    }

    /**
     * Add external Button to the ButtonPanel
     *
     * @param button button to add
     * @return ButtonFactory
     */
    public ButtonFactory addButton(final JButton button) {
        return new ButtonFactory(button, this);
    }

    public static final class ButtonFactory {

        private final JButton button;
        private final ButtonPanelFactory parent;

        private ButtonFactory(final String label, final Runnable action, final String accelerator,
                              final ButtonPanelFactory parent) {
            button = new JButton(label);
            this.parent = parent;
            setAction(action);
            setAccelerator(accelerator);
            styleButton();
        }

        private ButtonFactory(final String label, final Runnable action, final ButtonPanelFactory parent) {
            button = new JButton(label);
            this.parent = parent;
            setAction(action);
            styleButton();
        }

        private ButtonFactory(final String label, final ButtonPanelFactory parent) {
            button = new JButton(label);
            this.parent = parent;
            styleButton();
        }

        private ButtonFactory(final JButton button, final ButtonPanelFactory parent) {
            this.button = button;
            this.parent = parent;
            styleButton();
        }

        private void styleButton() {
            button.setForeground(Color.BLACK);
            final Border line = new LineBorder(Color.DARK_GRAY);
            final Border margin = new EmptyBorder(5, 15, 5, 15);
            final Border compound = new CompoundBorder(line, margin);
            button.setBorder(compound);
            button.setFocusPainted(false);
        }

        /**
         * Set the accelerator for the current button
         *
         * @param accelerator key combination to trigger button event
         * @return this
         */
        public ButtonFactory addAccelerator(final String accelerator) {
            setAccelerator(accelerator);
            return this;
        }

        /**
         * Set the action for the current button
         *
         * @param action action to perform at button press
         * @return this
         */
        public ButtonFactory addAction(final Runnable action) {
            setAction(action);
            return this;
        }

        /**
         * Set whether the button is enabled
         *
         * @param enabled boolean
         * @return this
         */
        public ButtonFactory setEnabled(final boolean enabled) {
            button.setEnabled(enabled);
            return this;
        }

        /**
         * Add next Button
         *
         * @param label       Label
         * @param action      action to perform at button press
         * @param accelerator key combination to trigger button action
         * @return ButtonFactory
         */
        public ButtonFactory addButton(final String label, final Runnable action, final String accelerator) {
            parent.buttons.offer(button);
            return new ButtonFactory(label, action, accelerator, parent);
        }

        /**
         * Add next Button
         *
         * @param label  Label
         * @param action action to perform at button press
         * @return ButtonFactory
         */
        public ButtonFactory addButton(final String label, final Runnable action) {
            parent.buttons.offer(button);
            return new ButtonFactory(label, action, parent);
        }

        /**
         * Add next Button
         *
         * @param label Label
         * @return ButtonFactory
         */
        public ButtonFactory addButton(final String label) {
            parent.buttons.offer(button);
            return new ButtonFactory(label, parent);
        }

        /**
         * Add external Button to the ButtonPanel
         *
         * @param button button to add
         * @return ButtonFactory
         */
        public ButtonFactory addButton(final JButton button) {
            parent.buttons.offer(this.button);
            return new ButtonFactory(button, parent);
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
