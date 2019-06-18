package edu.kit.mima.gui.components.tabframe.popuptab;

import edu.kit.mima.gui.components.console.terminal.Terminal;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

/**
 * Popup Component that offers OS dependent terminal session.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class TerminalPopupComponent extends TabbedPopupComponent {

    private static final Supplier<JComponent> COMPONENT_SUPPLIER = () -> {
        var terminal = Terminal.createTerminal();
        if (terminal != null) {
            var comp = terminal.getComponent();
            comp.setName("Local");
            return comp;
        } else {
            return null;
        }
    };

    public TerminalPopupComponent(final String title) {
        super(title, COMPONENT_SUPPLIER);
    }

    @Override
    protected void onTabClose(final Component closed) {
        super.onTabClose(closed);
        if (closed instanceof Terminal) {
            try {
                ((Terminal) closed).close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
