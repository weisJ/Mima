package edu.kit.mima.core.instruction;

import edu.kit.mima.core.Mima;
import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.interpretation.Environment;
import edu.kit.mima.core.interpretation.Value;
import edu.kit.mima.core.interpretation.ValueType;
import edu.kit.mima.core.logic.ArithmeticLogicUnit;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

/**
 * @author Jannis Weis
 * @since 2018
 */
public enum MimaInstruction implements BiFunction<List<Value<MachineWord>>, Environment, MachineWord> {

    /**
     * Load Constant to accumulator
     */
    LDC("LDC") {
        @Override
        public MachineWord apply(List<Value<MachineWord>> args, Environment environment) {
            checkArgNumber(args, 1);
            var argument = args.get(0);
            if (argument.getType() != ValueType.CONSTANT && argument.getType() != ValueType.NUMBER) {
                fail("can't pass a reference");
            }
            MachineWord value = argument.getValue();
            if (!(mima.getWordLength() == mima.getConstWordLength()) && (value.intValue() < 0)) {
                fail("can't pass negative values");
            }
            mima.setAccumulator(value);
            return null;
        }
    },
    /**
     * Load value from memory
     */
    LDV("LDV") {
        @Override
        public MachineWord apply(List<Value<MachineWord>> args, Environment environment) {
            checkArgNumber(args, 1);
            var argument = args.get(0);
            checkMemoryReference(argument);
            mima.setAccumulator(mima.loadValue(argument.getValue().intValue()));
            return null;
        }
    },
    /**
     * Store value to memory
     */
    STV("STV") {
        @Override
        public MachineWord apply(List<Value<MachineWord>> args, Environment environment) {
            checkArgNumber(args, 1);
            var argument = args.get(0);
            checkMemoryReference(argument);
            mima.storeValue(argument.getValue().intValue(), mima.getAccumulator());
            return null;
        }
    },
    /**
     * Load value indirect from memory
     */
    LDIV("LDIV") {
        @Override
        public MachineWord apply(List<Value<MachineWord>> args, Environment environment) {
            checkArgNumber(args, 1);
            var argument = args.get(0);
            checkMemoryReference(argument);
            mima.setAccumulator(mima.loadValue(mima.loadValue(argument.getValue().intValue()).intValue()));
            return null;
        }
    },
    /**
     * Store value indirect to memory
     */
    STIV("STIV") {
        @Override
        public MachineWord apply(List<Value<MachineWord>> args, Environment environment) {
            checkArgNumber(args, 1);
            var argument = args.get(0);
            checkMemoryReference(argument);
            mima.storeValue(mima.loadValue(argument.getValue().intValue()).intValue(), mima.getAccumulator());
            return null;
        }
    },
    /**
     * Rotate accumulator right by one bit
     */
    RAR("RAR") {
        @Override
        public MachineWord apply(List<Value<MachineWord>> args, Environment environment) {
            checkArgNumber(args, 0);
            mima.setAccumulator(arithmeticLogicUnit.rar(mima.getAccumulator()));
            return null;
        }
    },
    /**
     * Invert bits in accumulator
     */
    NOT("NOT") {
        @Override
        public MachineWord apply(List<Value<MachineWord>> args, Environment environment) {
            checkArgNumber(args, 0);
            mima.setAccumulator(mima.getAccumulator().invert());
            return null;
        }
    },
    /**
     * Add with accumulator
     */
    ADD("ADD") {
        @Override
        public MachineWord apply(List<Value<MachineWord>> args, Environment environment) {
            checkArgNumber(args, 1);
            var argument = args.get(0);
            checkMemoryReference(argument);
            mima.setAccumulator(arithmeticLogicUnit.add(
                    MachineWord.cast(mima.getAccumulator(), mima.getWordLength()),
                    mima.loadValue(argument.getValue().intValue())));
            return null;
        }
    },
    /**
     * And bitwise with accumulator
     */
    AND("AND") {
        @Override
        public MachineWord apply(List<Value<MachineWord>> args, Environment environment) {
            checkArgNumber(args, 1);
            var argument = args.get(0);
            checkMemoryReference(argument);
            mima.setAccumulator(arithmeticLogicUnit.and(
                    MachineWord.cast(mima.getAccumulator(), mima.getWordLength()),
                    mima.loadValue(argument.getValue().intValue())));
            return null;
        }
    },
    /**
     * or bitwise with accumulator
     */
    OR("OR") {
        @Override
        public MachineWord apply(List<Value<MachineWord>> args, Environment environment) {
            checkArgNumber(args, 1);
            var argument = args.get(0);
            checkMemoryReference(argument);
            mima.setAccumulator(arithmeticLogicUnit.or(
                    MachineWord.cast(mima.getAccumulator(), mima.getWordLength()),
                    mima.loadValue(argument.getValue().intValue())));
            return null;
        }
    },
    /**
     * xor bitwise with accumulator
     */
    XOR("XOR") {
        @Override
        public MachineWord apply(List<Value<MachineWord>> args, Environment environment) {
            checkArgNumber(args, 1);
            var argument = args.get(0);
            checkMemoryReference(argument);
            mima.setAccumulator(arithmeticLogicUnit.xor(
                    MachineWord.cast(mima.getAccumulator(), mima.getWordLength()),
                    mima.loadValue(argument.getValue().intValue())));
            return null;
        }
    },
    /**
     * Store -1 to accumulator if equals, else load 0
     */
    EQL("EQL") {
        @Override
        public MachineWord apply(List<Value<MachineWord>> args, Environment environment) {
            checkArgNumber(args, 1);
            var argument = args.get(0);
            checkMemoryReference(argument);
            mima.setAccumulator(arithmeticLogicUnit.equals(
                    MachineWord.cast(mima.getAccumulator(), mima.getWordLength()),
                    mima.loadValue(argument.getValue().intValue())));
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
    MimaInstruction(String instruction) {
        this.instruction = instruction;
    }

    /**
     * Set the mima controlled by the instructions
     *
     * @param mima Mima
     */
    public static void setMima(Mima mima) {
        MimaInstruction.mima = mima;
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
     * Check the argument for given number of arguemnts
     *
     * @param args                   arguments list
     * @param expectedArgumentNumber expected number of arguments
     */
    protected void checkArgNumber(List<Value<MachineWord>> args, int expectedArgumentNumber) {
        if (args.size() != expectedArgumentNumber) {
            fail("invalid number of arguments");
        }
    }

    /**
     * Check whether argument is a memory reference and if it is, whether the value is legal address
     *
     * @param argument argument
     */
    protected void checkMemoryReference(Value<MachineWord> argument) {
        if (argument == null
                || !(argument.getType() == ValueType.NUMBER
                             || argument.getType() == ValueType.CONSTANT
                             || argument.getType() == ValueType.MEMORY_REFERENCE)) {
            fail("must pass a memory address");
        }
        assert argument != null;
        if (!(argument.getType() == ValueType.MEMORY_REFERENCE) && argument.getValue().intValue() < 0) {
            fail("illegal memory address");
        }
    }

    @Override
    public String toString() {
        return instruction;
    }

    @Override
    public abstract @Nullable MachineWord apply(List<Value<MachineWord>> args, Environment environment);

}
