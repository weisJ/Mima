package edu.kit.mima.gui.components.tabframe.popuptab;

import edu.kit.mima.gui.components.alignment.Alignment;
import edu.kit.mima.gui.components.border.AdaptiveLineBorder;
import edu.kit.mima.gui.components.tabbedpane.DnDTabbedPane;
import edu.kit.mima.gui.laf.components.DarkDnDTabbedPaneUI;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class TerminalPopupComponent extends SimplePopupComponent {

    private final DnDTabbedPane tabbedPane;
    private final PopupTabbedPaneUI ui;

    public TerminalPopupComponent() {
        setLayout(new BorderLayout());
        tabbedPane = new DnDTabbedPane() {
            @Override
            public Insets getTabInsets() {
                return new Insets(0, 50, 0, 600);
            }
        };
        ui = new PopupTabbedPaneUI();
        tabbedPane.setUI(ui);
        add(tabbedPane, BorderLayout.CENTER);
        for (int i = 0; i < 4; i++) {
            var p = new JTextPane();
            p.setText("Pane " + i);
            tabbedPane.addTab("Tab " + i, p);
        }
    }

    @Override
    public void setAlignment(final Alignment a, final boolean[] info) {
        var insets = getBorderSize(a, info);
        tabbedPane.setBorder(new AdaptiveLineBorder(insets.top, insets.left, insets.bottom, insets.right,
                                                    "TabFramePopup.borderColor"));
    }

    @Override
    public void setFocus(final boolean focus) {
        ui.setFocus(focus);
    }

    private final class PopupTabbedPaneUI extends DarkDnDTabbedPaneUI {

        private Color defaultSelectedBackground;

        @Override
        protected void setupColors() {
            selectedColor = UIManager.getColor("DnDTabbedPane.selectionAccent");
            tabBorderColor = UIManager.getColor("TabFramePopup.borderColor");
            selectedBackground = UIManager.getColor("DnDTabbedPane.selectedTab");
            dropColor = UIManager.getColor("DnDTabbedPane.dropColor");
            defaultSelectedBackground = selectedBackground;
        }

        public void setFocus(final boolean focus) {
            if (focus) {
                tabBackground = headerFocusBackground;
                selectedBackground = headerFocusBackground != null ? headerFocusBackground.darker() : defaultSelectedBackground;

            } else {
                tabBackground = headerBackground;
                selectedBackground = defaultSelectedBackground;
            }
        }
    }
}
