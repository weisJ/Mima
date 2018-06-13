package edu.kit.mima.core.instruction;

import edu.kit.mima.core.ArithmeticLogicUnit;
import edu.kit.mima.core.Mima;
import edu.kit.mima.core.parsing.legacy.CompiledInstruction;
import edu.kit.mima.core.parsing.legacy.InterpretationException;

/**
 * @author Jannis Weis
 * @since 2018
 */
public enum MimaXInstructions implements Instruction {

    /**
     * CALL subroutine
     */
    CALL("CALL") {
        @Override
        public void run(CompiledInstruction instruction) {
            mima.pushRoutine(mima.getInstructionPointer() + 1);
            mima.setInstructionPointer(instruction.getValue().intValue() - 1);
        }
    },
    /**
     * RETurn from subroutine
     */
    RET("RET") {
        @Override
        public void run(CompiledInstruction instruction) {
            if (instruction.holdsValue()) {
                fail("unexpected argument", mima.getInstructionPointer() + 1);
            }
            if (!mima.canReturn()) {
                fail("nowhere to return to", mima.getInstructionPointer() + 1);
            }
            mima.setInstructionPointer(mima.returnRoutine());
        }
    },
    /**
     * Add constant to accumulator
     */
    ADC("ADC") {
        @Override
        public void run(CompiledInstruction instruction) {
            if (instruction.isReference()) {
                fail("can't pass a reference", mima.getInstructionPointer() + 1);
            }
            mima.setAccumulator(arithmeticLogicUnit.add(mima.getAccumulator(), instruction.getValue()));
        }
    },
    /**
     * Load stack pointer
     */
    LDSP("LDSP") {
        @Override
        public void run(CompiledInstruction instruction) {
            if (instruction.holdsValue()) {
                fail("unexpected argument", mima.getInstructionPointer() + 1);
            }
            mima.setAccumulator(mima.getStackPointer());
        }
    },
    /**
     * Store to stack pointer
     */
    STSP("STSP") {
        @Override
        public void run(CompiledInstruction instruction) {
            if (instruction.holdsValue()) {
                fail("unexpected argument", mima.getInstructionPointer() + 1);
            }
            int address = mima.getAccumulator().intValue();
            mima.storeValue(address, mima.loadValue(address));
            mima.setStackPointer(address);
        }
    },
    /**
     * Store value to stack pointer with disposition
     */
    STVRSP("STVR(SP)") {
        @Override
        public void run(CompiledInstruction instruction) {
            if (instruction.isReference()) {
                fail("can't pass a reference", mima.getInstructionPointer() + 1);
            }
            int address = mima.getStackPointer().intValue() + instruction.getValue().intValue();
            if (address < 0) {
                fail("illegal memory address", mima.getInstructionPointer() + 1);
            }
            mima.storeValue(address, mima.getAccumulator());
        }
    },
    /**
     * Load value from stack pointer with disposition
     */
    LDVRSP("LDVR(SP)") {
        @Override
        public void run(CompiledInstruction instruction) {
            if (instruction.isReference()) {
                fail("can't pass a reference", mima.getInstructionPointer() + 1);
            }
            int address = mima.getStackPointer().intValue() + instruction.getValue().intValue();
            if (address < 0) {
                fail("illegal memory address", mima.getInstructionPointer() + 1);
            }
            mima.setAccumulator(mima.loadValue(address));
        }
    };


    private static Mima mima;
    private static ArithmeticLogicUnit arithmeticLogicUnit;
    private final String instruction;

    /**
     * MimaX Instructions
     *
     * @param instruction instruction keyword
     */
    MimaXInstructions(String instruction) {
        this.instruction = instruction;
    }

    /**
     * Set the mima controlled by the instructions
     *
     * @param mima Mima
     */
    public static void setMima(Mima mima) {
        MimaXInstructions.mima = mima;
        arithmeticLogicUnit = new ArithmeticLogicUnit(mima.getWordLength());
    }

    private static void fail(final String message, final int lineNumber) {
        mima.stop();
        throw new InterpretationException(message, lineNumber);
    }

    @Override
    public String toString() {
        return instruction;
    }

    @Override
    public boolean matches(String instruction) {
        return this.instruction.matches(instruction);
    }

    @Override
    public abstract void run(CompiledInstruction instruction);

}
