package edu.kit.mima.gui.view;

import edu.kit.mima.core.Mima;
import edu.kit.mima.core.MimaRunner;
import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.interpretation.environment.Environment;
import edu.kit.mima.gui.components.ProtectedScrollTable;
import edu.kit.mima.gui.icons.Icons;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link MemoryView} based on Mima.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class MemoryTableView implements MemoryView {

    private static final String STACK_POINTER_POSTFIX = " [SP]";
    private static final String ACCUMULATOR = "accumulator";

    private final MimaRunner mimaRunner;

    private final ProtectedScrollTable table;
    private boolean binaryView = false;

    @Contract(pure = true)
    public MemoryTableView(final MimaRunner mimaRunner, final ProtectedScrollTable table) {
        this.mimaRunner = mimaRunner;
        this.table = table;
    }

    private static String getAssociation(
            @NotNull final Map<String, Integer> associations, final int value) {
        return associations.entrySet().stream()
                       .filter(entry -> entry.getValue() == value)
                       .findFirst()
                       .map(Map.Entry::getKey)
                       .orElse(null);
    }

    @Override
    public void updateView() {
        SwingUtilities.invokeLater(() -> {
            table.setContent(getMemoryTable());
            table.clearIcons();
            table.setIcon(Icons.STACK_POINTER, mimaRunner.getMima().getStackPointer().intValue() + 1, 0);
        });
    }

    /**
     * Set whether to use the binary representation of the memory values.
     *
     * @param binaryView true if binary should be used.
     */
    public void setBinaryView(final boolean binaryView) {
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
    @NotNull
    public Object[][] getMemoryTable() {
        Environment scope = mimaRunner.getCurrentEnvironment();
        final Map<String, Integer> map = new HashMap<>();
        while (scope != null) {
            map.putAll(
                    new HashSet<>(scope.getDefinitions().get(0).entrySet())
                            .stream()
                            .filter(e -> !map.containsKey(e.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().intValue())));
            scope = scope.returnToParent();
        }
        return createMemoryTable(map);
    }

    /**
     * Get the memory table labeled with the corresponding memory associations.
     *
     * @param associations memory associations to use
     * @return MemoryTable formatted for an n(rows) x 2(columns) {@link JTable}
     */
    @NotNull
    private Object[][] createMemoryTable(@NotNull final Map<String, Integer> associations) {
        final Mima mima = mimaRunner.getMima();
        final var memoryM = mima.getMemory();
        final var accumulator = mima.getAccumulator();
        final var stackPointer = mima.getStackPointer();

        final Map<Integer, MachineWord> values = memoryM.getMapping();
        final List<Object[]> data = new ArrayList<>();
        data.add(
                new Object[]{
                        ACCUMULATOR, binaryView ? accumulator.binaryRepresentation() : accumulator.intValue()
                });

        final List<Object[]> memory = new ArrayList<>();
        final var entryList = new ArrayList<>(values.entrySet());
        entryList.sort(Comparator.comparingInt(Map.Entry::getKey));
        for (final Map.Entry<Integer, MachineWord> entry : entryList) {
            final String valueString =
                    binaryView
                    ? entry.getValue().binaryRepresentation()
                    : String.valueOf(entry.getValue().intValue());
            final Object[] element =
                    associations.containsValue(entry.getKey())
                    ? new Object[]{
                            entry.getKey() + " (" + getAssociation(associations, entry.getKey()) + ')',
                            valueString
                    }
                    : new Object[]{entry.getKey(), valueString};
            boolean skip;
            try {
                final int value = Integer.parseInt(element[0].toString());
                skip = value < 0;
            } catch (@NotNull final NumberFormatException e) {
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
