package edu.kit.mima.gui.menu.settings;

import edu.kit.mima.gui.components.editor.Editor;
import edu.kit.mima.gui.components.fontchooser.panes.AbstractPreviewPane;

import java.awt.Dimension;
import java.awt.Font;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class EditorPreview extends AbstractPreviewPane {

    private Editor editor;

    public EditorPreview() {
        editor = new Editor();
        editor.setEnabled(false);
        editor.setText("Test Editor");
        add(editor);
    }

    @Override
    public void setPreviewFont(Font previewFont) {
        editor.setEditorFont(previewFont);
    }

    @Override
    public void setDimension(Dimension dimension) {
        editor.setPreferredSize(dimension);
        setPreferredSize(dimension);
    }
}
