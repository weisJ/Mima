package edu.kit.mima.gui.components;

import java.awt.event.ActionListener;
import javax.swing.JTextField;

/**
 * Search field with custom UI. Todo UI
 *
 * @author Jannis Weis
 * @since 2018
 */
public class SearchField extends JTextField {

    /**
     * Create new Search Field.
     */
    public SearchField() {
        super();
        this.putClientProperty("JTextField.variant", "search");
        putClientProperty("JTextField.Search.CancelAction", (ActionListener) e -> setText(""));
        revalidate();
    }
}
