package edu.kit.mima.gui.components.fontchooser.panes;

import edu.kit.mima.gui.components.SearchField;
import edu.kit.mima.gui.components.fontchooser.FontFamilies;
import edu.kit.mima.gui.components.fontchooser.FontFamily;
import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

/**
 * Family Display Pane.
 */
public class FamilyPane extends JPanel {

    private final JList<String> familyList = new JList<>();

    @NotNull private final SearchListener searchListener;

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
        setPreferredSize(new Dimension(240, 100));

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

        final JTextField searchField = new SearchField();
        searchField.setBorder(BorderFactory.createEmptyBorder());
        searchField.requestFocus();
        searchField.addKeyListener(searchListener);
        add(new JScrollPane(searchField), gridBagConstraints);
    }

    private void addScrollPane() {
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;

        final JScrollPane scrollPane = new JScrollPane(familyList);
        add(scrollPane, gridBagConstraints);
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
