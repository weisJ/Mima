package demo;

import org.jdesktop.jxlayer.JXLayer;
import org.jetbrains.annotations.NotNull;
import org.pbjar.jxlayer.plaf.ext.TransformUI;
import org.pbjar.jxlayer.plaf.ext.transform.DefaultTransformModel;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A table with vertical table headers, rotated by {@link TransformUI}.
 */
public class RotatedTableHeaderRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;
    @NotNull
    private final JPanel actualRenderer;
    @NotNull
    private final JLabel iconLabel;
    @NotNull
    private final JXLayer<JComponent> layer;
    private final Icon dummyIcon = new Icon() {

        @Override
        public void paintIcon(@NotNull Component c, @NotNull Graphics g, int x, int y) {
            g.setColor(c.getBackground());
            g.fillRect(x, y, 16, 16);

        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    };

    public RotatedTableHeaderRenderer() {
        super();
        this.setHorizontalAlignment(JLabel.LEADING);
        DefaultTransformModel model = new DefaultTransformModel();
        model.setQuadrantRotation(1);
        layer = TransformUtils.createTransformJXLayer(this, model);

        actualRenderer = new JPanel(new BorderLayout());
        iconLabel = new JLabel(dummyIcon, JLabel.CENTER);
        actualRenderer.add(layer, BorderLayout.PAGE_START);
        actualRenderer.add(iconLabel, BorderLayout.PAGE_END);
        actualRenderer.setOpaque(true);
        actualRenderer.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    }

    /**
     * Run a demo.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        TransformUI.prepareForJTextComponent();
        SwingUtilities.invokeLater(() -> {

            List<Integer> rowList = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
            Integer[][] data = new Integer[40][];
            for (int index = 0; index < data.length; index++) {
                Collections.shuffle(rowList);
                data[index] = rowList.toArray(Integer[]::new);
            }
            String[] header = {" Column One  ", " Column Two ", " Column Three ", " Column Four ",
                               " Column Five ", " Column Six ", " Column Seven ", " Column Eight ",
                               " Column Nine ", " Column Ten "};
            TableModel model = new DefaultTableModel(data, header);
            JTable table = new JTable(model);
            table.setAutoCreateRowSorter(true);
            TableCellRenderer renderer = new RotatedTableHeaderRenderer();
            TableColumnModel columnModel = table.getColumnModel();
            for (int index = 0; index < columnModel.getColumnCount(); index++) {
                columnModel.getColumn(index).setHeaderRenderer(renderer);
            }
            JFrame frame = new JFrame("TabFrameDemo");
            frame.add(new JScrollPane(table));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    @NotNull
    @Override
    public Component getTableCellRendererComponent(@NotNull JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row,
                                                   int column) {
        JTableHeader tableHeader = table.getTableHeader();
        JComponent preparedRenderer = (JComponent) tableHeader.getDefaultRenderer()
                                                              .getTableCellRendererComponent(table,
                                                                                             value,
                                                                                             isSelected,
                                                                                             hasFocus,
                                                                                             row,
                                                                                             column);
        layer.setView(preparedRenderer);
        actualRenderer.setBackground(preparedRenderer.getBackground());
        if (preparedRenderer instanceof JLabel) {
            Icon actualIcon = ((JLabel) preparedRenderer).getIcon();
            if (actualIcon == null) {
                iconLabel.setIcon(dummyIcon);
            } else {
                iconLabel.setIcon(actualIcon);
                ((JLabel) preparedRenderer).setIcon(null);
            }
        }
        preparedRenderer.setBorder(null);
        return actualRenderer;
    }
}
