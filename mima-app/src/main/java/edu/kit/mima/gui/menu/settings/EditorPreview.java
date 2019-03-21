package edu.kit.mima.gui.menu.settings;

import edu.kit.mima.gui.components.editor.Editor;
import edu.kit.mima.gui.components.fontchooser.panes.AbstractPreviewPane;
import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.awt.Font;

/**
 * Editor Preview Pane.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class EditorPreview extends AbstractPreviewPane {

    private final Editor editor;

    /**
     * Create new Editor Preview.
     */
    public EditorPreview() {
        editor = new Editor();
        editor.setEnabled(false);
        editor.setText("Test Editor");
        add(editor);
    }

    @Override
    public void setPreviewFont(@NotNull final Font previewFont) {
        editor.setEditorFont(previewFont);
    }

    @Override
    public void setDimension(final Dimension dimension) {
        editor.setPreferredSize(dimension);
        setPreferredSize(dimension);
    }
}
