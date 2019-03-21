package edu.kit.mima.gui.components.fontchooser.panes;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFormattedTextField;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionListener;

public class SizePane extends JPanel {

    private final JList<Integer> sizeList = new JList<>();
    private final JSpinner sizeSpinner = new JSpinner();
    private final DefaultListModel<Integer> sizeListModel = new DefaultListModel<>();

    /**
     * Create Size Pane.
     */
    public SizePane() {
        setLayout(new GridBagLayout());

        initSizeListModel();
        initSizeList();
        initSizeSpinner();
        addSizeSpinner();
        addSizeScrollPane();
    }

    private void addSizeScrollPane() {
        final JScrollPane sizeScrollPane = new JScrollPane();
        sizeScrollPane.setMinimumSize(new Dimension(50, 50));
        sizeScrollPane.setPreferredSize(new Dimension(60, 100));
        sizeScrollPane.setViewportView(sizeList);
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weighty = 1.0;
        add(sizeScrollPane, gridBagConstraints);
    }

    private void addSizeSpinner() {
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 6, 0);
        add(sizeSpinner, gridBagConstraints);
    }

    private void initSizeSpinner() {
        final int spinnerHeight = (int) sizeSpinner.getPreferredSize().getHeight();
        sizeSpinner.setPreferredSize(new Dimension(60, spinnerHeight));
        sizeSpinner.setModel(new SpinnerNumberModel(12, 6, 128, 1));
        final JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) sizeSpinner.getEditor();
        final JFormattedTextField textField = editor.getTextField();
        textField.setBorder(new JScrollPane().getBorder());
        sizeSpinner.addChangeListener(event -> {

            final Integer value = (Integer) sizeSpinner.getValue();
            final int index = ((DefaultListModel<Integer>) sizeList.getModel()).indexOf(value);
            if (index > -1) {
                sizeList.setSelectedValue(value, true);
            } else {
                sizeList.clearSelection();
            }

        });
    }

    private void initSizeList() {
        sizeList.setModel(sizeListModel);
        sizeList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sizeList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                final int index = ((DefaultListModel<Integer>) sizeList.getModel())
                        .indexOf(sizeList.getSelectedValue());
                if (index > -1) {
                    sizeSpinner.setValue(sizeList.getSelectedValue());
                }
            }
        });
        final DefaultListCellRenderer renderer = (DefaultListCellRenderer) sizeList
                .getCellRenderer();
        renderer.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    private void initSizeListModel() {
        int size = 6;
        int step = 1;
        int ceil = 14;
        do {
            sizeListModel.addElement(size);
            if (size == ceil) {
                ceil += ceil;
                step += step;
            }
            size += step;
        } while (size <= 128);
    }

    public void addListSelectionListener(final ListSelectionListener listener) {
        sizeList.addListSelectionListener(listener);
    }

    public void removeListSelectionListener(final ListSelectionListener listener) {
        sizeList.removeListSelectionListener(listener);
    }

    /**
     * Get the selected size.
     *
     * @return the selected size.
     */
    public int getSelectedSize() {
        if (!sizeList.isSelectionEmpty()) {
            return sizeList.getSelectedValue();
        }
        return (Integer) sizeSpinner.getValue();
    }

    /**
     * Set the selected size.
     *
     * @param size the size to select.
     */
    public void setSelectedSize(final int size) {
        if (sizeListModel.contains(size)) {
            sizeList.setSelectedValue(size, true);
        }
        sizeSpinner.setValue(size);
    }

}
