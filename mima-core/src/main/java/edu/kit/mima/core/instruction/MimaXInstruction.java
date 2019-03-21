package edu.kit.mima.core.instruction;

import edu.kit.mima.core.Mima;
import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.interpretation.Value;
import edu.kit.mima.core.interpretation.ValueType;
import edu.kit.mima.core.interpretation.environment.Environment;
import edu.kit.mima.core.logic.ArithmeticLogicUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Instructions for MimaX. These instructions extend the functionality of {@link MimaInstruction}.
 * Thus the instructions in {@link MimaInstruction} should also be included if using this.
 *
 * @author Jannis Weis
 * @since 2018
 */
public enum MimaXInstruction implements Instruction {

    /**
     * Add constant to accumulator.
     */
    ADC("ADC", 1) {
        @Override
        protected MachineWord applyInternal(@NotNull final List<Value> arguments,
                                            final Environment environment) {
            final var argument = InstructionTools.getReferenceValue(arguments, 0);
            mima.setAccumulator(arithmeticLogicUnit.add(mima.getAccumulator(),
                                                        (MachineWord) argument.getValue()));
            return null;
        }
    },
    /**
     * Load stack pointer.
     */
    LDSP("LDSP", 0) {
        @Override
        protected MachineWord applyInternal(final List<Value> arguments,
                                            final Environment environment) {
            mima.setAccumulator(mima.getStackPointer().clone());
            return null;
        }
    },
    /**
     * Store to stack pointer.
     */
    STSP("STSP", 0) {
        @Override
        protected MachineWord applyInternal(final List<Value> arguments,
                                            final Environment environment) {
            final int address = mima.getAccumulator().intValue();
            mima.storeValue(address, mima.loadValue(address));
            mima.setStackPointer(address);
            return null;
        }
    },
    /**
     * Returns the stack pointer.
     */
    SP("SP", 0) {
        @Override
        protected MachineWord applyInternal(final List<Value> arguments,
                                            final Environment environment) {
            return mima.getStackPointer().clone();
        }
    },
    /**
     * Store value to stack pointer with disposition.
     */
    STVR("STVR", 2) {
        @Override
        protected MachineWord applyInternal(@NotNull final List<Value> arguments,
                                            final Environment environment) {
            final int address = getOffsetAddress(arguments);
            mima.storeValue(address, mima.getAccumulator());
            return null;
        }
    },
    /**
     * Load value from stack pointer with disposition.
     */
    LDVR("LDVR", 2) {
        @Override
        protected MachineWord applyInternal(@NotNull final List<Value> arguments,
                                            final Environment environment) {
            final int address = getOffsetAddress(arguments);
            mima.setAccumulator(mima.loadValue(address));
            return null;
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
    MimaXInstruction(final String instruction, final int argNum) {
        this.instruction = instruction;
        this.argNum = argNum;
    }

    /**
     * Set the mima controlled by the instructions.
     *
     * @param mima Mima
     */
    public static void setMima(final Mima mima) {
        MimaXInstruction.mima = mima;
        arithmeticLogicUnit = new ArithmeticLogicUnit(mima.getWordLength());
    }

    /**
     * Get an offset address value.
     *
     * @param arguments argument list. first index, second offset
     * @return absolute index
     */
    protected int getOffsetAddress(@NotNull final List<Value> arguments) {
        final var first = InstructionTools.getReferenceValue(arguments, 0);
        final var second = InstructionTools.getReferenceValue(arguments, 1);
        final int address = ((MachineWord) first.getValue()).intValue()
                + ((MachineWord) second.getValue()).intValue();
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
    public void apply(@NotNull final List<Value> arguments,
                      final Environment environment,
                      @NotNull final Consumer<Value> callback) {
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
    protected abstract @Nullable MachineWord applyInternal(List<Value> arguments,
                                                           Environment environment);


}
