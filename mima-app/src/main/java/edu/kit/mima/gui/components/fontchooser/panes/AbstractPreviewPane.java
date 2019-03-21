package edu.kit.mima.gui.components.fontchooser.panes;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JPanel;

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
