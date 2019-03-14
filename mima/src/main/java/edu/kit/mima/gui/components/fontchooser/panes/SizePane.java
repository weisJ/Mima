package edu.kit.mima.gui.components.fontchooser.panes;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class SizePane extends JPanel {

    private final JList<Integer> sizeList = new JList<>();
    private final JSpinner sizeSpinner = new JSpinner();
    private final DefaultListModel<Integer> sizeListModel = new DefaultListModel<>();

    public SizePane() {
        setLayout(new GridBagLayout());

        initSizeListModel();
        initSizeList();
        initSizeSpinner();
        addSizeSpinner();
        addSizeScrollPane();
    }

    private void addSizeScrollPane() {
        JScrollPane sizeScrollPane = new JScrollPane();
        sizeScrollPane.setMinimumSize(new Dimension(50, 50));
        sizeScrollPane.setPreferredSize(new Dimension(60, 100));
        sizeScrollPane.setViewportView(sizeList);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.weighty = 1.0;
        add(sizeScrollPane, gridBagConstraints);
    }

    private void addSizeSpinner() {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new Insets(0, 0, 6, 0);
        add(sizeSpinner, gridBagConstraints);
    }

    private void initSizeSpinner() {
        int spinnerHeight = (int) sizeSpinner.getPreferredSize().getHeight();
        sizeSpinner.setPreferredSize(new Dimension(60, spinnerHeight));
        sizeSpinner.setModel(new SpinnerNumberModel(12, 6, 128, 1));
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) sizeSpinner.getEditor();
        JFormattedTextField textField = editor.getTextField();
        textField.setBorder(new JScrollPane().getBorder());
        sizeSpinner.addChangeListener(event -> {

            Integer value = (Integer) sizeSpinner.getValue();
            int index = ((DefaultListModel<Integer>) sizeList.getModel()).indexOf(value);
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
                int index = ((DefaultListModel<Integer>) sizeList.getModel()).indexOf(sizeList.getSelectedValue());
                if (index > -1) {
                    sizeSpinner.setValue(sizeList.getSelectedValue());
                }
            }
        });
        DefaultListCellRenderer renderer = (DefaultListCellRenderer) sizeList.getCellRenderer();
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

    public void addListSelectionListener(ListSelectionListener listener) {
        sizeList.addListSelectionListener(listener);
    }

    public void removeListSelectionListener(ListSelectionListener listener) {
        sizeList.removeListSelectionListener(listener);
    }

    public int getSelectedSize() {
        if (!sizeList.isSelectionEmpty()) {
            return sizeList.getSelectedValue();
        }
        return (Integer) sizeSpinner.getValue();
    }

    public void setSelectedSize(int size) {
        if (sizeListModel.contains(size)) {
            sizeList.setSelectedValue(size, true);
        }
        sizeSpinner.setValue(size);
    }

}
