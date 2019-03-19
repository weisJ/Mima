package edu.kit.mima.gui.laf;

import javax.swing.UIManager;

/**
 * {@link javax.swing.UIManager.LookAndFeelInfo} for {@link CustomDarculaLaf}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class DarkLafInfo extends UIManager.LookAndFeelInfo {
    /**
     * Constructs a {@link UIManager}s {@link javax.swing.UIManager.LookAndFeelInfo} object.
     */
    public DarkLafInfo() {
        super("Dark", CustomDarculaLaf.class.getCanonicalName());
    }
}
