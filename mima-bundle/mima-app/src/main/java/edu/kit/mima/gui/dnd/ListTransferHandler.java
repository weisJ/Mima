package edu.kit.mima.gui.dnd;

import javax.swing.*;
import java.util.List;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class ListTransferHandler extends StringTransferHandler {
    private int[] indices = null;
    private int addIndex = -1; //Location where items were added
    private int addCount = 0;  //Number of items added.

    //Bundle up the selected items in the list
    //as a single string, for export.
    protected String exportString(final JComponent c) {
        JList<?> list = (JList<?>) c;
        indices = list.getSelectedIndices();
        List<?> values = list.getSelectedValuesList();

        StringBuilder buff = new StringBuilder();

        for (int i = 0; i < values.size(); i++) {
            Object val = values.get(i);
            buff.append(val == null ? "" : val.toString());
            if (i != values.size() - 1) {
                buff.append("\n");
            }
        }

        return buff.toString();
    }

    //Take the incoming string and wherever there is a
    //newline, break it into a separate item in the list.
    protected void importString(final JComponent c, final String str) {
        JList<?> target = (JList<?>) c;
        DefaultListModel<String> listModel = (DefaultListModel<String>) target.getModel();
        int index = target.getSelectedIndex();

        //Prevent the user from dropping data back on itself.
        //For example, if the user is moving items #4,#5,#6 and #7 and
        //attempts to insert the items after item #5, this would
        //be problematic when removing the original items.
        //So this is not allowed.
        if (indices != null && index >= indices[0] - 1
            && index <= indices[indices.length - 1]) {
            indices = null;
            return;
        }

        int max = listModel.getSize();
        if (index < 0) {
            index = max;
        } else {
            index++;
            if (index > max) {
                index = max;
            }
        }
        addIndex = index;
        String[] values = str.split("\n");
        addCount = values.length;
        for (String value : values) {
            listModel.add(index++, value);
        }
    }

    //If the remove argument is true, the drop has been
    //successful and it's time to remove the selected items
    //from the list. If the remove argument is false, it
    //was a Copy operation and the original list is left
    //intact.
    protected void cleanup(final JComponent c, final boolean remove) {
        if (remove && indices != null) {
            JList<?> source = (JList<?>) c;
            DefaultListModel<?> model = (DefaultListModel<?>) source.getModel();
            //If we are moving items around in the same list, we
            //need to adjust the indices accordingly, since those
            //after the insertion point have moved.
            if (addCount > 0) {
                for (int i = 0; i < indices.length; i++) {
                    if (indices[i] > addIndex) {
                        indices[i] += addCount;
                    }
                }
            }
            for (int i = indices.length - 1; i >= 0; i--) {
                model.remove(indices[i]);
            }
        }
        indices = null;
        addCount = 0;
        addIndex = -1;
    }
}