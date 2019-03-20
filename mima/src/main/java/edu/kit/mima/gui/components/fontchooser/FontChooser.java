/*
 * A font chooser JavaBean component.
 * Copyright (C) 2009 Dr Christos Bohoris
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3 as published by the Free Software Foundation;
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * swing@connectina.com
 */

package edu.kit.mima.gui.components.fontchooser;

import edu.kit.mima.gui.components.fontchooser.listeners.FamilyListSelectionListener;
import edu.kit.mima.gui.components.fontchooser.listeners.SizeListSelectionListener;
import edu.kit.mima.gui.components.fontchooser.listeners.StyleListSelectionListener;
import edu.kit.mima.gui.components.fontchooser.model.DefaultFontSelectionModel;
import edu.kit.mima.gui.components.fontchooser.model.FontSelectionModel;
import edu.kit.mima.gui.components.fontchooser.panes.AbstractPreviewPane;
import edu.kit.mima.gui.components.fontchooser.panes.FamilyPane;
import edu.kit.mima.gui.components.fontchooser.panes.PreviewPane;
import edu.kit.mima.gui.components.fontchooser.panes.SizePane;
import edu.kit.mima.gui.components.fontchooser.panes.StylePane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;


/**
 * Provides a pane of controls designed to allow a user to select a {@code Font}.
 *
 * @author Christos Bohoris
 * @see Font
 */
public class FontChooser extends JPanel implements FontContainer {

    private static final int DEFAULT_FONT_SIZE = 12;
    private static final int DEFAULT_SPACE = 11;

    private static final String SELECTION_MODEL_PROPERTY = "selectionModel";
    private final ResourceBundle resourceBundle;
    private final JLabel familyLabel = new JLabel();
    private final JLabel styleLabel = new JLabel();
    private final JLabel sizeLabel = new JLabel();
    private final JLabel previewLabel = new JLabel();
    private final JPanel fontPanel = new JPanel();
    private final JPanel previewPanel = new JPanel();
    private final FamilyPane familyPane = new FamilyPane();
    @NotNull private final AbstractPreviewPane previewPane;
    private final StylePane stylePane = new StylePane();
    private final SizePane sizePane = new SizePane();
    private final FamilyListSelectionListener familyPaneListener =
            new FamilyListSelectionListener(this);
    private final StyleListSelectionListener stylePaneListener =
            new StyleListSelectionListener(this);
    private final SizeListSelectionListener sizePaneListener =
            new SizeListSelectionListener(this);
    @NotNull private FontSelectionModel selectionModel;

    /**
     * Creates a FontChooser pane with an initial default Font (Sans Serif, Plain, 12).
     */
    public FontChooser() {
        this(new Font(Font.SANS_SERIF, Font.PLAIN, DEFAULT_FONT_SIZE));
    }

    /**
     * Creates a FontChooser pane with the specified initial Font.
     *
     * @param initialFont the initial Font set in the chooser
     */
    public FontChooser(@NotNull final Font initialFont) {
        this(initialFont, new PreviewPane());
    }

    public FontChooser(@NotNull final Font initialFont,
                       @NotNull final AbstractPreviewPane previewPane) {
        this(new DefaultFontSelectionModel(initialFont), previewPane);
    }

    /**
     * Creates a FontChooser pane with the specified {@code FontSelectionModel}.
     *
     * @param model       the {@code FontSelectionModel} to be used
     * @param previewPane the preview pane
     */
    public FontChooser(@NotNull final FontSelectionModel model,
                       @NotNull final AbstractPreviewPane previewPane) {
        this.previewPane = previewPane;
        resourceBundle = ResourceBundle.getBundle("FontChooser");
        selectionModel = model;
        setSelectionModel(model);
        setLayout(new BorderLayout());
        addComponents();
        initPanes();

        previewPane.setPreviewFont(selectionModel.getSelectedFont());
    }

    /**
     * Gets the current Font value from the FontChooser. By default, this delegates to the model.
     *
     * @return the current Font value of the FontChooser
     */
    @Nullable
    @Override
    public Font getSelectedFont() {
        return selectionModel.getSelectedFont();
    }

    /**
     * Sets the current font of the FontChooser to the specified font. The {@code
     * FontSelectionModel} will fire a {@code ChangeEvent}
     *
     * @param font the font to be set in the font chooser
     * @see JComponent#addPropertyChangeListener
     */
    @Override
    public void setSelectedFont(@NotNull final Font font) {
        familyPane.removeListSelectionListener(familyPaneListener);
        stylePane.removeListSelectionListener(stylePaneListener);
        sizePane.removeListSelectionListener(sizePaneListener);

        selectionModel.setSelectedFont(font);

        initPanes();
    }

    /**
     * Returns the data model that handles Font selections.
     *
     * @return a {@code FontSelectionModel} object
     */
    @NotNull
    public FontSelectionModel getSelectionModel() {
        return selectionModel;
    }

    /**
     * Sets the model containing the selected Font.
     *
     * @param newModel the new {@code FontSelectionModel} object
     */
    public void setSelectionModel(@Nullable final FontSelectionModel newModel) {
        if (newModel == null) {
            throw new IllegalArgumentException("New model must not be null");
        }
        final FontSelectionModel oldModel = selectionModel;
        selectionModel = newModel;
        selectionModel.addChangeListener(stylePane);
        firePropertyChange(SELECTION_MODEL_PROPERTY, oldModel, newModel);
    }

    /**
     * Adds a {@code ChangeListener} to the model.
     *
     * @param listener the {@code ChangeListener} to be added
     */
    public void addChangeListener(final ChangeListener listener) {
        selectionModel.addChangeListener(listener);
    }

    /**
     * Removes a {@code ChangeListener} from the model.
     *
     * @param listener the {@code ChangeListener} to be removed
     */
    public void removeChangeListener(final ChangeListener listener) {
        selectionModel.removeChangeListener(listener);
    }

    private void initPanes() {
        familyPane.setSelectedFamily(selectionModel.getSelectedFontFamily());
        familyPane.addListSelectionListener(familyPaneListener);

        stylePane.loadFamily(selectionModel.getSelectedFontFamily());
        stylePane.setSelectedStyle(selectionModel.getSelectedFontName());
        stylePane.addListSelectionListener(stylePaneListener);

        sizePane.addListSelectionListener(sizePaneListener);
        sizePane.setSelectedSize(selectionModel.getSelectedFontSize());
    }

    private void addComponents() {
        addFontPanel();
        addFamilyLabel();
        addStyleLabel();
        addSizeLabel();
        addFamilyPane();
        addStylePane();
        addSizePane();
        addPreview();
        addPreviewLabel();
    }

    private void addPreview() {
        previewPanel.setLayout(new GridBagLayout());
        add(previewPanel, BorderLayout.PAGE_END);
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        previewPane.setDimension(fontPanel.getPreferredSize());
        previewPanel.add(previewPane, gridBagConstraints);
    }

    private void addPreviewLabel() {
        previewLabel.setText(resourceBundle.getString("font.preview"));
        previewLabel.setLabelFor(sizePane);
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        previewPanel.add(previewLabel, gridBagConstraints);
    }

    private void addSizePane() {
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, DEFAULT_SPACE, 0);
        fontPanel.add(sizePane, gridBagConstraints);
    }

    private void addStylePane() {
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, DEFAULT_SPACE, DEFAULT_SPACE);
        fontPanel.add(stylePane, gridBagConstraints);
    }

    private void addFamilyPane() {
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, DEFAULT_SPACE, DEFAULT_SPACE);
        fontPanel.add(familyPane, gridBagConstraints);
    }

    private void addSizeLabel() {
        sizeLabel.setLabelFor(sizePane);
        sizeLabel.setText(resourceBundle.getString("font.size"));
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        fontPanel.add(sizeLabel, gridBagConstraints);
    }

    private void addStyleLabel() {
        styleLabel.setLabelFor(stylePane);
        styleLabel.setText(resourceBundle.getString("font.style"));
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 5, DEFAULT_SPACE);
        fontPanel.add(styleLabel, gridBagConstraints);
    }

    private void addFamilyLabel() {
        familyLabel.setLabelFor(familyPane);
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 5, DEFAULT_SPACE);
        fontPanel.add(familyLabel, gridBagConstraints);
        familyLabel.setText(resourceBundle.getString("font.family"));
    }

    private void addFontPanel() {
        fontPanel.setLayout(new GridBagLayout());
        add(fontPanel);
    }

    @Override
    public String getSelectedStyle() {
        return stylePane.getSelectedStyle();
    }

    @Override
    public float getSelectedSize() {
        return sizePane.getSelectedSize();
    }

    @Override
    public String getSelectedFamily() {
        return familyPane.getSelectedFamily();
    }

    @Override
    public void setPreviewFont(final Font font) {
        previewPane.setPreviewFont(font);
    }
}
