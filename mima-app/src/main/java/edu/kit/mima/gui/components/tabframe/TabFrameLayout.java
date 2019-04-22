package edu.kit.mima.gui.components.tabframe;

import edu.kit.mima.gui.components.alignment.Alignment;
import edu.kit.mima.gui.components.border.MutableLineBorder;
import org.jdesktop.jxlayer.JXLayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.pbjar.jxlayer.plaf.ext.transform.DefaultTransformModel;
import org.pbjar.jxlayer.plaf.ext.transform.TransformUtils;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class TabFrameLayout implements LayoutManager {

    private static final Action EMPTY_ACTION = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    };
    final TabArea bottomTabs = new TabArea();
    final TabArea topTabs = new TabArea();
    private final TabArea leftTabs = new TabArea();
    private final TabArea rightTabs = new TabArea();
    final JXLayer<JComponent> rotatePaneLeft;
    final JXLayer<JComponent> rotatePaneRight;
    private final TabFrame tabFrame;

    private final MutableLineBorder leftBorder;
    private final MutableLineBorder topBorder;
    private final MutableLineBorder bottomBorder;
    private final MutableLineBorder rightBorder;

    private final DefaultTransformModel leftTransformModel;
    private final DefaultTransformModel rightTransformModel;

    /**
     * The width/height of the frame.
     */
    private final int size = 24;
    /**
     * The increase in capacity if arrays are full.
     */
    private final TabFrameContent content = new TabFrameContent();
    private final Map<Alignment, LinkedList<TabFrameTabComponent>> tabsMap;
    private final Map<Alignment, LinkedList<PopupComponent>> compsMap;
    private Color lineColor = Color.BLACK;

    public TabFrameLayout(@NotNull final TabFrame tabFrame) {
        this.tabFrame = tabFrame;

        rightTransformModel = new DefaultTransformModel();
        rightTransformModel.setQuadrantRotation(1);
        rightTransformModel.setScaleToPreferredSize(false);
        rotatePaneRight = TransformUtils.createTransformJXLayer(rightTabs, rightTransformModel);

        leftTransformModel = new DefaultTransformModel();
        leftTransformModel.setQuadrantRotation(3);
        leftTransformModel.setScaleToPreferredSize(false);
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

        new Timer(100, e -> {
            //            transformModel2.setRotation(transformModel2.getRotation() + 0.1);
            rotatePaneLeft.repaint();
            System.out.println(rotatePaneLeft.getBounds());
            System.out.println(leftTabs.getBounds());
        });//.start();
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
        tabComponent.setOrientation(a);
        getTab(a).add(tabComponent);

        compsForAlignment(a).add(index, c);

        var tabs = tabsForAlignment(a);
        //Adjust indices for tabs.
        var iterator = tabs.listIterator(index);
        while (iterator.hasNext()) {
            var tab = iterator.next();
            tab.setIndex(tab.getIndex() + 1);
        }
        tabs.add(index, tabComponent);

        c.setCloseAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabComponent.setSelected(false);
                notifySelectChange(tabComponent);
            }
        });
        c.setEnabled(false);
    }

    public void removeTab(final Alignment a, final int index) {
        try {
            compsForAlignment(a).get(index).close();
            var tabs = tabsForAlignment(a);
            //Adjust indices for tabs.
            var iterator = tabs.listIterator(index);
            while (iterator.hasNext()) {
                var tab = iterator.next();
                tab.setIndex(tab.getIndex() - 1);
            }
            var tab = tabs.remove(index);
            getTab(a).remove(tab);
            getTab(a).repaint();

            var comp = compsForAlignment(a).remove(index);
            comp.setCloseAction(EMPTY_ACTION);
            layoutContainer(tabFrame);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    public void addTab(@NotNull final PopupComponent c, final String title, final Icon icon,
                       @NotNull final Alignment a) {
        insertTab(c, title, icon, a, tabsForAlignment(a).size());
    }

    private LinkedList<PopupComponent> compsForAlignment(final Alignment a) {
        if (!compsMap.containsKey(a)) {
            compsMap.put(a, new LinkedList<>());
        }
        return compsMap.get(a);
    }

    private LinkedList<TabFrameTabComponent> tabsForAlignment(final Alignment a) {
        if (!tabsMap.containsKey(a)) {
            tabsMap.put(a, new LinkedList<>());
        }
        return tabsMap.get(a);
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


    @NotNull
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        var b = content.getMinimumSize();
        return new Dimension(leftTabs.getWidth() + rightTabs.getWidth() + b.width,
                             topTabs.getHeight() + bottomTabs.getHeight() + b.height);
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public void layoutContainer(@NotNull Container parent) {
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

        /*
         * In case the dimensions are uneven we need to shift the rotation center to prevent the
         * rotated pane from being shifted over half a unit. Usually this wouldn't be a major
         * problem but it made the line borders look too thin compared to the top and bottom ones.
         *
         * I only partly understand why the offsets need to be different for the left and right
         * panel, but it already took far too long to figure out what was causing the issue, so I
         *  won't be looking into this any further.
         */
        double d = rightTabs.getWidth() % 2 == 1 ? 1 : 0;
        rightTransformModel.setRotationCenter(
                new Point2D.Double(rightTabs.getHeight() / 2.0, (rightTabs.getWidth() + d) / 2.0));
        rightTransformModel.invalidate();
        leftTransformModel.setRotationCenter(
                new Point2D.Double((leftTabs.getHeight() + d) / 2.0, leftTabs.getWidth() / 2.0));
        leftTransformModel.invalidate();

        leftBorder.setRight(topSize > 0 ? 0 : 1);
        leftBorder.setLeft(bottomSize > 0 ? 0 : 1);
        rightBorder.setRight(bottomSize > 0 ? 0 : 1);
        rightBorder.setLeft(topSize > 0 ? 0 : 1);

        content.setBounds(rotatePaneLeft.getWidth(), topTabs.getHeight(),
                          dim.width - rotatePaneLeft.getWidth() - rotatePaneRight.getWidth(),
                          dim.height - topTabs.getHeight() - bottomTabs.getHeight());
    }

    private void layoutTopTab(Dimension dim, int topSize, int leftSize, int rightSize) {
        if (topSize > 0) {
            topTabs.setBounds(0, 0, dim.width, size);
            layoutHorizontal(dim, Alignment.NORTH, Alignment.NORTH_EAST, 0, leftSize, rightSize);
        } else {
            topTabs.setBounds(0, 0, 0, 0);
        }
    }

    private void layoutBottomTab(Dimension dim, int bottomSize, int leftSize, int rightSize) {
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

    private void layoutLeftTab(Dimension dim, int leftSize) {
        if (leftSize > 0) {
            rotatePaneLeft.setBounds(0, topTabs.getHeight(), size,
                                     dim.height - topTabs.getHeight() - bottomTabs.getHeight() + 1);
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

    private void layoutRightTab(Dimension dim, int rightSize) {
        if (rightSize > 0) {
            rotatePaneRight.setBounds(dim.width - size, topTabs.getHeight() - 1, size,
                                      dim.height - topTabs.getHeight() - bottomTabs.getHeight()
                                      + 2);
            rightTabs.setPreferredSize(
                    new Dimension(rotatePaneRight.getHeight(), rotatePaneRight.getWidth()));
            rightTabs.setSize(rightTabs.getPreferredSize());
            var start = new Point(0, 0);
            int topEnd = layoutTabArea(start, Alignment.EAST, true, size - 1);
            start.x = rightTabs.getWidth() - 1;
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
        }
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
                for (var c : compsForAlignment(a)) {
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
            setOpaque(true);
        }

        @Override
        public void paint(@NotNull Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
            paintChildren(g);
            paintBorder(g);
        }
    }

    public void setContent(JComponent c) {
        content.setContentPane(c);
    }
}
