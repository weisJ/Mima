package edu.kit.mima.gui.button;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.util.Queue;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ButtonPanelFactory {

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
     * @param label Label
     * @param action Action to perform when clicked
     * @param accelerator Key combination to trigger button event
     * @return ButtonFactory
     */
    public ButtonFactory addButton(String label, Runnable action, String accelerator) {
        return new ButtonFactory(label, action, accelerator, this);
    }

    /**
     * Add Button to the ButtonPanel
     *
     * @param label Label
     * @param action Action to perform when clicked
     * @return ButtonFactory
     */
    public ButtonFactory addButton(String label, Runnable action) {
        return new ButtonFactory(label, action, this);
    }

    /**
     * Add Button to the ButtonPanel
     *
     * @param label Label
     * @return ButtonFactory
     */
    public ButtonFactory addButton(String label) {
        return new ButtonFactory(label, this);
    }

    /**
     * Add external Button to the ButtonPanel
     *
     * @param button button to add
     * @return ButtonFactory
     */
    public ButtonFactory addButton(JButton button) {
        return new ButtonFactory(button, this);
    }

    public class ButtonFactory {

        private final JButton button;
        private final ButtonPanelFactory parent;

        private ButtonFactory(String label, Runnable action, String accelerator, ButtonPanelFactory parent) {
            this.button = new JButton(label);
            this.parent = parent;
            setAction(action);
            setAccelerator(accelerator);
            styleButton();
        }

        private ButtonFactory(String label, Runnable action, ButtonPanelFactory parent) {
            this.button = new JButton(label);
            this.parent = parent;
            setAction(action);
            styleButton();
        }

        private ButtonFactory(String label, ButtonPanelFactory parent) {
            this.button = new JButton(label);
            this.parent = parent;
            styleButton();
        }

        private ButtonFactory(JButton button, ButtonPanelFactory parent) {
            this.button = button;
            this.parent = parent;
            styleButton();
        }

        private void styleButton() {
            this.button.setForeground(Color.BLACK);
            Border line = new LineBorder(Color.DARK_GRAY);
            Border margin = new EmptyBorder(5, 15, 5, 15);
            Border compound = new CompoundBorder(line, margin);
            this.button.setBorder(compound);
            this.button.setFocusPainted(false);
        }

        /**
         * Set the accelerator for the current button
         *
         * @param accelerator key combination to trigger button event
         * @return this
         */
        public ButtonFactory addAccelerator(String accelerator) {
            setAccelerator(accelerator);
            return this;
        }

        /**
         * Set the action for the current button
         *
         * @param action action to perform at button press
         * @return this
         */
        public ButtonFactory addAction(Runnable action) {
            setAction(action);
            return this;
        }

        /**
         * Set whether the button is enabled
         *
         * @param enabled boolean
         * @return this
         */
        public ButtonFactory setEnabled(boolean enabled) {
            this.button.setEnabled(enabled);
            return this;
        }

        /**
         * Add next Button
         *
         * @param label Label
         * @param action action to perform at button press
         * @param accelerator key combination to trigger button action
         * @return ButtonFactory
         */
        public ButtonFactory addButton(String label, Runnable action, String accelerator) {
            this.parent.buttons.offer(this.button);
            return new ButtonFactory(label, action, accelerator, parent);
        }

        /**
         * Add next Button
         *
         * @param label Label
         * @param action action to perform at button press
         * @return ButtonFactory
         */
        public ButtonFactory addButton(String label, Runnable action) {
            this.parent.buttons.offer(this.button);
            return new ButtonFactory(label, action, parent);
        }

        /**
         * Add next Button
         *
         * @param label Label
         * @return ButtonFactory
         */
        public ButtonFactory addButton(String label) {
            this.parent.buttons.offer(this.button);
            return new ButtonFactory(label, parent);
        }

        /**
         * Add external Button to the ButtonPanel
         *
         * @param button button to add
         * @return ButtonFactory
         */
        public ButtonFactory addButton(JButton button) {
            this.parent.buttons.offer(this.button);
            return new ButtonFactory(button, parent);
        }

        /**
         * Construct the ButtonPanel. Buttons are added in the order they were configured
         *
         * @return JPanel containing the buttons next to each other
         */
        public JPanel get() {
            this.parent.buttons.offer(this.button);
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(1, this.parent.buttons.size()));
            while (!this.parent.buttons.isEmpty()) {
                panel.add(this.parent.buttons.poll());
            }
            return panel;
        }

        private void setAction(Runnable action) {
            this.button.addActionListener(e -> action.run());
        }

        private void setAccelerator(String accelerator) {
            ClickAction clickAction = new ClickAction(this.button);
            this.button.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(accelerator), accelerator);
            this.button.getActionMap().put(accelerator, clickAction);
            this.button.setToolTipText(" ");
        }
    }
}
