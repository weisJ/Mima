package edu.kit.mima.gui.components.fontchooser.panes;

import edu.kit.mima.gui.components.fontchooser.FontFamilies;
import edu.kit.mima.gui.components.fontchooser.FontFamily;
import edu.kit.mima.gui.components.fontchooser.model.FontSelectionModel;
import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.awt.Font;
import java.util.Objects;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;


public class StylePane extends JScrollPane implements ChangeListener {

    private final JList<String> styleList = new JList<>();

    @NotNull private final DefaultListModel<String> styleListModel;

    private String family;

    /**
     * Create a StylePane.
     */
    public StylePane() {

        styleListModel = new DefaultListModel<>();
        styleList.setModel(styleListModel);
        styleList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleList.setCellRenderer(new ToolTipCellRenderer());

        setMinimumSize(new Dimension(140, 50));
        setPreferredSize(new Dimension(160, 100));
        setViewportView(styleList);

    }

    public void addListSelectionListener(final ListSelectionListener listener) {
        styleList.addListSelectionListener(listener);
    }

    public void removeListSelectionListener(final ListSelectionListener listener) {
        styleList.removeListSelectionListener(listener);
    }

    public String getSelectedStyle() {
        return styleList.getSelectedValue();
    }

    public void setSelectedStyle(final String name) {
        styleList.setSelectedValue(name, true);
    }

    @Override
    public void stateChanged(@NotNull final ChangeEvent e) {

        final FontSelectionModel fontSelectionModel = (FontSelectionModel) e.getSource();
        final Font selectedFont = fontSelectionModel.getSelectedFont();
        final String family = selectedFont.getFamily();

        loadFamily(family);

    }

    /**
     * Load a FontFamily.
     *
     * @param family name of font family.
     */
    public void loadFamily(final String family) {
        if (Objects.equals(this.family, family)) {
            return;
        }

        this.family = family;

        final FontFamilies fontFamilies = FontFamilies.getInstance();
        final FontFamily fontFamily = fontFamilies.get(family);

        if (fontFamily != null) {
            final ListSelectionListener[] selectionListeners =
                    styleList.getListSelectionListeners();
            removeSelectionListeners(selectionListeners);
            updateListModel(fontFamily);
            addSelectionListeners(selectionListeners);
        }

    }

    private void updateListModel(@NotNull final Iterable<Font> fonts) {
        styleListModel.clear();

        for (final Font font : fonts) {
            styleListModel.addElement(font.getName());
        }
    }

    private void addSelectionListeners(
            @NotNull final ListSelectionListener[] selectionListeners) {
        for (final ListSelectionListener listener : selectionListeners) {
            styleList.addListSelectionListener(listener);
        }
    }

    private void removeSelectionListeners(
            @NotNull final ListSelectionListener[] selectionListeners) {
        for (final ListSelectionListener listener : selectionListeners) {
            styleList.removeListSelectionListener(listener);
        }
    }
}
