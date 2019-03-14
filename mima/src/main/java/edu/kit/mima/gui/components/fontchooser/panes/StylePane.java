package edu.kit.mima.gui.components.fontchooser.panes;

import edu.kit.mima.gui.components.fontchooser.FontFamilies;
import edu.kit.mima.gui.components.fontchooser.FontFamily;
import edu.kit.mima.gui.components.fontchooser.model.FontSelectionModel;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Objects;


public class StylePane extends JScrollPane implements ChangeListener {

    private final JList<String> styleList = new JList<>();

    private final DefaultListModel<String> styleListModel;

    private String family;

    public StylePane() {

        styleListModel = new DefaultListModel<>();
        styleList.setModel(styleListModel);
        styleList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleList.setCellRenderer(new ToolTipCellRenderer());

        setMinimumSize(new Dimension(140, 50));
        setPreferredSize(new Dimension(160, 100));
        setViewportView(styleList);

    }

    public void addListSelectionListener(ListSelectionListener listener) {
        styleList.addListSelectionListener(listener);
    }

    public void removeListSelectionListener(ListSelectionListener listener) {
        styleList.removeListSelectionListener(listener);
    }

    public String getSelectedStyle() {
        return styleList.getSelectedValue();
    }

    public void setSelectedStyle(String name) {
        styleList.setSelectedValue(name, true);
    }

    @Override
    public void stateChanged(ChangeEvent e) {

        FontSelectionModel fontSelectionModel = (FontSelectionModel) e.getSource();
        Font selectedFont = fontSelectionModel.getSelectedFont();
        String family = selectedFont.getFamily();

        loadFamily(family);

    }

    public void loadFamily(String family) {
        if (Objects.equals(this.family, family)) {
            return;
        }

        this.family = family;

        FontFamilies fontFamilies = FontFamilies.getInstance();
        FontFamily fontFamily = fontFamilies.get(family);

        if (fontFamily != null) {
            ListSelectionListener[] selectionListeners = styleList.getListSelectionListeners();
            removeSelectionListeners(selectionListeners);
            updateListModel(fontFamily);
            addSelectionListeners(selectionListeners);
        }

    }

    private void updateListModel(Iterable<Font> fonts) {
        styleListModel.clear();

        for (Font font : fonts) {
            styleListModel.addElement(font.getName());
        }
    }

    private void addSelectionListeners(ListSelectionListener[] selectionListeners) {
        for (ListSelectionListener listener : selectionListeners) {
            styleList.addListSelectionListener(listener);
        }
    }

    private void removeSelectionListeners(ListSelectionListener[] selectionListeners) {
        for (ListSelectionListener listener : selectionListeners) {
            styleList.removeListSelectionListener(listener);
        }
    }
}
