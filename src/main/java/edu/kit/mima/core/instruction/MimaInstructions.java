package edu.kit.mima.core.instruction;

import edu.kit.mima.core.Mima;
import edu.kit.mima.core.data.MachineWord;
import edu.kit.mima.core.logic.ArithmeticLogicUnit;
import edu.kit.mima.core.parsing.legacy.CompiledInstruction;
import edu.kit.mima.core.parsing.legacy.InterpretationException;

/**
 * @author Jannis Weis
 * @since 2018
 */
public enum MimaInstructions implements Instruction {

    /**
     * Load Constant to accumulator
     */
    LDC("LDC") {
        @Override
        public void run(CompiledInstruction instruction) {
            if (instruction.isReference()) {
                fail("can't pass a reference at line ", mima.getInstructionPointer() + 1);
            }
            if (!(mima.getWordLength() == mima.getWordLengthConst()) && (instruction.getValue().intValue() < 0)) {
                fail("can't pass negative values", mima.getInstructionPointer() + 1);
            }
            mima.setAccumulator(instruction.getValue());
        }
    },
    /**
     * Load value from memory
     */
    LDV("LDV") {
        @Override
        public void run(CompiledInstruction instruction) {
            if (!instruction.isReference() && (instruction.getValue().intValue() < 0)) {
                fail("illegal memory address", mima.getInstructionPointer() + 1);
            }
            mima.setAccumulator(mima.loadValue(instruction.getValue().intValue()));
        }
    },
    /**
     * Store value to memory
     */
    STV("STV") {
        @Override
        public void run(CompiledInstruction instruction) {
            if (!instruction.isReference() && (instruction.getValue().intValue() < 0)) {
                fail("illegal memory address", mima.getInstructionPointer() + 1);
            }
            mima.storeValue(instruction.getValue().intValue(), mima.getAccumulator());
        }
    },
    /**
     * Load value indirect from memory
     */
    LDIV("LDIV") {
        @Override
        public void run(CompiledInstruction instruction) {
            if (!instruction.isReference() && (instruction.getValue().intValue() < 0)) {
                fail("illegal memory address", mima.getInstructionPointer() + 1);
            }
            mima.setAccumulator(mima.loadValue(mima.loadValue(instruction.getValue().intValue()).intValue()));
        }
    },
    /**
     * Store value indirect to memory
     */
    STIV("STIV") {
        @Override
        public void run(CompiledInstruction instruction) {
            if (!instruction.isReference() && (instruction.getValue().intValue() < 0)) {
                fail("illegal memory address", mima.getInstructionPointer() + 1);
            }
            mima.storeValue(mima.loadValue(instruction.getValue().intValue()).intValue(), mima.getAccumulator());
        }
    },
    /**
     * Rotate accumulator right by one bit
     */
    RAR("rar") {
        @Override
        public void run(CompiledInstruction instruction) {
            if (instruction.holdsValue()) {
                fail("unexpected argument", mima.getInstructionPointer() + 1);
            }
            mima.setAccumulator(arithmeticLogicUnit.rar(mima.getAccumulator()));
        }
    },
    /**
     * Invert bits in accumulator
     */
    NOT("NOT") {
        @Override
        public void run(CompiledInstruction instruction) {
            if (instruction.holdsValue()) {
                fail("unexpected argument", mima.getInstructionPointer() + 1);
            }
            mima.setAccumulator(mima.getAccumulator().invert());
        }
    },
    /**
     * Add with accumulator
     */
    ADD("ADD") {
        @Override
        public void run(CompiledInstruction instruction) {
            if (!instruction.isReference() && (instruction.getValue().intValue() < 0)) {
                fail("illegal memory address", mima.getInstructionPointer() + 1);
            }
            mima.setAccumulator(arithmeticLogicUnit.add(MachineWord.cast(mima.getAccumulator(), mima.getWordLength()),
                    mima.loadValue(instruction.getValue().intValue())));
        }
    },
    /**
     * And bitwise with accumulator
     */
    AND("AND") {
        @Override
        public void run(CompiledInstruction instruction) {
            if (!instruction.isReference() && (instruction.getValue().intValue() < 0)) {
                fail("illegal memory address", mima.getInstructionPointer() + 1);
            }
            mima.setAccumulator(arithmeticLogicUnit.and(MachineWord.cast(mima.getAccumulator(), mima.getWordLength()),
                    mima.loadValue(instruction.getValue().intValue())));
        }
    },
    /**
     * or bitwise with accumulator
     */
    OR("OR") {
        @Override
        public void run(CompiledInstruction instruction) {
            if (!instruction.isReference() && (instruction.getValue().intValue() < 0)) {
                fail("illegal memory address", mima.getInstructionPointer() + 1);
            }
            mima.setAccumulator(arithmeticLogicUnit.or(MachineWord.cast(mima.getAccumulator(), mima.getWordLength()),
                    mima.loadValue(instruction.getValue().intValue())));
        }
    },
    /**
     * xor bitwise with accumulator
     */
    XOR("XOR") {
        @Override
        public void run(CompiledInstruction instruction) {
            if (!instruction.isReference() && (instruction.getValue().intValue() < 0)) {
                fail("illegal memory address", mima.getInstructionPointer() + 1);
            }
            mima.setAccumulator(arithmeticLogicUnit.xor(MachineWord.cast(mima.getAccumulator(), mima.getWordLength()),
                    mima.loadValue(instruction.getValue().intValue())));
        }
    },
    /**
     * Store -1 to accumulator if equals, else load 0
     */
    EQL("EQL") {
        @Override
        public void run(CompiledInstruction instruction) {
            if (!instruction.isReference() && (instruction.getValue().intValue() < 0)) {
                fail("illegal memory address", mima.getInstructionPointer() + 1);
            }
            mima.setAccumulator(arithmeticLogicUnit.equals(
                    MachineWord.cast(mima.getAccumulator(), mima.getWordLength()),
                    mima.loadValue(instruction.getValue().intValue())));
        }
    },
    /**
     * STOP the mima
     */
    HALT("HALT") {
        @Override
        public void run(CompiledInstruction instruction) {
            if (instruction.holdsValue()) {
                fail("unexpected argument", mima.getInstructionPointer() + 1);
            }
            mima.stop();
        }
    },
    /**
     * Jump to instruction address
     */
    JMP("JMP") {
        @Override
        public void run(CompiledInstruction instruction) {
            mima.setInstructionPointer(instruction.getValue().intValue() - 1);
        }
    },
    /**
     * Jump if negative
     */
    JMN("JMN") {
        @Override
        public void run(CompiledInstruction instruction) {
            if (mima.getAccumulator().msb() == 1) {
                mima.setInstructionPointer(instruction.getValue().intValue() - 1);
            }
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
    MimaInstructions(String instruction) {
        this.instruction = instruction;
    }

    /**
     * Set the mima controlled by the instructions
     *
     * @param mima Mima
     */
    public static void setMima(Mima mima) {
        MimaInstructions.mima = mima;
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
