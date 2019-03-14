package edu.kit.mima.gui.components;

import javax.swing.JTextField;
import java.awt.event.ActionListener;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class SearchField extends JTextField {

    public SearchField() {
        super();
        this.putClientProperty("JTextField.variant", "search");
        putClientProperty("JTextField.Search.CancelAction", (ActionListener) e -> setText(""));
        revalidate();
    }
}
