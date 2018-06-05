package edu.kit.mima.gui.button;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.Queue;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ButtonPanelFactory {

    private final Queue<JButton> buttons;

    public ButtonPanelFactory() {
        buttons = new LinkedList<>();
    }

    public ButtonFactory addButton(String label, Runnable action, String accelerator) {
        return new ButtonFactory(label, action, accelerator, this);
    }

    public ButtonFactory addButton(String label) {
        return new ButtonFactory(label, this);
    }

    public ButtonFactory addButton(String label, Runnable action) {
        return new ButtonFactory(label, action, this);
    }

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
        }

        private ButtonFactory(String label, Runnable action, ButtonPanelFactory parent) {
            this.button = new JButton(label);
            this.parent = parent;
            setAction(action);
        }

        private ButtonFactory(String label, ButtonPanelFactory parent) {
            this.button = new JButton(label);
            this.parent = parent;
        }

        private ButtonFactory(JButton button, ButtonPanelFactory parent) {
            this.button = button;
            this.parent = parent;
        }

        public ButtonFactory addAccelerator(String accelerator) {
            setAccelerator(accelerator);
            return this;
        }

        public ButtonFactory addAction(Runnable action) {
            setAction(action);
            return this;
        }

        public ButtonFactory setEnabled(boolean enabled) {
            this.button.setEnabled(enabled);
            return this;
        }

        public ButtonFactory addButton(String label, Runnable action, String accelerator) {
            this.parent.buttons.offer(this.button);
            return new ButtonFactory(label, action, accelerator, parent);
        }

        public ButtonFactory addButton(String label) {
            this.parent.buttons.offer(this.button);
            return new ButtonFactory(label, parent);
        }

        public ButtonFactory addButton(String label, Runnable action) {
            this.parent.buttons.offer(this.button);
            return new ButtonFactory(label, action, parent);
        }

        public ButtonFactory addButton(JButton button) {
            this.parent.buttons.offer(this.button);
            return new ButtonFactory(button, parent);
        }

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
        }
    }
}
