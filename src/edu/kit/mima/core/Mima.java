package edu.kit.mima.core;

import java.util.*;


/**
 * @author Jannis Weis
 * @since 2018
 */
public class Mima {

    private static final int WORD_LENGTH = 24;
    private static final int WORD_LENGTH_CONST = 20;
    private final Memory memory;
    private final ArithmeticLogicUnit alu;
    private Interpreter icu;
    private MachineWord akku;
    private boolean running;

    public Mima() {
        memory = new Memory(WORD_LENGTH);
        alu = new ArithmeticLogicUnit(WORD_LENGTH);
        akku = new MachineWord(0, WORD_LENGTH);
    }

    public void reset() {
        icu.reset();
        memory.reset();
        akku.setValue(0);
    }

    public void loadProgram(String[] lines) {
        icu = new Interpreter(lines, WORD_LENGTH_CONST);
        memory.empty();
        icu.compile();
    }

    public Object[][] memoryTable() {
        Map<Integer, MachineWord> values = memory.getMemory();
        Map<String, Integer> associations = icu.getMemoryLookupTable();
        Object[][] data = new Object[values.values().size() + 1][];
        data[0] = new Object[]{"akku", akku};
        int index = 1;
        for (int i : values.keySet()) {
            if (associations.containsValue(i)) {
                data[index] = new Object[]{getAssociation(associations, i), values.get(i).intValue()};
            } else {
                data[index] = new Object[]{i, values.get(i).intValue()};
            }
            index++;
        }
        return data;
    }

    private String getAssociation(Map<String, Integer> associations, int value) {
        for (Map.Entry<String, Integer> entry : associations.entrySet()) {
            if (entry.getValue() == value) return entry.getKey();
        }
        return null;
    }

    public void stop() {
        running = false;
    }

    public void step() {
        if (icu.hasInstructions()) {
            processCommand(icu.nextInstruction());
        } else {
            stop();
            throw new IllegalArgumentException("No more instructions");
        }
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

    private void processCommand(Command command) {
        switch (command.getCommand()) {
            case "LDC":
                akku = command.getValue();
                break;
            case "LDV":
                akku = memory.loadValue(command.getValue().intValue());
                break;
            case "STV":
                memory.storeValue(command.getValue().intValue(), akku.copy());
                break;
            case "LDIV":
                akku = memory.loadValue(memory.loadValue(command.getValue().intValue()).intValue());
                break;
            case "STIV":
                memory.storeValue(memory.loadValue(command.getValue().intValue()).intValue(), akku.copy());
                break;
            case "RAR":
                akku = alu.RAR(akku);
                break;
            case "NOT":
                akku.invert();
                break;
            case "ADD":
                akku = alu.ADD(MachineWord.cast(akku, WORD_LENGTH), memory.loadValue(command.getValue().intValue()));
                break;
            case "AND":
                akku = alu.AND(MachineWord.cast(akku, WORD_LENGTH), memory.loadValue(command.getValue().intValue()));
                break;
            case "OR":
                akku = alu.OR(MachineWord.cast(akku, WORD_LENGTH), memory.loadValue(command.getValue().intValue()));
                break;
            case "XOR":
                akku = alu.XOR(MachineWord.cast(akku, WORD_LENGTH), memory.loadValue(command.getValue().intValue()));
                break;
            case "EQL":
                akku = alu.EQL(MachineWord.cast(akku, WORD_LENGTH), memory.loadValue(command.getValue().intValue()));
                break;
            case "HALT":
                stop();
                break;
            case "JMP":
                icu.setInstructionPointer(command.getValue().intValue() - 1);
                break;
            case "JMN":
                if (akku.MSB() == 1) {
                    icu.setInstructionPointer(command.getValue().intValue() - 1);
                }
                break;
            default:
                throw new IllegalArgumentException("unknown instruction <" + command.getCommand()
                                                           + "> at line " + (icu.getInstructionPointer() + 1));
        }
    }

    public boolean isRunning() {
        return icu.hasInstructions();
    }

    public static String[] getInstructionSet() {
        return new String[]{"LDC", "LDV", "STV", "STIV", "LDIV", "RAR", "NOT", "ADD", "AND", "OR", "XOR", "EQL", "HALT", "JMP", "JMN"};
    }
}
