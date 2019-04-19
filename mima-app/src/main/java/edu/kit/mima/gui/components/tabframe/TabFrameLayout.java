package edu.kit.mima.gui.components.tabframe;

import edu.kit.mima.gui.components.alignment.Alignment;
import edu.kit.mima.gui.components.border.MutableLineBorder;
import org.jdesktop.jxlayer.JXLayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.pbjar.jxlayer.plaf.ext.transform.DefaultTransformModel;
import org.pbjar.jxlayer.plaf.ext.transform.TransformUtils;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class TabFrameLayout implements LayoutManager {

    private final TabArea bottomTabs = new TabArea();
    private final TabArea leftTabs = new TabArea();
    private final TabArea rightTabs = new TabArea();
    private final TabArea topTabs = new TabArea();

    private final MutableLineBorder leftBorder;
    private final MutableLineBorder rightBorder;

    public TabFrameLayout(@NotNull final TabFrame tabFrame) {
        tabFrame.setBackground(Color.ORANGE);
        tabFrame.add(content);
        tabFrame.add(topTabs);
        tabFrame.add(bottomTabs);
        tabFrame.add(leftTabs);
        tabFrame.add(rightTabs);

        var lineColor = UIManager.getColor("TabFrame.line");
        topTabs.setBorder(new MutableLineBorder(0, 0, 1, 0, lineColor));
        bottomTabs.setBorder(new MutableLineBorder(1, 0, 0, 0, lineColor));
        rightBorder = new MutableLineBorder(0, 1, 0, 0, lineColor);
        leftBorder = new MutableLineBorder(0, 0, 0, 1, lineColor);

        rightTabs.setBorder(rightBorder);
        leftTabs.setBorder(leftBorder);
    }

    public JPanel getTopTabs() {
        return topTabs;
    }

    public JPanel getBottomTabs() {
        return bottomTabs;
    }

    public JPanel getLeftTabs() {
        return leftTabs;
    }

    public JPanel getRightTabs() {
        return rightTabs;
    }

    /**
     * The width/height of the frame.
     */
    private final int size = 23;
    /**
     * The increase in capacity if arrays are full.
     */
    private final int increase = 10;
    private final TabFrameContent content = new TabFrameContent();

    private final Map<TabFrameTabComponent, DefaultTransformModel>
            transformModelMap = new HashMap<>();
    private final Map<TabFrameTabComponent, JXLayer<JComponent>>
            rotationPanelMap = new HashMap<>();
    private final TabFrameTabComponent[][] tabMap = new TabFrameTabComponent[8][];
    private final PopupComponent[][] compMap = new PopupComponent[8][];
    private final int[] sizes = new int[8];

    public void insertTab(@NotNull final PopupComponent c, final String title, final Icon icon,
                          @NotNull final Alignment a, final int index) {
        indexForAlignment(a); //Check alignment
        var tabComponent = new TabFrameTabComponent(title, icon, a, index, this);
        var transformModel = new DefaultTransformModel();
        transformModel.setQuadrantRotation(getAngle(a));
        JXLayer<JComponent> rotatePane = TransformUtils
                .createTransformJXLayer(tabComponent, transformModel);
        rotatePane.setOpaque(true);
        getTab(a).add(rotatePane);

        transformModelMap.put(tabComponent, transformModel);
        rotationPanelMap.put(tabComponent, rotatePane);
        tabComponent.setOrientation(a);
        componentsForAlignment(a)[index] = c;
        tabsForAlignment(a)[index] = tabComponent;
        c.setCloseAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabComponent.setSelected(false);
                notifySelectChange(tabComponent);
            }
        });
        c.setEnabled(false);
    }


    protected PopupComponent[] componentsForAlignment(@NotNull final Alignment a) {
        int index = indexForAlignment(a);
        if (compMap[index] == null) {
            compMap[index] = new PopupComponent[increase];
        }
        return compMap[index];
    }

    protected TabFrameTabComponent[] tabsForAlignment(@NotNull final Alignment a) {
        int index = indexForAlignment(a);
        if (tabMap[index] == null) {
            tabMap[index] = new TabFrameTabComponent[increase];
        }
        return tabMap[index];
    }

    protected int indexForAlignment(@NotNull final Alignment a) {
        if (a == Alignment.CENTER) {
            throw new IllegalArgumentException("invalid alignment: " + a);
        } else {
            return a.getIndex();
        }
    }

    @Contract(pure = true)
    private JPanel getTab(final Alignment a) {
        return switch (a) {
            case NORTH, NORTH_EAST -> topTabs;
            case SOUTH, SOUTH_WEST -> bottomTabs;
            case EAST, SOUTH_EAST -> rightTabs;
            case WEST, NORTH_WEST -> leftTabs;
            case CENTER -> throw new IllegalArgumentException("invalid alignment: " + a);
        };
    }

    @NotNull
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        var b = content.getPreferredSize();
        return new Dimension(leftTabs.getWidth() + rightTabs.getWidth() + b.width,
                             topTabs.getHeight() + bottomTabs.getHeight() + b.height);
    }

    //    public void removeTab(final Alignment a, final int index) {
    //        var tc = tabsForAlignment(a)[index];
    //        tabFrame.remove(tc);
    //        tabsForAlignment(a)[index] = null;
    //        transformModelMap.remove(tc);
    //        rotationPanelMap.remove(tc);
    //    }

    public void addTab(@NotNull final PopupComponent c, final String title, final Icon icon,
                       @NotNull final Alignment a) {
        indexForAlignment(a); //Check alignment
        ensureSize(a);
        insertTab(c, title, icon, a, sizes[indexForAlignment(a)]);
        sizes[indexForAlignment(a)] += 1;
    }

    @NotNull
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        var b = content.getMinimumSize();
        return new Dimension(leftTabs.getWidth() + rightTabs.getWidth() + b.width,
                             topTabs.getHeight() + bottomTabs.getHeight() + b.height);
    }

    private void ensureSize(@NotNull Alignment a) {
        int size = sizes[indexForAlignment(a)];
        int oldSize = tabsForAlignment(a).length;
        if (size == oldSize) {
            PopupComponent[] newComp = new PopupComponent[oldSize + increase];
            System.arraycopy(componentsForAlignment(a), 0, newComp, 0, oldSize);
            compMap[indexForAlignment(a)] = newComp;

            TabFrameTabComponent[] newTab = new TabFrameTabComponent[oldSize + increase];
            System.arraycopy(tabsForAlignment(a), 0, newTab, 0, oldSize);
            tabMap[indexForAlignment(a)] = newTab;
        }
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public void layoutContainer(@NotNull Container parent) {
        var dim = parent.getSize();
        int topSize = sizes[indexForAlignment(Alignment.NORTH)] + sizes[indexForAlignment(
                Alignment.NORTH_EAST)];
        int bottomSize = sizes[indexForAlignment(Alignment.SOUTH)] + sizes[indexForAlignment(
                Alignment.SOUTH_WEST)];
        int leftSize = sizes[indexForAlignment(Alignment.WEST)] + sizes[indexForAlignment(
                Alignment.NORTH_WEST)];
        int rightSize = sizes[indexForAlignment(Alignment.EAST)] + sizes[indexForAlignment(
                Alignment.SOUTH_EAST)];
        layoutTopTab(dim, topSize, leftSize, rightSize);
        layoutBottomTab(dim, bottomSize, leftSize, rightSize);
        layoutLeftTab(dim, leftSize);
        layoutRightTab(dim, rightSize);

        leftBorder.setTop(topSize > 0 ? 0 : 1);
        leftBorder.setBottom(bottomSize > 0 ? 0 : 1);
        rightBorder.setBottom(bottomSize > 0 ? 0 : 1);
        rightBorder.setTop(topSize > 0 ? 0 : 1);

        content.setBounds(leftTabs.getWidth(), topTabs.getHeight(),
                          dim.width - leftTabs.getWidth() - rightTabs.getWidth() - 1,
                          dim.height - topTabs.getHeight() - bottomTabs.getHeight() - 1);
    }

    @Contract(pure = true)
    private int getAngle(final Alignment alignment) {
        return switch (alignment) {
            case EAST, SOUTH_EAST -> 1;
            case WEST, NORTH_WEST -> 3;
            default -> 0;
        };
    }

    private void layoutTopTab(Dimension dim, int topSize, int leftSize, int rightSize) {
        if (topSize > 0) {
            topTabs.setBounds(0, 0, dim.width, size);

            var start = new Point(leftSize > 0 ? size : 0, 0);
            int topLeftEnd = layoutHorizontalTab(start, Alignment.NORTH, true);
            start.x = rightSize > 0 ? dim.width - 1 - size : dim.width - 1;
            var rightEnd = layoutHorizontalTab(start, Alignment.NORTH_EAST, false);
            if (rightEnd < topLeftEnd) {
                shiftHorizontal(topLeftEnd - rightEnd, Alignment.NORTH_EAST);
            }
        } else {
            topTabs.setBounds(0, 0, 0, 0);
        }
    }

    private void layoutBottomTab(Dimension dim, int bottomSize, int leftSize, int rightSize) {
        if (bottomSize > 0) {
            bottomTabs.setBounds(0, dim.height - 1 - size, dim.width, size);

            var start = new Point(leftSize > 0 ? size : 0, 1);
            int bottomLeftEnd = layoutHorizontalTab(start, Alignment.SOUTH_WEST, true);
            start.x = rightSize > 0 ? dim.width - 1 - size : dim.width - 1;
            var rightEnd = layoutHorizontalTab(start, Alignment.SOUTH, false);
            if (rightEnd < bottomLeftEnd) {
                shiftHorizontal(bottomLeftEnd - rightEnd, Alignment.SOUTH);
            }
        } else {
            bottomTabs.setBounds(0, 0, 0, 0);
        }
    }

    private void layoutLeftTab(Dimension dim, int leftSize) {
        if (leftSize > 0) {
            leftTabs.setBounds(0, topTabs.getHeight(), size,
                               dim.height - topTabs.getHeight() - bottomTabs.getHeight());
            var start = new Point(0, leftTabs.getY());
            int leftTopEnd = layoutVerticalTab(start, Alignment.NORTH_WEST, true);
            start.y += leftTabs.getHeight() - 1;
            var bottomEnd = layoutVerticalTab(start, Alignment.WEST, false);
            if (bottomEnd < leftTopEnd) {
                shiftVertical(leftTopEnd - bottomEnd, Alignment.WEST);
            }
        } else {
            leftTabs.setBounds(0, 0, 0, 0);
        }
    }

    private void layoutRightTab(Dimension dim, int rightSize) {
        if (rightSize > 0) {
            rightTabs.setBounds(dim.width - 1 - size, topTabs.getHeight(), size,
                                dim.height - topTabs.getHeight() - bottomTabs.getHeight());

            var start = new Point(1, rightTabs.getY());
            int rightTopEnd = layoutVerticalTab(start, Alignment.EAST, true);
            start.y += rightTabs.getHeight() - 1;
            var bottomEnd = layoutVerticalTab(start, Alignment.SOUTH_EAST, false);
            if (bottomEnd < rightTopEnd) {
                shiftVertical(rightTopEnd - bottomEnd, Alignment.SOUTH_EAST);
            }
        } else {
            rightTabs.setBounds(0, 0, 0, 0);
        }
    }

    private void shiftHorizontal(final int shift, final Alignment a) {
        int length = sizes[indexForAlignment(a)];
        var tabComps = tabsForAlignment(a);
        for (int i = 0; i < length; i++) {
            var pos = rotationPanelMap.get(tabComps[i]).getLocation();
            pos.x += shift;
            rotationPanelMap.get(tabComps[i]).setLocation(pos);
        }
    }

    private void shiftVertical(final int shift, final Alignment a) {
        int length = sizes[indexForAlignment(a)];
        var tabComps = tabsForAlignment(a);
        for (int i = 0; i < length; i++) {
            var pos = rotationPanelMap.get(tabComps[i]).getLocation();
            pos.y += shift;
            rotationPanelMap.get(tabComps[i]).setLocation(pos);
        }
    }

    protected int layoutHorizontalTab(@NotNull final Point start, @NotNull final Alignment a,
                                       final boolean forward) {
        int length = sizes[indexForAlignment(a)];
        var tabComps = tabsForAlignment(a);
        int x = start.x;
        int y = start.y;
        var bounds = new Rectangle(0, 0, 0, 0);
        for (int i = 0; i < length; i++) {
            var prefSize = tabComps[i].getMinimumSize();
            bounds.width = prefSize.width;
            bounds.height = size - 1;
            if (forward) {
                bounds.x = x;
                bounds.y = y;
                x += bounds.width;
            } else {
                x -= bounds.width;
                bounds.x = x;
                bounds.y = y;
            }
            tabComps[i].setPreferredSize(bounds.getSize());
            rotationPanelMap.get(tabComps[i]).setBounds(bounds);
        }
        return x;
    }

    protected int layoutVerticalTab(@NotNull final Point start, @NotNull final Alignment a,
                                    final boolean downwards) {
        int length = sizes[indexForAlignment(a)];
        var tabComps = tabsForAlignment(a);
        int x = start.x;
        int y = start.y - topTabs.getHeight();
        var bounds = new Rectangle(0, 0, 0, 0);
        for (int i = 0; i < length; i++) {
            var prefSize = tabComps[i].getMinimumSize();
            //noinspection SuspiciousNameCombination
            bounds.height = prefSize.width;
            bounds.width = size - 1;
            if (downwards) {
                bounds.x = x;
                bounds.y = y;
                y += bounds.height;
            } else {
                y -= bounds.height;
                bounds.x = x;
                bounds.y = y;
            }
            //noinspection SuspiciousNameCombination
            tabComps[i].setPreferredSize(new Dimension(bounds.height, bounds.width));
            rotationPanelMap.get(tabComps[i]).setBounds(bounds);
        }
        return y;
    }

    private void setCompVisibility(@NotNull final TabFrameTabComponent tabComponent,
                                   final boolean selected) {
        var a = tabComponent.getAlignment();
        var c = componentsForAlignment(a)[tabComponent.getIndex()];
        c.setEnabled(selected);
        c.open();
        content.setPopupComponent(a, c);
        content.setEnabled(a, selected);
        content.updateSizes();
    }

    public void notifySelectChange(@NotNull final TabFrameTabComponent tabComponent) {
        if (tabComponent.isSelected()) {
            for (var tc : tabsForAlignment(tabComponent.getAlignment())) {
                if (tc != null && tc != tabComponent) {
                    tc.setSelected(false);
                }
            }
        }
        setCompVisibility(tabComponent, tabComponent.isSelected());
        updateAlignments();
    }

    private void updateAlignments() {
        for (var a : Alignment.values()) {
            if (a != Alignment.CENTER) {
                for (var c : componentsForAlignment(a)) {
                    if (c != null) {
                        c.setAlignment(a, content.getStatus());
                    }
                }
            }
        }
    }

    private class TabArea extends JPanel {

        private TabArea() {
            setLayout(null);
            setOpaque(false);
        }

        @Override
        public void paint(@NotNull Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paint(g);
        }
    }

    public void setContent(JComponent c) {
        content.setContentPane(c);
    }
}
