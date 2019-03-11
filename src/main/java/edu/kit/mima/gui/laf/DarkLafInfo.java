package edu.kit.mima.gui.laf;

import javax.swing.UIManager;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class DarkLafInfo extends UIManager.LookAndFeelInfo {
    /**
     * Constructs a <code>UIManager</code>s
     * <code>LookAndFeelInfo</code> object.
     */
    public DarkLafInfo() {
        super("Dark", CustomDarculaLaf.class.getCanonicalName());
    }
}
