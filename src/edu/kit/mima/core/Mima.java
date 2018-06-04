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

    private MachineWord accumulator;
    private MachineWord stackPointer;
    private Stack<Integer> returnStack;

    private boolean running;
    private boolean extendedInstructions;

    public Mima() {
        memory = new Memory(WORD_LENGTH);
        alu = new ArithmeticLogicUnit(WORD_LENGTH);
        accumulator = new MachineWord(0, WORD_LENGTH);
        stackPointer = accumulator.copy();
        returnStack = new Stack<>();
    }

    public static String[] getInstructionSet() {
        return new String[]{"LDC", "LDV", "STV", "STIV", "LDIV", "RAR", "NOT", "ADD", "AND", "OR", "XOR", "EQL", "HALT", "JMP", "JMN"};
    }

    public static String[] getMimaXInstructionSet() {
        return new String[]{"ADC", "CALL", "RET", "LDSP", "STSP", "STVR", "SP", "LDVR"};
    }

    public boolean usesExtendedInstructionSet() {
        return extendedInstructions;
    }

    public void reset() {
        icu.reset();
        memory.reset();
        accumulator.setValue(0);
    }

    public void loadProgram(String[] lines, boolean extendedInstructions) {
        this.extendedInstructions = extendedInstructions;
        if (extendedInstructions) {
            icu = new Interpreter(lines, WORD_LENGTH, WORD_LENGTH);
        } else {
            icu = new Interpreter(lines, WORD_LENGTH_CONST, WORD_LENGTH);
        }
        memory.empty();
        icu.compile();
    }

    public Object[][] memoryTable() {
        Map<Integer, MachineWord> values = memory.getMemory();
        Map<String, Integer> associations = icu.getMemoryLookupTable();
        Object[][] data = new Object[values.values().size() + 1][];
        data[0] = new Object[]{"accumulator", accumulator};
        int index = 1;
        for (int i : values.keySet()) {
            if (associations.containsValue(i)) {
                data[index] = new Object[]{getAssociation(associations, i), values.get(i).intValue()};
            } else {
                data[index] = new Object[]{i, values.get(i).intValue()};
            }
            System.out.println(data[i][0]);
            if (i == stackPointer.intValue()) {
                data[i][0] = data[i][0] + "(SP)";
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
            Command command = icu.nextInstruction();
            boolean finished = processMimaCommand(command);
            if (finished)
                return;
            if (extendedInstructions) {
                finished = processMimaXCommand(command);
            }
            if (!finished)
                fail("unknown instruction <" + command.getCommand() + "> at line " + (icu.getInstructionPointer() + 1));
        } else {
            stop();
            throw new IllegalArgumentException("no more instructions");
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

    private boolean processMimaCommand(Command command) {
        boolean found = true;
        switch (command.getCommand()) {
            case "LDC":
                if (command.isReference()) {
                    fail("can't pass a reference at line " + (icu.getInstructionPointer() + 1));
                }
                if (command.getValue().intValue() < 0) {
                    fail("can't pass negative values in line " + (icu.getInstructionPointer() + 1));
                }
                accumulator = command.getValue();
                break;
            case "LDV":
                if (!command.isReference() && command.getValue().intValue() < 0) {
                    fail("invalid memory address at line " + (icu.getInstructionPointer() + 1));
                }
                accumulator = memory.loadValue(command.getValue().intValue());
                break;
            case "STV":
                if (!command.isReference() && command.getValue().intValue() < 0) {
                    fail("invalid memory address at line " + (icu.getInstructionPointer() + 1));
                }
                memory.storeValue(command.getValue().intValue(), accumulator.copy());
                break;
            case "LDIV":
                if (!command.isReference() && command.getValue().intValue() < 0) {
                    fail("invalid memory address at line " + (icu.getInstructionPointer() + 1));
                }
                accumulator = memory.loadValue(memory.loadValue(command.getValue().intValue()).intValue());
                break;
            case "STIV":
                if (!command.isReference() && command.getValue().intValue() < 0) {
                    fail("invalid memory address at line " + (icu.getInstructionPointer() + 1));
                }
                memory.storeValue(memory.loadValue(command.getValue().intValue()).intValue(), accumulator.copy());
                break;
            case "RAR":
                if (command.hasCommand()) {
                    fail("unexpected argument at line " + (icu.getInstructionPointer() + 1));
                }
                accumulator = alu.RAR(accumulator);
                break;
            case "NOT":
                if (command.hasCommand()) {
                    fail("unexpected argument at line " + (icu.getInstructionPointer() + 1));
                }
                accumulator.invert();
                break;
            case "ADD":
                if (!command.isReference() && command.getValue().intValue() < 0) {
                    fail("invalid memory address at line " + (icu.getInstructionPointer() + 1));
                }
                accumulator = alu.ADD(MachineWord.cast(accumulator, WORD_LENGTH),
                                      memory.loadValue(command.getValue().intValue()));
                break;
            case "AND":
                if (!command.isReference() && command.getValue().intValue() < 0) {
                    fail("invalid memory address at line " + (icu.getInstructionPointer() + 1));
                }
                accumulator = alu.AND(MachineWord.cast(accumulator, WORD_LENGTH),
                                      memory.loadValue(command.getValue().intValue()));
                break;
            case "OR":
                if (!command.isReference() && command.getValue().intValue() < 0) {
                    fail("invalid memory address at line " + (icu.getInstructionPointer() + 1));
                }
                accumulator = alu.OR(MachineWord.cast(accumulator, WORD_LENGTH),
                                     memory.loadValue(command.getValue().intValue()));
                break;
            case "XOR":
                if (!command.isReference() && command.getValue().intValue() < 0) {
                    fail("invalid memory address at line " + (icu.getInstructionPointer() + 1));
                }
                accumulator = alu.XOR(MachineWord.cast(accumulator, WORD_LENGTH),
                                      memory.loadValue(command.getValue().intValue()));
                break;
            case "EQL":
                if (!command.isReference() && command.getValue().intValue() < 0) {
                    fail("invalid memory address at line " + (icu.getInstructionPointer() + 1));
                }
                accumulator = alu.EQL(MachineWord.cast(accumulator, WORD_LENGTH),
                                      memory.loadValue(command.getValue().intValue()));
                break;
            case "HALT":
                if (command.hasCommand()) {
                    fail("unexpected argument at line " + (icu.getInstructionPointer() + 1));
                }
                stop();
                break;
            case "JMP":
                icu.setInstructionPointer(command.getValue().intValue() - 1);
                break;
            case "JMN":
                if (accumulator.MSB() == 1) {
                    icu.setInstructionPointer(command.getValue().intValue() - 1);
                }
                break;
            default:
                found = false;
                break;
        }
        return found;
    }

    private boolean processMimaXCommand(Command command) {
        boolean found = true;
        switch (command.getCommand()) {
            case "CALL":
                returnStack.push(icu.getInstructionPointer() + 1);
                icu.setInstructionPointer(command.getValue().intValue() - 1);
            case "RET":
                if (command.hasCommand()) {
                    fail("unexpected argument at line " + (icu.getInstructionPointer() + 1));
                }
                if (returnStack.isEmpty()) {
                    fail("nowhere to return to");
                }
                icu.setInstructionPointer(returnStack.pop());
                break;
            case "ADC":
                if (command.isReference()) {
                    fail("can't pass a reference at line " + (icu.getInstructionPointer() + 1));
                }
                accumulator = alu.ADD(accumulator, command.getValue());
                break;
            case "LDSP":
                if (command.hasCommand()) {
                    fail("unexpected argument at line " + (icu.getInstructionPointer() + 1));
                }
                accumulator.setBits(stackPointer.getBits());
                break;
            case "STSP":
                if (command.hasCommand()) {
                    fail("unexpected argument at line " + (icu.getInstructionPointer() + 1));
                }
                int address = accumulator.intValue();
                memory.storeValue(address, memory.loadValue(address));
                stackPointer.setValue(address);
                break;
            case "STVR(SP)":
                if (command.isReference()) {
                    fail("can't pass a reference at line " + (icu.getInstructionPointer() + 1));
                }
                address = stackPointer.intValue() + command.getValue().intValue();
                if (address < 0) {
                    fail("illegal memory address <" + address + "> at line " + (icu.getInstructionPointer() + 1));
                }
                memory.storeValue(address, accumulator);
                break;
            case "LDVR(SP)":
                if (command.isReference()) {
                    fail("can't pass a reference at line " + (icu.getInstructionPointer() + 1));
                }
                address = stackPointer.intValue() + command.getValue().intValue();
                if (address < 0) {
                    fail("illegal memory address <" + address + "> at line " + (icu.getInstructionPointer() + 1));
                }
                accumulator = memory.loadValue(address);
                break;
            default:
                found = false;
                break;
        }
        return found;
    }

    public boolean isRunning() {
        return icu.hasInstructions();
    }

    private void fail(String message) {
        running = false;
        throw new IllegalArgumentException(message);
    }
}
