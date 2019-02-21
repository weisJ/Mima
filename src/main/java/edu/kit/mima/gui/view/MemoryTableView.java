package edu.kit.mima.gui.view;

import edu.kit.mima.core.Mima;
import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.interpretation.Environment;
import edu.kit.mima.core.running.MimaRunner;
import edu.kit.mima.gui.table.FixedScrollTable;

import javax.swing.JTable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class MemoryTableView implements MemoryView {

    private static final String STACK_POINTER_POSTFIX = " [SP]";
    private static final String ACCUMULATOR = "accumulator";

    private final MimaRunner mimaRunner;

    private final FixedScrollTable table;
    private boolean binaryView = false;

    public MemoryTableView(MimaRunner mimaRunner, FixedScrollTable table) {
        this.mimaRunner = mimaRunner;
        this.table = table;
    }

    private static String getAssociation(final Map<String, Integer> associations, final int value) {
        return associations.entrySet().stream().filter(entry -> entry.getValue() == value).findFirst()
                .map(Map.Entry::getKey).orElse(null);
    }

    @Override
    public void updateView() {
        table.setContent(getMemoryTable());
        table.repaint();
    }

    /**
     * Set whether to use the binary representation of the memory values.
     *
     * @param binaryView true if binary should be used.
     */
    public void setBinaryView(boolean binaryView) {
        if (this.binaryView != binaryView) {
            this.binaryView = binaryView;
            updateView();
        }
    }

    /**
     * Get the current mima memory table.
     *
     * @return memory table
     */
    public Object[][] getMemoryTable() {
        Environment scope = mimaRunner.getCurrentEnvironment();
        Map<String, Integer> map = new HashMap<>();
        while (scope != null) {
            map.putAll(scope.getDefinitions().get(0).entrySet().stream()
                    .filter(e -> !map.containsKey(e.getKey().getValue().toString()))
                    .collect(Collectors.toMap(e -> e.getKey().getValue().toString(), e -> e.getValue().intValue())));
            scope = scope.returnToParent();
        }
        return createMemoryTable(map);
    }

    /**
     * Get the memory table labeled with the corresponding memory associations
     *
     * @param associations memory associations to use
     * @return MemoryTable formatted for an n(rows) x 2(columns) {@link JTable}
     */
    private Object[][] createMemoryTable(Map<String, Integer> associations) {
        Mima mima = mimaRunner.getMima();
        var memoryM = mima.getMemory();
        var accumulator = mima.getAccumulator();
        var stackPointer = mima.getStackPointer();

        final Map<Integer, MachineWord> values = memoryM.getMapping();
        final List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{ACCUMULATOR, binaryView ? accumulator.binaryRepresentation() : accumulator.intValue()});

        final List<Object[]> memory = new ArrayList<>();
        for (Map.Entry<Integer, MachineWord> entry : values.entrySet()) {
            String valueString = binaryView
                    ? entry.getValue().binaryRepresentation()
                    : String.valueOf(entry.getValue().intValue());
            Object[] element = associations.containsValue(entry.getKey())
                    ? new Object[]{entry.getKey()
                    + " (" + getAssociation(associations, entry.getKey())
                    + ')', valueString}
                    : new Object[]{entry.getKey(), valueString};
            boolean skip;
            try {
                int value = Integer.parseInt(element[0].toString());
                skip = value < 0;
            } catch (NumberFormatException e) {
                data.add(element);
                skip = true;
            }
            if (entry.getKey() == stackPointer.intValue()) {
                element[0] += STACK_POINTER_POSTFIX;
            }
            if (!skip) {
                memory.add(element);
            }
        }
        data.addAll(memory);
        return data.toArray(new Object[0][]);
    }
}
