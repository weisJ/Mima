package edu.kit.mima.gui.components.fontchooser.panes;

import javax.swing.*;
import java.awt.*;

/**
 * Preview Pane for {@link edu.kit.mima.gui.components.fontchooser.FontChooser}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public abstract class AbstractPreviewPane extends JPanel {

    public abstract void setPreviewFont(Font previewFont);

    public abstract void setDimension(Dimension dimension);
}
