package edu.kit.mima.core.instruction;

import edu.kit.mima.core.Mima;
import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.interpretation.Value;
import edu.kit.mima.core.interpretation.ValueType;
import edu.kit.mima.core.interpretation.environment.Environment;
import edu.kit.mima.core.logic.ArithmeticLogicUnit;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Jannis Weis
 * @since 2018
 */
public enum MimaXInstruction implements Instruction {

    /**
     * Add constant to accumulator
     */
    ADC("ADC", 1) {
        @Override
        protected MachineWord applyInternal(List<Value> arguments, Environment environment) {
            var argument = InstructionTools.getReferenceValue(arguments, 0);
            mima.setAccumulator(arithmeticLogicUnit.add(mima.getAccumulator(), (MachineWord)argument.getValue()));
            return null;
        }
    },
    /**
     * Load stack pointer
     */
    LDSP("LDSP", 0) {
        @Override
        protected MachineWord applyInternal(List<Value> arguments, Environment environment) {
            mima.setAccumulator(mima.getStackPointer().clone());
            return null;
        }
    },
    /**
     * Store to stack pointer
     */
    STSP("STSP", 0) {
        @Override
        protected MachineWord applyInternal(List<Value> arguments, Environment environment) {
            int address = mima.getAccumulator().intValue();
            mima.storeValue(address, mima.loadValue(address)); //Make sure there is an memory entry for <SP>
            mima.setStackPointer(address);
            return null;
        }
    },
    /**
     * Returns the stack pointer
     */
    SP("SP", 0) {
        @Override
        protected MachineWord applyInternal(List<Value> arguments, Environment environment) {
            return mima.getStackPointer().clone();
        }
    },
    /**
     * Store value to stack pointer with disposition
     */
    STVR("STVR", 2) {
        @Override
        protected MachineWord applyInternal(List<Value> arguments, Environment environment) {
            int address = getOffsetAddress(arguments);
            mima.storeValue(address, mima.getAccumulator());
            return null;
        }
    },
    /**
     * Load value from stack pointer with disposition
     */
    LDVR("LDVR", 2) {
        @Override
        protected MachineWord applyInternal(List<Value> arguments, Environment environment) {
            int address = getOffsetAddress(arguments);
            mima.setAccumulator(mima.loadValue(address));
            return null;
        }
    };

    @SuppressWarnings("NonFinalFieldInEnum")
    private static Mima mima;
    @SuppressWarnings("NonFinalFieldInEnum")
    private static ArithmeticLogicUnit arithmeticLogicUnit;
    private final String instruction;
    private final int argNum;

    /**
     * Mima Instructions
     *
     * @param instruction instruction keyword
     */
    MimaXInstruction(String instruction, int argNum) {
        this.instruction = instruction;
        this.argNum = argNum;
    }

    /**
     * Set the mima controlled by the instructions
     *
     * @param mima Mima
     */
    public static void setMima(Mima mima) {
        MimaXInstruction.mima = mima;
        arithmeticLogicUnit = new ArithmeticLogicUnit(mima.getWordLength());
    }

    /**
     * Get an offset address value
     *
     * @param arguments argument list. first index, second offset
     * @return absolute index
     */
    protected int getOffsetAddress(List<Value> arguments) {
        var first = InstructionTools.getReferenceValue(arguments, 0);
        var second = InstructionTools.getReferenceValue(arguments, 1);
        int address = ((MachineWord)first.getValue()).intValue()
                + ((MachineWord)second.getValue()).intValue();
        if (address < 0) {
            InstructionTools.fail("illegal memory address");
        }
        return address;
    }

    @Override
    public String toString() {
        return instruction;
    }

    @Override
    public void apply(List<Value> arguments, Environment environment,
                                Consumer<Value> callback) {
        InstructionTools.checkArgNumber(arguments, this.argNum);
        callback.accept(new Value<>(ValueType.NUMBER, this.applyInternal(arguments, environment)));
    }

    /**
     * Internal apply function
     *
     * @param arguments   argument list
     * @param environment execution environment
     * @return return value of instruction
     */
    protected abstract @Nullable MachineWord applyInternal(List<Value> arguments, Environment environment);


}
