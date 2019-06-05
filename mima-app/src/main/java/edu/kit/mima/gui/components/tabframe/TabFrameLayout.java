package edu.kit.mima.gui.components.tabframe;

import edu.kit.mima.gui.components.alignment.Alignment;
import edu.kit.mima.gui.components.border.MutableLineBorder;
import edu.kit.mima.gui.components.tabframe.popuptab.PopupComponent;
import org.jdesktop.jxlayer.JXLayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.pbjar.jxlayer.plaf.ext.transform.DefaultTransformModel;
import org.pbjar.jxlayer.plaf.ext.transform.TransformUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class TabFrameLayout implements LayoutManager {

    private static final Action EMPTY_ACTION = new AbstractAction() {
        @Override
        public void actionPerformed(final ActionEvent e) {
        }
    };
    private final TabArea bottomTabs = new TabArea();
    private final TabArea topTabs = new TabArea();
    private final TabArea leftTabs = new TabArea();
    private final TabArea rightTabs = new TabArea();
    private final JXLayer<JComponent> rotatePaneLeft;
    private final JXLayer<JComponent> rotatePaneRight;
    private final TabFrame tabFrame;

    private final MutableLineBorder leftBorder;
    private final MutableLineBorder topBorder;
    private final MutableLineBorder bottomBorder;
    private final MutableLineBorder rightBorder;

    /**
     * The width/height of the frame.
     */
    private final int size = 24;
    private final TabFrameContent content = new TabFrameContent();
    private final Map<Alignment, List<TabFrameTabComponent>> tabsMap;
    private final Map<Alignment, List<PopupComponent>> compsMap;
    private final Map<Alignment, Integer> indexMap;
    private Color lineColor = Color.BLACK;

    public TabFrameLayout(@NotNull final TabFrame tabFrame) {
        this.tabFrame = tabFrame;

        DefaultTransformModel rightTransformModel = new DefaultTransformModel();
        rightTransformModel.setQuadrantRotation(1);
        rightTransformModel.setScaleToPreferredSize(true);
        rotatePaneRight = TransformUtils.createTransformJXLayer(rightTabs, rightTransformModel);

        DefaultTransformModel leftTransformModel = new DefaultTransformModel();
        leftTransformModel.setQuadrantRotation(3);
        leftTransformModel.setScaleToPreferredSize(true);
        rotatePaneLeft = TransformUtils.createTransformJXLayer(leftTabs, leftTransformModel);

        tabFrame.add(content);
        tabFrame.add(topTabs);
        tabFrame.add(bottomTabs);
        tabFrame.add(rotatePaneLeft);
        tabFrame.add(rotatePaneRight);


        topBorder = new MutableLineBorder(0, 0, 1, 0, lineColor);
        bottomBorder = new MutableLineBorder(1, 0, 0, 0, lineColor);
        rightBorder = new MutableLineBorder(0, 0, 1, 0, lineColor);
        leftBorder = new MutableLineBorder(0, 0, 1, 0, lineColor);

        topTabs.setBorder(topBorder);
        bottomTabs.setBorder(bottomBorder);
        rightTabs.setBorder(rightBorder);
        leftTabs.setBorder(leftBorder);

        tabsMap = new HashMap<>();
        compsMap = new HashMap<>();
        indexMap = new HashMap<>();
        for (var a : Alignment.values()) {
            indexMap.put(a, 0);
        }
    }

    public void setLineColor(final Color lineColor) {
        this.lineColor = lineColor;
        topBorder.setColor(this.lineColor);
        bottomBorder.setColor(this.lineColor);
        leftBorder.setColor(this.lineColor);
        rightBorder.setColor(this.lineColor);
    }

    public void insertTab(@NotNull final PopupComponent c, final String title, final Icon icon,
                          @NotNull final Alignment a, final int index) {
        if (a == Alignment.CENTER) {
            return;
        }
        var tabComponent = new TabFrameTabComponent(title, icon, a, index, this);
        tabComponent.updateUI();
        insertTabComp(tabComponent, a, index);
        compsForAlignment(a).add(index, c);

        c.setCloseAction(new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                tabComponent.setSelected(false);
                notifySelectChange(tabComponent);
            }
        });
        c.setEnabled(false);
    }

    private void insertTabComp(@NotNull final TabFrameTabComponent tabComp, final Alignment a, final int index) {
        tabComp.setOrientation(a);
        getTab(a).add(tabComp);
        var tabs = tabsForAlignment(a);
        //Adjust indices for tabs.
        var iterator = tabs.listIterator(index);
        while (iterator.hasNext()) {
            var tab = iterator.next();
            tab.setIndex(tab.getIndex() + 1);
        }
        tabComp.setIndex(index);
        tabComp.setAlignment(a);
        tabs.add(index, tabComp);
    }

    public void removeTab(final Alignment a, final int index) {
        try {
            compsForAlignment(a).get(index).close();
            removeTabComp(a, index);

            var comp = compsForAlignment(a).remove(index);
            comp.setCloseAction(EMPTY_ACTION);
            layoutContainer(tabFrame);
            getTab(a).repaint();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    private void removeTabComp(final Alignment a, final int index) {
        var tabs = tabsForAlignment(a);
        //Adjust indices for tabs.
        var iterator = tabs.listIterator(index);
        while (iterator.hasNext()) {
            var tab = iterator.next();
            tab.setIndex(tab.getIndex() - 1);
        }
        var tab = tabs.remove(index);
        getTab(a).remove(tab);
    }

    public void moveTab(@NotNull final TabFrameTabComponent tabComp, final Alignment a) {
        if (a == tabComp.getAlignment()) {
            return;
        }
        boolean oldSelected = tabComp.isSelected();
        var oldAlign = tabComp.getAlignment();
        int index = tabComp.getIndex();
        compsForAlignment(oldAlign).get(index).close();
        removeTabComp(oldAlign, index);

        var comp = compsForAlignment(oldAlign).remove(index);
        int newIndex = tabsForAlignment(a).size();

        insertTabComp(tabComp, a, newIndex);
        compsForAlignment(a).add(newIndex, comp);
        tabComp.setPopupVisible(oldSelected);

        layoutContainer(tabFrame);
        getTab(oldAlign).repaint();
        getTab(a).repaint();
    }

    public void addTab(@NotNull final PopupComponent c, final String title, final Icon icon,
                       @NotNull final Alignment a) {
        insertTab(c, title, icon, a, tabsForAlignment(a).size());
    }

    public List<PopupComponent> compsForAlignment(final Alignment a) {
        if (!compsMap.containsKey(a)) {
            compsMap.put(a, new LinkedList<>());
        }
        return compsMap.get(a);
    }

    public List<TabFrameTabComponent> tabsForAlignment(final Alignment a) {
        if (!tabsMap.containsKey(a)) {
            tabsMap.put(a, new LinkedList<>());
        }
        return tabsMap.get(a);
    }

    public int lastSelectedIndex(final Alignment a) {
        return indexMap.get(a);
    }

    @Contract(pure = true)
    public TabArea getTab(final Alignment a) {
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
    public Dimension preferredLayoutSize(final Container parent) {
        var b = content.getPreferredSize();
        return new Dimension(leftTabs.getWidth() + rightTabs.getWidth() + b.width,
                topTabs.getHeight() + bottomTabs.getHeight() + b.height);
    }


    @NotNull
    @Override
    public Dimension minimumLayoutSize(final Container parent) {
        var b = content.getMinimumSize();
        return new Dimension(leftTabs.getWidth() + rightTabs.getWidth() + b.width,
                topTabs.getHeight() + bottomTabs.getHeight() + b.height);
    }

    @Override
    public void addLayoutComponent(final String name, final Component comp) {
    }

    @Override
    public void removeLayoutComponent(final Component comp) {
    }

    @Override
    public void layoutContainer(@NotNull final Container parent) {
        var dim = parent.getBounds().getSize();
        int topSize = tabsForAlignment(Alignment.NORTH).size() + tabsForAlignment(
                Alignment.NORTH_EAST).size();
        int bottomSize = tabsForAlignment(Alignment.SOUTH).size() + tabsForAlignment(
                Alignment.SOUTH_WEST).size();
        int leftSize = tabsForAlignment(Alignment.WEST).size() + tabsForAlignment(
                Alignment.NORTH_WEST).size();
        int rightSize = tabsForAlignment(Alignment.EAST).size() + tabsForAlignment(
                Alignment.SOUTH_EAST).size();
        layoutTopTab(dim, topSize, leftSize, rightSize);
        layoutBottomTab(dim, bottomSize, leftSize, rightSize);
        layoutLeftTab(dim, leftSize);
        layoutRightTab(dim, rightSize);

        leftBorder.setRight(topSize > 0 ? 0 : 1);
        leftBorder.setLeft(bottomSize > 0 ? 0 : 1);
        rightBorder.setRight(bottomSize > 0 ? 0 : 1);
        rightBorder.setLeft(topSize > 0 ? 0 : 1);

        content.setBounds(rotatePaneLeft.getWidth(), topTabs.getHeight(),
                dim.width - rotatePaneLeft.getWidth() - rotatePaneRight.getWidth(),
                dim.height - topTabs.getHeight() - bottomTabs.getHeight());
    }

    private void layoutTopTab(final Dimension dim, final int topSize, final int leftSize, final int rightSize) {
        if (topSize > 0) {
            topTabs.setBounds(0, 0, dim.width, size);
            layoutHorizontal(dim, Alignment.NORTH, Alignment.NORTH_EAST, 0, leftSize, rightSize);
        } else {
            topTabs.setBounds(0, 0, 0, 0);
        }
    }

    private void layoutBottomTab(final Dimension dim, final int bottomSize, final int leftSize, final int rightSize) {
        if (bottomSize > 0) {
            bottomTabs.setBounds(0, dim.height - size, dim.width, size);
            layoutHorizontal(dim, Alignment.SOUTH_WEST, Alignment.SOUTH, 1, leftSize, rightSize);
        } else {
            bottomTabs.setBounds(0, 0, 0, 0);
        }
    }

    private void layoutHorizontal(final Dimension dim, final Alignment left, final Alignment right,
                                  final int yOff, final int leftSize, final int rightSize) {
        var start = new Point(leftSize > 0 ? size : 0, yOff);
        int leftEnd = layoutTabArea(start, left, true, size - 1);
        start.x = rightSize > 0 ? dim.width - size : dim.width;
        int rightStart = layoutTabArea(start, right, false, size - 1);
        if (rightStart < leftEnd) {
            shift(leftEnd - rightStart, right);
        }
    }

    private void layoutLeftTab(final Dimension dim, final int leftSize) {
        if (leftSize > 0) {
            rotatePaneLeft.setBounds(0, topTabs.getHeight(), size,
                    dim.height - topTabs.getHeight() - bottomTabs.getHeight()
                            + (dim.height - topTabs.getHeight() - bottomTabs.getHeight())
                                      % 2);
            leftTabs.setPreferredSize(
                    new Dimension(rotatePaneLeft.getHeight(), rotatePaneLeft.getWidth()));
            leftTabs.setSize(leftTabs.getPreferredSize());
            var start = new Point(rotatePaneLeft.getHeight(), 0);
            int topStart = layoutTabArea(start, Alignment.NORTH_WEST, false, size - 1);
            start.x = 0;
            int bottomEnd = layoutTabArea(start, Alignment.WEST, true, size - 1);
            if (bottomEnd > topStart) {
                shift(topStart - bottomEnd, Alignment.WEST);
            }
        } else {
            leftTabs.setBounds(0, 0, 0, 0);
            rotatePaneLeft.setBounds(0, 0, 0, 0);
        }
    }

    private void layoutRightTab(final Dimension dim, final int rightSize) {
        if (rightSize > 0) {
            rotatePaneRight.setBounds(dim.width - size, topTabs.getHeight(), size,
                    dim.height - topTabs.getHeight() - bottomTabs.getHeight()
                            + (dim.height - topTabs.getHeight() - bottomTabs.getHeight()) % 2);
            rightTabs.setPreferredSize(
                    new Dimension(rotatePaneRight.getHeight(), rotatePaneRight.getWidth()));
            rightTabs.setSize(rightTabs.getPreferredSize());
            var start = new Point(0, 0);
            int topEnd = layoutTabArea(start, Alignment.EAST, true, size - 1);
            start.x = rightTabs.getWidth();
            var bottomStart = layoutTabArea(start, Alignment.SOUTH_EAST, false, size - 1);
            if (bottomStart < topEnd) {
                shift(topEnd - bottomStart, Alignment.SOUTH_EAST);
            }
        } else {
            rightTabs.setBounds(0, 0, 0, 0);
            rotatePaneRight.setBounds(0, 0, 0, 0);
        }
    }

    private void shift(final int shift, final Alignment a) {
        for (var c : tabsForAlignment(a)) {
            var pos = c.getLocation();
            pos.x += shift;
            c.setLocation(pos);
        }
    }

    protected int layoutTabArea(@NotNull final Point start, @NotNull final Alignment a,
                                final boolean forward, final int size) {
        int x = start.x;
        int y = start.y;
        var bounds = new Rectangle(0, 0, 0, 0);
        for (var c : tabsForAlignment(a)) {
            var prefSize = c.getMinimumSize();
            bounds.width = prefSize.width;
            bounds.height = size;
            if (forward) {
                bounds.x = x;
                bounds.y = y;
                x += bounds.width;
            } else {
                x -= bounds.width;
                bounds.x = x;
                bounds.y = y;
            }
            c.setBounds(bounds);
        }
        return x;
    }

    private void setCompVisibility(@NotNull final TabFrameTabComponent tabComponent,
                                   final boolean selected) {
        var a = tabComponent.getAlignment();
        var c = compsForAlignment(a).get(tabComponent.getIndex());
        c.setEnabled(selected);
        if (selected) {
            c.open();
            c.requestFocus();
        }
        content.setPopupComponent(a, c);
        content.setEnabled(a, selected);
        content.updateSizes();
    }

    public void notifySelectChange(final TabFrameTabComponent tabComponent) {
        if (tabComponent == null) {
            return;
        }
        indexMap.put(tabComponent.getAlignment(), tabComponent.getIndex());
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
                for (var c : compsForAlignment(a)) {
                    if (c != null) {
                        c.setAlignment(a, content.getStatus());
                    }
                }
            }
        }
    }

    TabFrameContent getTabFrameContent() {
        return content;
    }

    public void setContent(final JComponent c) {
        content.setContentPane(c);
    }

    final class TabArea extends JPanel {

        private TabArea() {
            setLayout(null);
            setOpaque(true);
        }

        @Override
        public void paint(@NotNull final Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
            paintChildren(g);
            paintBorder(g);
        }
    }
}
