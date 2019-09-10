package edu.kit.mima.gui.components.tabframe.popuptab;

import com.weis.darklaf.components.border.AdaptiveLineBorder;
import edu.kit.mima.gui.icon.Icons;
import com.weis.darklaf.components.alignment.Alignment;
import edu.kit.mima.gui.components.button.IconButton;
import edu.kit.mima.gui.components.tabbedpane.DnDTabbedPane;
import edu.kit.mima.gui.components.tabbedpane.TabAddon;
import edu.kit.mima.gui.components.tabbedpane.TabContainer;
import edu.kit.mima.gui.laf.DarkDnDTabbedPaneUI;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.UIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.function.Supplier;

/**
 * Implementation of {@link PopupComponent} using a TabbedPane.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class TabbedPopupComponent extends SimplePopupComponent {

    private final DnDTabbedPane<JComponent> tabbedPane;
    private final PopupTabbedPaneUI ui;
    private final Supplier<JComponent> componentSupplier;
    private PopupTabContainer tabContainer;

    public TabbedPopupComponent(final String title) {
        this(title, () -> {
            var p = new JTextPane();
            p.setText("Pane");
            p.setName("Pane (Default)");
            return p;
        });
    }

    public TabbedPopupComponent(final String title, @NotNull final Supplier<JComponent> componentSupplier) {
        this.componentSupplier = componentSupplier;
        setLayout(new BorderLayout());
        var label = new UILabel(title + ":");

        tabbedPane = new DnDTabbedPane<>() {
            @Override
            public Insets getTabInsets() {
                return new Insets(0, label.getWidth() + 10, 0,
                                  closeButton.getPreferredSize().width + 2);
            }

            @Override
            public TabContainer createTabContainer() {
                tabContainer = new PopupTabContainer(this);
                return tabContainer;
            }

            @Override
            public Dimension getMinimumTabAreaSize() {
                return new Dimension(0, 25);
            }

            @Override
            public void doLayout() {
                super.doLayout();
                var bounds = getTabAreaBound();
                var prefSize = label.getPreferredSize();
                int y = (bounds.height - prefSize.height) / 2;
                label.setBounds(5, y, prefSize.width, prefSize.height);

                prefSize = closeButton.getPreferredSize();
                y = (bounds.height - prefSize.height) / 2;
                closeButton.setBounds(bounds.x + bounds.width, y, prefSize.width, prefSize.height);
            }
        };
        tabbedPane.addTabClosedEventHandler(this::onTabClose);
        tabbedPane.putClientProperty("lineThrough", Boolean.TRUE);

        tabbedPane.add(label);
        tabbedPane.add(closeButton);

        var initComp = componentSupplier.get();
        tabbedPane.addTab(initComp.getName(), initComp);

        ui = new PopupTabbedPaneUI();
        tabbedPane.setUI(ui);
        tabbedPane.setBorder(null);
        add(tabbedPane, BorderLayout.CENTER);
    }

    protected void onTabClose(final Component closed) {
    }

    @Override
    public void setAlignment(final Alignment a, final boolean[] info) {
        var insets = getBorderSize(a, info);
        setBorder(new AdaptiveLineBorder(insets.top, insets.left, insets.bottom, insets.right,
                                         "TabFramePopup.borderColor"));
    }

    @Override
    public void setFocus(final boolean focus) {
        ui.setFocus(focus);
        tabbedPane.repaint();
    }

    public final class PopupTabbedPaneUI extends DarkDnDTabbedPaneUI {

        private Color defaultSelectedBackground;

        @Override
        protected void setupColors() {
            selectedColor = UIManager.getColor("DnDTabbedPane.selectionAccent");
            tabBorderColor = UIManager.getColor("TabFramePopup.borderColor");
            selectedBackground = UIManager.getColor("DnDTabbedPane.selectedTab2");
            dropColor = UIManager.getColor("DnDTabbedPane.dropColor");
            defaultSelectedBackground = selectedBackground;
        }

        private void setFocus(final boolean focus) {
            if (focus) {
                tabBackground = headerFocusBackground;
                selectedBackground = headerFocusBackground != null ? headerFocusBackground.darker() : defaultSelectedBackground;
            } else {
                tabBackground = headerBackground;
                selectedBackground = defaultSelectedBackground;
            }
        }
    }

    private final class PopupTabContainer extends TabContainer {

        private final UIButton addButton;
        private final List<TabAddon> addons;
        private boolean visible;
        private int maxVisible;

        private PopupTabContainer(final DnDTabbedPane tabPane) {
            super(tabPane);
            addButton = new UIButton(Icons.ADD);
            addButton.setAction(new AbstractAction() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    var comp = componentSupplier.get();
                    tabPane.addTab(comp.getName(), comp);
                }
            });
            add(addButton);
            addons = List.of(addButton);
        }

        @Override
        public void showStash(final int minVisible, final int maxVisible) {
            super.showStash(minVisible, maxVisible);
            this.maxVisible = maxVisible;
            visible = true;
        }

        @Override
        public void hideStash() {
            super.hideStash();
            visible = false;
        }

        @Override
        public void doLayout() {
            super.doLayout();
            layoutAddons();
        }

        @Override
        public void layoutAddons() {
            super.layoutAddons();
            layoutAdd(visible ? maxVisible : tabbedPane.getTabCount() - 1);
        }

        private void layoutAdd(final int lastIndex) {
            var bounds = lastIndex >= 0
                         ? tabPane.getTabComponentAt(lastIndex).getBounds()
                         : new Rectangle(0, 0, 0, 0);
            var maxX = bounds.x + bounds.width;
            var prefBounds = addButton.getPreferredSize();
            var areaBounds = tabPane.getTabAreaBound();
            int y = (areaBounds.height - prefBounds.height) / 2;
            addButton.setBounds(maxX + 2, y, prefBounds.width, prefBounds.height);
        }

        @Contract(pure = true)
        @Override
        public List<TabAddon> getAddons() {
            return addons;
        }
    }

    private final class UIButton extends IconButton implements UIResource, TabAddon {

        private UIButton(@NotNull final Icon icon) {
            super(icon);
        }

        @Contract(pure = true)
        @Override
        public int getPlacement() {
            return TabAddon.LEFT;
        }


        @Override
        public int getAddonWidth() {
            return getPreferredSize().width + 4;
        }

        @Override
        public int getAddonHeight() {
            return getPreferredSize().height;
        }

        @Contract(value = " -> this", pure = true)
        @Override
        public Component getComponent() {
            return this;
        }
    }

    private final class UILabel extends JLabel implements UIResource {
        private UILabel(final String title) {
            super(title);
        }
    }
}
