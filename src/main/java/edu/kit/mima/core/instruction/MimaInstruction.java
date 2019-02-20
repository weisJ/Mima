package edu.kit.mima.core.instruction;

import edu.kit.mima.core.Mima;
import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.interpretation.Environment;
import edu.kit.mima.core.interpretation.Value;
import edu.kit.mima.core.logic.ArithmeticLogicUnit;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Jannis Weis
 * @since 2018
 */
public enum MimaInstruction implements Instruction {

    /**
     * Load Constant to accumulator
     */
    LDC("LDC", 1) {
        @Override
        protected MachineWord applyInternal(List<Value<MachineWord>> arguments, Environment environment) {
            var argument = InstructionTools.getReferenceValue(arguments, 0);
            MachineWord value = argument.getValue();
            if (!(mima.getWordLength() == mima.getConstWordLength()) && (value.intValue() < 0)) {
                InstructionTools.fail("can't pass negative values");
            }
            mima.setAccumulator(value);
            return null;
        }
    },
    /**
     * Load value from memory
     */
    LDV("LDV", 1) {
        @Override
        protected MachineWord applyInternal(List<Value<MachineWord>> arguments, Environment environment) {
            var argument = InstructionTools.getMemoryReference(arguments, 0);
            mima.setAccumulator(mima.loadValue(argument.getValue().intValue()));
            return null;
        }
    },
    /**
     * Store value to memory
     */
    STV("STV", 1) {
        @Override
        protected MachineWord applyInternal(List<Value<MachineWord>> arguments, Environment environment) {
            var argument = InstructionTools.getMemoryReference(arguments, 0);
            mima.storeValue(argument.getValue().intValue(), mima.getAccumulator());
            return null;
        }
    },
    /**
     * Load value indirect from memory
     */
    LDIV("LDIV", 1) {
        @Override
        protected MachineWord applyInternal(List<Value<MachineWord>> arguments, Environment environment) {
            var argument = InstructionTools.getMemoryReference(arguments, 0);
            mima.setAccumulator(mima.loadValue(mima.loadValue(argument.getValue().intValue()).intValue()));
            return null;
        }
    },
    /**
     * Store value indirect to memory
     */
    STIV("STIV", 1) {
        @Override
        protected MachineWord applyInternal(List<Value<MachineWord>> arguments, Environment environment) {
            var argument = InstructionTools.getMemoryReference(arguments, 0);
            mima.storeValue(mima.loadValue(argument.getValue().intValue()).intValue(), mima.getAccumulator());
            return null;
        }
    },
    /**
     * Rotate accumulator right by one bit
     */
    RAR("RAR", 0) {
        @Override
        protected MachineWord applyInternal(List<Value<MachineWord>> arguments, Environment environment) {
            mima.setAccumulator(arithmeticLogicUnit.rar(mima.getAccumulator()));
            return null;
        }
    },
    /**
     * Invert bits in accumulator
     */
    NOT("NOT", 0) {
        @Override
        protected MachineWord applyInternal(List<Value<MachineWord>> arguments, Environment environment) {
            mima.setAccumulator(mima.getAccumulator().invert());
            return null;
        }
    },
    /**
     * Add with accumulator
     */
    ADD("ADD", 1) {
        @Override
        protected MachineWord applyInternal(List<Value<MachineWord>> arguments, Environment environment) {
            var argument = InstructionTools.getMemoryReference(arguments, 0);
            mima.setAccumulator(arithmeticLogicUnit.add(
                    MachineWord.cast(mima.getAccumulator(), mima.getWordLength()),
                    mima.loadValue(argument.getValue().intValue())));
            return null;
        }
    },
    /**
     * And bitwise with accumulator
     */
    AND("AND", 1) {
        @Override
        protected MachineWord applyInternal(List<Value<MachineWord>> arguments, Environment environment) {
            var argument = InstructionTools.getMemoryReference(arguments, 0);
            mima.setAccumulator(arithmeticLogicUnit.and(
                    MachineWord.cast(mima.getAccumulator(), mima.getWordLength()),
                    mima.loadValue(argument.getValue().intValue())));
            return null;
        }
    },
    /**
     * or bitwise with accumulator
     */
    OR("OR", 1) {
        @Override
        protected MachineWord applyInternal(List<Value<MachineWord>> arguments, Environment environment) {
            var argument = InstructionTools.getMemoryReference(arguments, 0);
            mima.setAccumulator(arithmeticLogicUnit.or(
                    MachineWord.cast(mima.getAccumulator(), mima.getWordLength()),
                    mima.loadValue(argument.getValue().intValue())));
            return null;
        }
    },
    /**
     * xor bitwise with accumulator
     */
    XOR("XOR", 1) {
        @Override
        protected MachineWord applyInternal(List<Value<MachineWord>> arguments, Environment environment) {
            var argument = InstructionTools.getMemoryReference(arguments, 0);
            mima.setAccumulator(arithmeticLogicUnit.xor(
                    MachineWord.cast(mima.getAccumulator(), mima.getWordLength()),
                    mima.loadValue(argument.getValue().intValue())));
            return null;
        }
    },
    /**
     * Store -1 to accumulator if negativeIfEquals, else load 0
     */
    EQL("EQL", 1) {
        @Override
        protected MachineWord applyInternal(List<Value<MachineWord>> arguments, Environment environment) {
            var argument = InstructionTools.getMemoryReference(arguments, 0);
            mima.setAccumulator(arithmeticLogicUnit.negativeIfEquals(
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
    private final int argNum;

    /**
     * Mima Instructions
     *
     * @param instruction instruction keyword
     */
    MimaInstruction(String instruction, int argNum) {
        this.instruction = instruction;
        this.argNum = argNum;
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

    @Override
    public String toString() {
        return instruction;
    }

    @Override
    public @Nullable MachineWord apply(List<Value<MachineWord>> arguments, Environment environment) {
        InstructionTools.checkArgNumber(arguments, this.argNum);
        return this.applyInternal(arguments, environment);
    }

    /**
     * Internal apply function
     *
     * @param arguments   argument list
     * @param environment execution environment
     * @return return value of instruction
     */
    protected abstract @Nullable MachineWord applyInternal(List<Value<MachineWord>> arguments, Environment environment);

}
