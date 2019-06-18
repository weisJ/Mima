package edu.kit.mima.core.instruction;

import edu.kit.mima.core.Mima;
import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.interpretation.Value;
import edu.kit.mima.core.interpretation.ValueType;
import edu.kit.mima.core.interpretation.environment.Environment;
import edu.kit.mima.core.logic.ArithmeticLogicUnit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Instructions for Mima.
 *
 * @author Jannis Weis
 * @since 2018
 */
public enum MimaInstruction implements Instruction {

    /**
     * Load Constant to accumulator.
     */
    LDC("LDC", 1) {
        @Override
        protected MachineWord applyInternal(
                @NotNull final List<Value<?>> arguments, final Environment environment) {
            final var argument = InstructionTools.getReferenceValue(arguments, 0);
            final MachineWord value = (MachineWord) argument.getValue();
            if (!(mima.getWordLength() == mima.getConstWordLength()) && (value.intValue() < 0)) {
                InstructionTools.fail("can't pass negative values");
            }
            mima.setAccumulator(value);
            return null;
        }
    },
    /**
     * Load value from memory.
     */
    LDV("LDV", 1) {
        @Override
        protected MachineWord applyInternal(
                @NotNull final List<Value<?>> arguments, final Environment environment) {
            final var argument = InstructionTools.getMemoryReference(arguments, 0);
            mima.setAccumulator(mima.loadValue(((MachineWord) argument.getValue()).intValue()));
            return null;
        }
    },
    /**
     * Store value to memory.
     */
    STV("STV", 1) {
        @Override
        protected MachineWord applyInternal(
                @NotNull final List<Value<?>> arguments, final Environment environment) {
            final var argument = InstructionTools.getMemoryReference(arguments, 0);
            mima.storeValue(((MachineWord) argument.getValue()).intValue(), mima.getAccumulator());
            return null;
        }
    },
    /**
     * Load value indirect from memory.
     */
    LDIV("LDIV", 1) {
        @Override
        protected MachineWord applyInternal(
                @NotNull final List<Value<?>> arguments, final Environment environment) {
            final var argument = InstructionTools.getMemoryReference(arguments, 0);
            mima.setAccumulator(
                    mima.loadValue(
                            mima.loadValue(((MachineWord) argument.getValue()).intValue()).intValue()));
            return null;
        }
    },
    /**
     * Store value indirect to memory.
     */
    STIV("STIV", 1) {
        @Override
        protected MachineWord applyInternal(
                @NotNull final List<Value<?>> arguments, final Environment environment) {
            final var argument = InstructionTools.getMemoryReference(arguments, 0);
            mima.storeValue(
                    mima.loadValue(((MachineWord) argument.getValue()).intValue()).intValue(),
                    mima.getAccumulator());
            return null;
        }
    },
    /**
     * Rotate accumulator right by one bit.
     */
    RAR("RAR", 0) {
        @Override
        protected MachineWord applyInternal(
                final List<Value<?>> arguments, final Environment environment) {
            mima.setAccumulator(arithmeticLogicUnit.rar(mima.getAccumulator()));
            return null;
        }
    },
    /**
     * Invert bits in accumulator.
     */
    NOT("NOT", 0) {
        @Override
        protected MachineWord applyInternal(
                final List<Value<?>> arguments, final Environment environment) {
            mima.setAccumulator(mima.getAccumulator().invert());
            return null;
        }
    },
    /**
     * Add with accumulator.
     */
    ADD("ADD", 1) {
        @Override
        protected MachineWord applyInternal(
                @NotNull final List<Value<?>> arguments, final Environment environment) {
            return applyAlu(arguments, arithmeticLogicUnit::add);
        }
    },
    /**
     * And bitwise with accumulator.
     */
    AND("AND", 1) {
        @Override
        protected MachineWord applyInternal(
                @NotNull final List<Value<?>> arguments, final Environment environment) {
            return applyAlu(arguments, arithmeticLogicUnit::and);
        }
    },
    /**
     * or bitwise with accumulator.
     */
    OR("OR", 1) {
        @Override
        protected MachineWord applyInternal(
                @NotNull final List<Value<?>> arguments, final Environment environment) {
            return applyAlu(arguments, arithmeticLogicUnit::or);
        }
    },
    /**
     * xor bitwise with accumulator.
     */
    XOR("XOR", 1) {
        @Override
        protected MachineWord applyInternal(
                @NotNull final List<Value<?>> arguments, final Environment environment) {
            return applyAlu(arguments, arithmeticLogicUnit::xor);
        }
    },
    /**
     * Store -1 to accumulator if negativeIfEquals, else load 0.
     */
    EQL("EQL", 1) {
        @Override
        protected MachineWord applyInternal(
                @NotNull final List<Value<?>> arguments, final Environment environment) {
            return applyAlu(arguments, arithmeticLogicUnit::negativeIfEquals);
        }
    };

    private static Mima mima;
    private static ArithmeticLogicUnit arithmeticLogicUnit;

    private final String instruction;
    private final int argNum;

    /**
     * Mima Instructions.
     *
     * @param instruction instruction keyword
     */
    @Contract(pure = true)
    MimaInstruction(final String instruction, final int argNum) {
        this.instruction = instruction;
        this.argNum = argNum;
    }

    /**
     * Set the mima controlled by the instructions.
     *
     * @param mima Mima
     */
    public static void setMima(@NotNull final Mima mima) {
        MimaInstruction.mima = mima;
        arithmeticLogicUnit = new ArithmeticLogicUnit(mima.getWordLength());
    }

    @Override
    public String toString() {
        return instruction;
    }

    @Override
    public void apply(
            @NotNull final List<Value<?>> arguments,
            final Environment environment,
            @NotNull final Consumer<Value<?>> callback) {
        InstructionTools.checkArgNumber(arguments, this.argNum);
        callback.accept(new Value<>(ValueType.NUMBER, this.applyInternal(arguments, environment)));
    }

    /**
     * Internal apply function.
     *
     * @param arguments   argument list
     * @param environment execution environment
     * @return return value of instruction
     */
    protected abstract @Nullable MachineWord applyInternal(
            List<Value<?>> arguments, Environment environment);

    protected @Nullable MachineWord applyAlu(
            @NotNull final List<Value<?>> arguments,
            @NotNull final BiFunction<MachineWord, MachineWord, MachineWord> func) {
        final var argument = InstructionTools.getMemoryReference(arguments, 0);
        mima.setAccumulator(
                func.apply(
                        MachineWord.cast(mima.getAccumulator(), mima.getWordLength()),
                        mima.loadValue((((MachineWord) argument.getValue()).intValue()))));
        return null;
    }
}
