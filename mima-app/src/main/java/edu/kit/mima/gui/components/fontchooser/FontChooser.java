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

import edu.kit.mima.gui.components.LabeledPane;
import edu.kit.mima.gui.components.WrapPanel;
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

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ResourceBundle;


/**
 * Provides a pane of controls designed to allow a user to select a {@code Font}.
 *
 * @author Christos Bohoris
 * @see Font
 */
public class FontChooser extends WrapPanel implements FontContainer {

    private static final int DEFAULT_FONT_SIZE = 12;

    private static final String SELECTION_MODEL_PROPERTY = "selectionModel";
    private final ResourceBundle resourceBundle;
    private final FamilyPane familyPane = new FamilyPane();
    private final StylePane stylePane = new StylePane();
    private final SizePane sizePane = new SizePane();
    private final FamilyListSelectionListener familyPaneListener =
            new FamilyListSelectionListener(this);
    private final StyleListSelectionListener stylePaneListener =
            new StyleListSelectionListener(this);
    private final SizeListSelectionListener sizePaneListener =
            new SizeListSelectionListener(this);
    @NotNull
    private final AbstractPreviewPane previewPane;
    @NotNull
    private FontSelectionModel selectionModel;

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
        addComponents();
        initPanes();
        previewPane.setPreviewFont(selectionModel.getSelectedFont());
        setBorder(null);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
    }

    @Override
    public Dimension getMinimumSize() {
        var dim = familyPane.getSize();
        dim.width += getVerticalScrollBar().getWidth();
        return dim;
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
        addComponent(new LabeledPane(familyPane,
                                     resourceBundle.getString("font.family")), 0);
        addComponent(new LabeledPane(stylePane,
                                     resourceBundle.getString("font.style")), 0);
        addComponent(new LabeledPane(sizePane,
                                     resourceBundle.getString("font.size")), 0);
        addComponent(new LabeledPane(previewPane,
                                     resourceBundle.getString("font.preview")), 1);
        setPanelScale(true, 1);
        addComponent(Box.createRigidArea(new Dimension(0, 10)), 2);
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
