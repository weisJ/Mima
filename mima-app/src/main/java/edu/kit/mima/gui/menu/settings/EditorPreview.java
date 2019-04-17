package edu.kit.mima.gui.menu.settings;

import edu.kit.mima.gui.components.editor.Editor;
import edu.kit.mima.gui.components.fontchooser.panes.AbstractPreviewPane;
import org.jetbrains.annotations.NotNull;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Editor Preview Pane.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class EditorPreview extends AbstractPreviewPane {

    @NotNull
    private final Editor editor;

    /**
     * Create new Editor Preview.
     */
    public EditorPreview() {
        setLayout(new BorderLayout());
        editor = new Editor();
        editor.setEnabled(false);
        editor.setText("Test Editor");
        add(editor);
        setPreferredSize(new Dimension(150, 150));
    }

    @Override
    public void setPreviewFont(@NotNull final Font previewFont) {
        editor.setEditorFont(previewFont);
    }

    @Override
    public void setDimension(final Dimension dimension) {
        setPreferredSize(dimension);
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        editor.setPreferredSize(preferredSize);
        super.setPreferredSize(preferredSize);
    }
}
