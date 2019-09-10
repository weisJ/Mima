package edu.kit.mima.gui.components.fontchooser.panes;

import edu.kit.mima.gui.components.fontchooser.FontFamilies;
import edu.kit.mima.gui.components.fontchooser.FontFamily;
import edu.kit.mima.gui.components.text.SearchTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

/**
 * Family Display Pane.
 */
public class FamilyPane extends JPanel {

    private final JList<String> familyList = new JList<>();

    @NotNull
    private final SearchListener searchListener;

    /**
     * Create Font Family Chooser Pane.
     */
    public FamilyPane() {

        final DefaultListModel<String> familyListModel = new DefaultListModel<>();
        final FontFamilies fontFamilies = FontFamilies.getInstance();
        searchListener = new SearchListener(this);
        for (final FontFamily fontFamily : fontFamilies) {
            final String name = fontFamily.getName();
            familyListModel.addElement(name);
            searchListener.addFamilyName(name);
        }
        initializeList(familyListModel);

        setMinimumSize(new Dimension(80, 50));
        setPreferredSize(new Dimension(240, 180));

        setLayout(new GridBagLayout());
        addSearchField();
        addScrollPane();
    }

    private void initializeList(@NotNull final ListModel<String> familyListModel) {
        familyList.setModel(familyListModel);
        familyList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        familyList.setCellRenderer(new ToolTipCellRenderer());
    }

    private void addSearchField() {
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 0, 5, 0);
        gridBagConstraints.weightx = 1.0;

        final var searchField = new SearchTextField();
        searchField.setBorder(BorderFactory.createEmptyBorder());
        searchField.requestFocus();
        searchField.addKeyboardListener(searchListener);
        add(searchField, gridBagConstraints);
    }

    private void addScrollPane() {
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;

        add(new JScrollPane(familyList), gridBagConstraints);
    }

    public void addListSelectionListener(final ListSelectionListener listener) {
        familyList.addListSelectionListener(listener);
    }

    public void removeListSelectionListener(final ListSelectionListener listener) {
        familyList.removeListSelectionListener(listener);
    }

    public String getSelectedFamily() {
        return familyList.getSelectedValue();
    }

    public void setSelectedFamily(final String family) {
        familyList.setSelectedValue(family, true);
    }
}
