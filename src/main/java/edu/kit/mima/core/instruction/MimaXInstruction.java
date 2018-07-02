package edu.kit.mima.core.instruction;

import edu.kit.mima.core.Mima;
import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.interpretation.Environment;
import edu.kit.mima.core.interpretation.Value;
import edu.kit.mima.core.interpretation.ValueType;
import edu.kit.mima.core.logic.ArithmeticLogicUnit;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Jannis Weis
 * @since 2018
 */
public enum MimaXInstruction implements Instruction {

    /**
     * Add constant to accumulator
     */
    ADC("ADC") {
        @Override
        public MachineWord apply(List<Value<MachineWord>> arguments, Environment environment) {
            checkArgNumber(arguments, 1);
            var argument = arguments.get(0);
            if (argument.getType() != ValueType.CONSTANT && argument.getType() != ValueType.NUMBER) {
                fail("can't pass a reference");
            }
            mima.setAccumulator(arithmeticLogicUnit.add(mima.getAccumulator(), argument.getValue()));
            return null;
        }
    },
    /**
     * Load stack pointer
     */
    LDSP("LDSP") {
        @Override
        public MachineWord apply(List<Value<MachineWord>> arguments, Environment environment) {
            checkArgNumber(arguments, 0);
            mima.setAccumulator(mima.getStackPointer());
            return null;
        }
    },
    /**
     * Store to stack pointer
     */
    STSP("STSP") {
        @Override
        public MachineWord apply(List<Value<MachineWord>> arguments, Environment environment) {
            checkArgNumber(arguments, 0);
            int address = mima.getAccumulator().intValue();
            mima.storeValue(address, mima.loadValue(address)); //Make sure there is an memory entry for <SP>
            mima.setStackPointer(address);
            return null;
        }
    },
    /**
     * Returns the stack pointer
     */
    SP("SP") {
        @Override
        public MachineWord apply(List<Value<MachineWord>> arguments, Environment environment) {
            checkArgNumber(arguments, 0);
            return mima.getStackPointer();
        }
    },
    /**
     * Store value to stack pointer with disposition
     */
    STVR("STVR") {
        @Override
        public MachineWord apply(List<Value<MachineWord>> arguments, Environment environment) {
            checkArgNumber(arguments, 2);
            var first = arguments.get(0);
            var second = arguments.get(1);
            if ((first.getType() != ValueType.CONSTANT && first.getType() != ValueType.NUMBER)
                    || (second.getType() != ValueType.CONSTANT && second.getType() != ValueType.NUMBER)) {
                fail("can't pass a reference");
            }
            int address = first.getValue().intValue() + second.getValue().intValue();
            if (address < 0) {
                fail("illegal memory address");
            }
            mima.storeValue(address, mima.getAccumulator());
            return null;
        }
    },
    /**
     * Load value from stack pointer with disposition
     */
    LDVR("LDVR") {
        @Override
        public MachineWord apply(List<Value<MachineWord>> arguments, Environment environment) {
            checkArgNumber(arguments, 2);
            var first = arguments.get(0);
            var second = arguments.get(1);
            if ((first.getType() != ValueType.CONSTANT && first.getType() != ValueType.NUMBER)
                    || (second.getType() != ValueType.CONSTANT && second.getType() != ValueType.NUMBER)) {
                fail("can't pass a reference");
            }
            int address = first.getValue().intValue() + second.getValue().intValue();
            if (address < 0) {
                fail("illegal memory address");
            }
            mima.setAccumulator(mima.loadValue(address));
            return null;
        }
    };


    @SuppressWarnings("NonFinalFieldInEnum")
    private static Mima mima;
    @SuppressWarnings("NonFinalFieldInEnum")
    private static ArithmeticLogicUnit arithmeticLogicUnit;

    private final String instruction;

    /**
     * Mima Instructions
     *
     * @param instruction instruction keyword
     */
    MimaXInstruction(String instruction) {
        this.instruction = instruction;
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
     * Throw error with given message
     *
     * @param message fail message
     */
    protected void fail(final String message) {
        throw new IllegalArgumentException(message);
    }

    /**
     * Check the argument for given number of arguments
     *
     * @param args                   arguments list
     * @param expectedArgumentNumber expected number of arguments
     */
    protected void checkArgNumber(List<Value<MachineWord>> args, int expectedArgumentNumber) {
        if (args.size() != expectedArgumentNumber) {
            fail("invalid number of arguments");
        }
    }

    @Override
    public String toString() {
        return instruction;
    }

    @Override
    public abstract @Nullable MachineWord apply(List<Value<MachineWord>> arguments, Environment environment);

}
