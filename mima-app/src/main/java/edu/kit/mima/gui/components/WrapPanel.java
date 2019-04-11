package edu.kit.mima.gui.components;

import edu.kit.mima.gui.layout.WrapLayout;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Map;

public class WrapPanel extends JScrollPane {

    private final Box box;
    private int width = 0;
    private Map<Integer, CPanel> panelList;

    /**
     * Create new WrapPanel.
     */
    public WrapPanel() {
        setAlignmentX(LEFT_ALIGNMENT);
        setAlignmentY(TOP_ALIGNMENT);

        box = Box.createVerticalBox();
        box.setAlignmentX(LEFT_ALIGNMENT);
        box.setAlignmentY(TOP_ALIGNMENT);
        panelList = new HashMap<>();
        setViewportView(box);

        //Important!!! Need to invalidate the Scroll pane, otherwise it
        //doesn't try to lay out when the container is shrunk
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent ce) {
                width = getWidth();
                box.invalidate();
            }
        });
    }

    @Override
    public Insets getInsets() {
        return new Insets(0, 0, 0, 0);
    }


    /**
     * Add a component.
     *
     * @param c     the component.
     * @param index of target panel.
     */
    public void addComponent(final Component c, int index) {
        ensurePanel(index);
        panelList.get(index).add(c);
    }

    /**
     * Set whether the panel at index only holds a single component.
     *
     * @param single whether the component has only one child.
     * @param index  index
     */
    public void setPanelScale(final boolean single, int index) {
        ensurePanel(index);
        panelList.get(index).setSingle(single);
    }

    public JPanel getPanel(int index) {
        return panelList.get(index);
    }

    private void ensurePanel(int index) {
        if (!panelList.containsKey(index)) {
            var p = new CPanel();
            panelList.put(index, p);
            box.add(p, index);
        }
    }

    private class CPanel extends JPanel {

        private boolean single;

        private CPanel() {
            setupWrapLayout();
            setAlignmentY(TOP_ALIGNMENT);
            setAlignmentX(LEFT_ALIGNMENT);
        }

        private void setupWrapLayout() {
            var layout = new WrapLayout(WrapLayout.LEFT);
            layout.setAlignOnBaseline(true);
            setLayout(layout);
            setBorder(null);
        }

        public Dimension getPreferredSize() {
            Dimension result = super.getPreferredSize();
            result.width = width - WrapPanel.this.getVerticalScrollBar().getWidth();
            return result;
        }

        private void setSingle(boolean single) {
            if (this.single == single) {
                return;
            }
            this.single = single;
            var comp = getComponents();
            if (single) {
                var layout = (WrapLayout) getLayout();
                int w = layout.getHgap();
                int h = layout.getVgap();
                setBorder(new EmptyBorder(h, w, h, w));
                setLayout(new BorderLayout());
            } else {
                setupWrapLayout();
            }
            for (var c : comp) {
                add(c);
            }
            revalidate();
        }

        @Override
        public Component.BaselineResizeBehavior getBaselineResizeBehavior() {
            return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
        }

        @Override
        public int getBaseline(int width, int height) {
            return 0;
        }
    }
}