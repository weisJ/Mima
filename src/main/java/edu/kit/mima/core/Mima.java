package edu.kit.mima.core;

import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.data.MemoryMap;
import edu.kit.mima.core.interpretation.Environment;
import javafx.util.Pair;

import java.util.Stack;


/**
 * @author Jannis Weis
 * @since 2018
 */
public class Mima {

    private final int wordLength;
    private final int constWordLength;

    private final MemoryMap memoryMap;
    private final MachineWord stackPointer;
    private final Stack<Pair<Integer, Environment>> returnStack;
    private MachineWord accumulator;

    public Mima(int wordLength, int constWordLength) {
        this.wordLength = wordLength;
        this.constWordLength = constWordLength;
        memoryMap = new MemoryMap(wordLength, 100);
        accumulator = new MachineWord(0, wordLength);
        stackPointer = accumulator.clone();
        returnStack = new Stack<>();
    }

    public Object[][] memoryTable() {
//        final Map<Integer, MachineWord> values = memoryMap.getMemoryMap();
//        final Map<String, Integer> associations = icu.getMemoryLookupTable();
//        final Object[][] data = new Object[values.values().size() + 1][];
//        data[0] = new Object[]{"accumulator", accumulator};
//        int index = 1;
//        for (final Map.Entry<Integer, MachineWord> entry : values.entrySet()) {
//            data[index] = associations.containsValue(entry.getKey())
//                    ? new Object[]{getAssociation(associations, entry.getKey()), entry.getValue().intValue()}
//                    : new Object[]{entry.getKey(), entry.getValue().intValue()};
//            if (entry.getKey() == stackPointer.intValue()) {
//                data[index][0] += "(SP)";
//            }
//            index++;
//        }
//        return data;
        return null;
    }

    public int getWordLength() {
        return wordLength;
    }

    public int getConstWordLength() {
        return constWordLength;
    }

    public MachineWord loadValue(int address) {
        return memoryMap.loadValue(address);
    }

    public void storeValue(int address, MachineWord value) {
        memoryMap.storeValue(address, value);
    }

    public MachineWord getAccumulator() {
        return accumulator;
    }

    public void setAccumulator(MachineWord value) {
        accumulator = value;
    }

    public void pushRoutine(int address, Environment environment) {
        returnStack.push(new Pair<>(address, environment));
    }

    public boolean hasEmptyReturnStack() {
        return returnStack.isEmpty();
    }

    public Pair<Integer, Environment> returnRoutine() {
        return returnStack.pop();
    }

    public MachineWord getStackPointer() {
        return stackPointer.clone();
    }

    public void setStackPointer(int address) {
        stackPointer.setValue(address);
    }
}
