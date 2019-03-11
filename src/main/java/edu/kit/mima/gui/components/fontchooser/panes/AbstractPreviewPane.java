package edu.kit.mima.gui.components.fontchooser.panes;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Font;

/**
 * @author Jannis Weis
 * @since 2018
 */
public abstract class AbstractPreviewPane extends JPanel {

    public abstract void setPreviewFont(Font previewFont);

    public abstract void setDimension(Dimension dimension);
}
