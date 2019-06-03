package edu.kit.mima.gui.components.tabframe;

import edu.kit.mima.annotations.ContextManager;
import edu.kit.mima.gui.components.IconLabel;
import edu.kit.mima.gui.components.alignment.Alignment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Tab Component for {@link TabFrame}.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class TabFrameTabComponent extends IconLabel {

    @NotNull
    private final TabFrameLayout parent;
    private Color defaultFontColor;
    private Color selectedFontColor;
    private String title;
    private boolean selected;
    private boolean hover;
    private int accelerator;
    private Alignment alignment;
    private int index;
    private Color selectedColor;
    private Color hoverColor;

    /**
     * Create new TabComponent for the frame of {@link TabFrame}.
     *
     * @param title     the title.
     * @param icon      the icon.
     * @param alignment the alignment.
     * @param index     the index.
     * @param parent    the parent layout manager.
     */
    public TabFrameTabComponent(final String title, final Icon icon, final Alignment alignment,
                                final int index, @NotNull final TabFrameLayout parent) {
        super(icon, title, CENTER, 20, 2);
        this.alignment = alignment;
        this.title = title;
        this.index = index;
        this.parent = parent;
        comp.setFont(comp.getFont().deriveFont(11.0f));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    setSelected(!selected);
                    parent.notifySelectChange(TabFrameTabComponent.this);
                }
            }

            @Override
            public void mouseEntered(final MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(final MouseEvent e) {
                hover = false;
                repaint();
            }
        });
        ContextManager.createContext(this);
        setOrientation(Alignment.WEST);
    }

    public void removeFromParent() {
        parent.removeTab(alignment, TabFrameTabComponent.this.index);
    }

    public void moveTo(final Alignment a) {
        parent.moveTab(this, a);
    }

    public void setPopupVisible(final boolean visible) {
        setSelected(visible);
        parent.notifySelectChange(TabFrameTabComponent.this);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public void setAlignment(final Alignment a) {
        this.alignment = a;
    }

    /**
     * Get the alignment.
     *
     * @return the alignment.
     */
    public Alignment getAlignment() {
        return alignment;
    }

    /**
     * Get the accelerator.
     *
     * @return the accelerator.
     */
    public int getAccelerator() {
        return accelerator;
    }

    /**
     * Set the accelerator.
     *
     * @param accelerator accelerator (>0).
     */
    public void setAccelerator(final int accelerator) {
        if (accelerator < 0) {
            throw new IllegalArgumentException("accelerator must be > 0");
        }
        this.accelerator = accelerator;
        updateLabel();
    }

    /**
     * Set the orientation of the component. Orientation must be one of {@link Alignment#NORTH},
     * {@link Alignment#WEST}, {@link Alignment#EAST} or {@link Alignment#SOUTH}.
     *
     * @param orientation the orientation.
     */
    public void setOrientation(final Alignment orientation) {
        var o = switch (orientation) {
            case SOUTH, SOUTH_WEST, NORTH, NORTH_EAST -> Alignment.NORTH;
            case EAST, SOUTH_EAST -> Alignment.WEST;
            case WEST, NORTH_WEST -> Alignment.EAST;
            default -> throw new IllegalArgumentException("invalid orientation: " + orientation);
        };
        iconPanel.setAlignment(o);
    }

    @Override
    public Color getBackground() {
        return selected && selectedColor != null
                       ? selectedColor
                       : hover && hoverColor != null ? hoverColor : super.getBackground();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        defaultFontColor = UIManager.getColor("TabFrameTab.defaultForeground");
        selectedColor = UIManager.getColor("TabFrameTab.selected");
        hoverColor = UIManager.getColor("TabFrameTab.hover");
        selectedFontColor = UIManager.getColor("TabFrameTab.selectedFontColor");
    }

    /**
     * Returns whether the component is selected.
     *
     * @return true if selected.
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Set if the component is selected.
     *
     * @param selected true if selected.
     */
    public void setSelected(final boolean selected) {
        this.selected = selected;
        comp.setForeground(selected ? selectedFontColor : defaultFontColor);
        repaint();
    }

    /**
     * Set the title of the component.
     *
     * @param title the title
     */
    public void setTitle(@Nullable final String title) {
        this.title = title == null ? "" : title;
        updateLabel();
    }

    private void updateLabel() {
        if (accelerator >= 0 && accelerator <= 9) {
            comp.setText(accelerator + ':' + title);
            comp.setDisplayedMnemonicIndex(0);
        } else {
            comp.setText(title);
            comp.setDisplayedMnemonicIndex(1);
        }
    }
}
