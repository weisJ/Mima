package edu.kit.mima.core;

import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.data.Memory;
import edu.kit.mima.core.instruction.MimaInstructions;
import edu.kit.mima.core.instruction.MimaXInstructions;
import edu.kit.mima.core.parsing.CompiledInstruction;
import edu.kit.mima.core.parsing.InterpretationException;
import edu.kit.mima.core.parsing.Interpreter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;


/**
 * @author Jannis Weis
 * @since 2018
 */
public class Mima {

    private static final int WORD_LENGTH = 24;
    private static final int WORD_LENGTH_CONST = 20;

    private final Memory memory;
    private final MachineWord stackPointer;
    private final Stack<Integer> returnStack;
    private MachineWord accumulator;
    private Interpreter icu;
    private boolean running;
    private boolean extendedInstructions;

    public Mima() {
        super();
        memory = new Memory(WORD_LENGTH, 100);
        accumulator = new MachineWord(0, WORD_LENGTH);
        stackPointer = accumulator.copy();
        returnStack = new Stack<>();
    }

    private static String getAssociation(final Map<String, Integer> associations, final int value) {
        return associations.entrySet().stream().filter(entry -> entry.getValue() == value).findFirst()
                .map(Map.Entry::getKey).orElse(null);
    }

    public String[] getInstructionSet() {
        Set<String> instructions = Arrays.stream(MimaInstructions.values())
                .map(MimaInstructions::toString)
                .collect(Collectors.toSet());
        if (extendedInstructions) {
            instructions.addAll(Arrays.stream(MimaXInstructions.values())
                                        .map(MimaXInstructions::toString)
                                        .collect(Collectors.toSet()));
        }
        return instructions.toArray(new String[0]);
    }

    public void reset() {
        icu.reset();
        memory.reset();
        accumulator.setValue(0);
    }

    public void loadProgram(final String[] lines, final boolean extendedInstructions) {
        this.extendedInstructions = extendedInstructions;
        icu = extendedInstructions ? new Interpreter(lines, WORD_LENGTH, WORD_LENGTH)
                                   : new Interpreter(lines, WORD_LENGTH_CONST, WORD_LENGTH);
        memory.empty();
        icu.compile();
    }

    public Object[][] memoryTable() {
        final Map<Integer, MachineWord> values = memory.getMemory();
        final Map<String, Integer> associations = icu.getMemoryLookupTable();
        final Object[][] data = new Object[values.values().size() + 1][];
        data[0] = new Object[]{"accumulator", accumulator};
        int index = 1;
        for (final Map.Entry<Integer, MachineWord> entry : values.entrySet()) {
            data[index] = associations.containsValue(entry.getKey())
                          ? new Object[]{getAssociation(associations, entry.getKey()), entry.getValue().intValue()}
                          : new Object[]{entry.getKey(), entry.getValue().intValue()};
            if (entry.getKey() == stackPointer.intValue()) {
                data[index][0] += "(SP)";
            }
            index++;
        }
        return data;
    }

    public int getInstructionPointer() {
        return icu.getInstructionPointer();
    }

    public void setInstructionPointer(int address) {
        icu.setInstructionPointer(address);
    }

    public void stop() {
        running = false;
    }

    public void step() {
        final CompiledInstruction compiledInstruction = icu.nextInstruction();
        for (MimaInstructions instruction : MimaInstructions.values()) {
            if (instruction.matches(compiledInstruction.getCommand())) {
                instruction.run(compiledInstruction);
                return;
            }
        }
        if (extendedInstructions) {
            for (MimaXInstructions instruction : MimaXInstructions.values()) {
                if (instruction.matches(compiledInstruction.getCommand())) {
                    instruction.run(compiledInstruction);
                    return;
                }
            }
        }
        running = false;
        throw new InterpretationException("unknown instruction", getInstructionPointer() + 1);
    }

    public void run() {
        running = true;
        while (running) {
            step();
        }
    }

    public int getCurrentLineIndex() {
        return icu.getInstructionPointer();
    }

    public boolean isRunning() {
        return icu.hasInstructions();
    }

    public List<Set<String>> getReferences(final String[] lines) {
        return icu.getReferences(lines);
    }

    public int getWordLength() {
        return WORD_LENGTH;
    }

    public int getWordLengthConst() {
        return extendedInstructions ? WORD_LENGTH : WORD_LENGTH_CONST;
    }

    public MachineWord loadValue(int address) {
        return memory.loadValue(address);
    }

    public MachineWord getAccumulator() {
        return accumulator.copy();
    }

    public void setAccumulator(MachineWord value) {
        accumulator = value;
    }

    public void storeValue(int address, MachineWord value) {
        memory.storeValue(address, value);
    }

    public void pushRoutine(int address) {
        returnStack.push(address);
    }

    public boolean canReturn() {
        return !returnStack.isEmpty();
    }

    public int returnRoutine() {
        return returnStack.pop();
    }

    public MachineWord getStackPointer() {
        return stackPointer.copy();
    }

    public void setStackPointer(int address) {
        stackPointer.setValue(address);
    }
}
