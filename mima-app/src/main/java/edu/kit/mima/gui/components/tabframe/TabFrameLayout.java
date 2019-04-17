package edu.kit.mima.gui.components.tabframe;

import edu.kit.mima.gui.components.alignment.Alignment;
import org.jdesktop.jxlayer.JXLayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.pbjar.jxlayer.plaf.ext.transform.DefaultTransformModel;
import org.pbjar.jxlayer.plaf.ext.transform.TransformUtils;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
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

    final Rectangle topRect = new Rectangle();
    final Rectangle bottomRect = new Rectangle();
    final Rectangle leftRect = new Rectangle();
    final Rectangle rightRect = new Rectangle();

    /**
     * The width/height of the frame.
     */
    private final int size = 23;
    /**
     * The increase in capacity if arrays are full.
     */
    private final int increase = 10;
    private final TabFrameContent content = new TabFrameContent();
    @NotNull
    private final TabFrame tabFrame;

    private final Map<TabFrameTabComponent, DefaultTransformModel>
            transformModelMap = new HashMap<>();
    private final Map<TabFrameTabComponent, JXLayer<JComponent>>
            rotationPanelMap = new HashMap<>();
    private final TabFrameTabComponent[][] tabMap = new TabFrameTabComponent[8][];
    private final PopupComponent[][] compMap = new PopupComponent[8][];
    private final int[] sizes = new int[8];


    public TabFrameLayout(@NotNull final TabFrame tabFrame) {
        this.tabFrame = tabFrame;
        tabFrame.add(content);
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

    public void insertTab(@NotNull final PopupComponent c, final String title, final Icon icon,
                          @NotNull final Alignment a, final int index) {
        indexForAlignment(a); //Check alignment
        var tabComponent = new TabFrameTabComponent(title, icon, a, index, this);
        var transformModel = new DefaultTransformModel();
        transformModel.setRotation(getAngle(a));
        transformModel.setScaleToPreferredSize(true);
        JXLayer<JComponent> rotatePane = TransformUtils
                .createTransformJXLayer(tabComponent, transformModel);
        tabFrame.add(rotatePane, index);
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

    public void addTab(@NotNull final PopupComponent c, final String title, final Icon icon,
                       @NotNull final Alignment a) {
        indexForAlignment(a); //Check alignment
        ensureSize(a);
        insertTab(c, title, icon, a, sizes[indexForAlignment(a)]);
        sizes[indexForAlignment(a)] += 1;
    }

    @Contract(pure = true)
    private double getAngle(final Alignment alignment) {
        return switch (alignment) {
            case EAST, SOUTH_EAST -> Math.PI / 2;
            case WEST, NORTH_WEST -> Math.PI + Math.PI / 2;
            default -> 0;
        };
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

    @NotNull
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        var b = content.getPreferredSize();
        return new Dimension(leftRect.width + rightRect.width + b.width,
                             topRect.height + bottomRect.height + b.height);
    }

    @NotNull
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        var b = content.getMinimumSize();
        return new Dimension(leftRect.width + rightRect.width + b.width,
                             topRect.height + bottomRect.height + b.height);
    }

    @Override
    public void layoutContainer(@NotNull Container parent) {
        var dim = parent.getSize();
        int topSize = sizes[indexForAlignment(Alignment.NORTH)]
                      + sizes[indexForAlignment(Alignment.NORTH_EAST)];
        int bottomSize = sizes[indexForAlignment(Alignment.SOUTH)]
                         + sizes[indexForAlignment(Alignment.SOUTH_WEST)];
        int leftSize = sizes[indexForAlignment(Alignment.WEST)]
                       + sizes[indexForAlignment(Alignment.NORTH_WEST)];
        int rightSize = sizes[indexForAlignment(Alignment.EAST)]
                        + sizes[indexForAlignment(Alignment.SOUTH_EAST)];
        if (topSize > 0) {
            topRect.setBounds(0, 0,
                              dim.width, size);
            layoutHorizontalTab(new Point(leftSize > 0 ? size + 1 : 0, 0),
                                Alignment.NORTH, true);
            layoutHorizontalTab(new Point(rightSize > 0 ? dim.width - size - 2 : dim.width - 1,
                                          0),
                                Alignment.NORTH_EAST, false);
        } else {
            topRect.setBounds(0, 0, 0, 0);
        }
        if (bottomSize > 0) {
            bottomRect.setBounds(0, dim.height - 1 - size,
                                 dim.width, size);
            layoutHorizontalTab(new Point(rightSize > 0 ? dim.width - size - 2 : dim.width - 1,
                                          dim.height - size),
                                Alignment.SOUTH, false);
            layoutHorizontalTab(new Point(leftSize > 0 ? size + 1 : 0, dim.height - size),
                                Alignment.SOUTH_WEST, true);
        } else {
            bottomRect.setBounds(0, 0, 0, 0);
        }
        if (leftSize > 0) {
            leftRect.setBounds(0, topRect.height,
                               size, dim.height - topRect.height - bottomRect.height - 1);
            layoutVerticalTab(new Point(leftRect.x, leftRect.y + leftRect.height),
                              Alignment.WEST, false);
            layoutVerticalTab(new Point(leftRect.x, leftRect.y),
                              Alignment.NORTH_WEST, true);
        } else {
            leftRect.setBounds(0, 0, 0, 0);
        }
        if (rightSize > 0) {
            rightRect.setBounds(dim.width - 1 - size, topRect.height,
                                size, dim.height - topRect.height - bottomRect.height - 1);
            layoutVerticalTab(new Point(rightRect.x + 1, rightRect.y),
                              Alignment.EAST, true);
            layoutVerticalTab(new Point(rightRect.x + 1,
                                        rightRect.y + rightRect.height),
                              Alignment.SOUTH_EAST, false);
        } else {
            rightRect.setBounds(0, 0, 0, 0);
        }
        content.setBounds(leftRect.width, topRect.height,
                          dim.width - leftRect.width - rightRect.width - 1,
                          dim.height - topRect.height - bottomRect.height - 1);
    }


    protected void layoutHorizontalTab(@NotNull final Point start, @NotNull final Alignment a,
                                       final boolean forward) {
        int length = sizes[indexForAlignment(a)];
        var tabComps = tabsForAlignment(a);
        int x = start.x;
        int y = start.y;
        for (int i = 0; i < length; i++) {
            var bounds = new Rectangle(0, 0, 0, 0);
            var prefSize = tabComps[i].getMinimumSize();
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
            tabComps[i].setBounds(new Rectangle(0, 0, bounds.width, bounds.height));
            rotationPanelMap.get(tabComps[i]).setBounds(bounds);
        }
    }

    protected void layoutVerticalTab(@NotNull final Point start, @NotNull final Alignment a,
                                     final boolean downwards) {
        int length = sizes[indexForAlignment(a)];
        var tabComps = tabsForAlignment(a);
        int x = start.x;
        int y = start.y;
        for (int i = 0; i < length; i++) {
            var bounds = new Rectangle(0, 0, 0, 0);
            var prefSize = tabComps[i].getMinimumSize();
            //noinspection SuspiciousNameCombination
            bounds.height = prefSize.width;
            bounds.width = size;
            if (downwards) {
                bounds.x = x;
                bounds.y = y;
                y += bounds.height;
            } else {
                y -= bounds.height;
                bounds.x = x;
                bounds.y = y;
            }
            var tabBounds = new Rectangle(0, 0, 0, 0);
            tabBounds.x = (int) (bounds.width / 2.0d - bounds.height / 2.0d);
            tabBounds.y = (int) (bounds.height / 2.0d - bounds.width / 2.0d);
            //rotating swaps dimensions
            //noinspection SuspiciousNameCombination
            tabBounds.width = bounds.height;
            //noinspection SuspiciousNameCombination
            tabBounds.height = bounds.width;
            tabComps[i].setBounds(tabBounds);
            rotationPanelMap.get(tabComps[i]).setBounds(bounds);
        }
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

    private void setCompVisibility(@NotNull final TabFrameTabComponent tabComponent,
                                   final boolean selected) {
        var a = tabComponent.getAlignment();
        var c = componentsForAlignment(a)[tabComponent.getIndex()];
        c.setEnabled(selected);
        c.open();
        content.setPopupComponent(a, c);
        content.setEnabled(a, selected);
    }

    public void setContent(JComponent c) {
        content.setContentPane(c);
    }
}
