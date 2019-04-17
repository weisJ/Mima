package edu.kit.mima.gui.components.tabframe;

import edu.kit.mima.gui.components.IconLabel;
import edu.kit.mima.gui.components.alignment.Alignment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Tab Component for {@link TabFrame}.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class TabFrameTabComponent extends IconLabel {

    private final Color defaultFontColor;
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
        super(icon, title, CENTER, 20);
        this.alignment = alignment;
        this.title = title;
        this.index = index;
        label.setFont(label.getFont().deriveFont(11.0f));
        defaultFontColor = label.getForeground();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setSelected(!selected);
                parent.notifySelectChange(TabFrameTabComponent.this);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
            }
        });
        setOrientation(Alignment.WEST);
    }

    public int getIndex() {
        return index;
    }

    /**
     * Get the alignment.
     *
     * @return the alignment.
     */
    public Alignment getAlignment() {
        return alignment;
    }

    @Override
    public void paint(@NotNull Graphics g) {
        getLayout().layoutContainer(this);
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
        super.paint(g);
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
        selectedColor = UIManager.getColor("TabFrameTab.selected");
        hoverColor = UIManager.getColor("TabFrameTab.hover");
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
    public void setSelected(boolean selected) {
        this.selected = selected;
        label.setForeground(selected ? Color.WHITE : defaultFontColor);
    }

    /**
     * Set the title of the component.
     *
     * @param title the title
     */
    public void setTitle(@Nullable String title) {
        this.title = title == null ? "" : title;
        updateLabel();
    }

    private void updateLabel() {
        if (accelerator >= 0 && accelerator <= 9) {
            label.setText(accelerator + ':' + title);
        }
    }
}
