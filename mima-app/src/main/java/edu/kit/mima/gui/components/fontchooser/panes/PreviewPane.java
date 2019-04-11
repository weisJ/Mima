package edu.kit.mima.gui.components.fontchooser.panes;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ResourceBundle;

public class PreviewPane extends AbstractPreviewPane {

    private final JTextArea previewText = new JTextArea();
    private final JScrollPane scrollPane = new JScrollPane();

    /**
     * Crate a Preview Pane.
     */
    public PreviewPane() {
        final ResourceBundle resourceBundle = ResourceBundle.getBundle("FontChooser");
        previewText.setText(resourceBundle.getString("font.preview.text"));
        previewText.setBorder(BorderFactory.createCompoundBorder(
                previewText.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5))
        );
        scrollPane.setViewportView(previewText);
        add(scrollPane);
    }

    public void setPreviewFont(final Font font) {
        previewText.setFont(font);
    }

    @Override
    public void setDimension(final Dimension dimension) {
        previewText.setPreferredSize(dimension);
        scrollPane.setPreferredSize(dimension);
        setPreferredSize(dimension);
    }

}
