package edu.kit.mima.core;

import java.util.function.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ArithmeticLogicUnit {

    private final int machineWordLength;

    public ArithmeticLogicUnit(final int machineWordLength) {
        this.machineWordLength = machineWordLength;
    }

    public MachineWord ADD(final MachineWord a, final MachineWord b) {
        checkWord(a, b);
        return new MachineWord(a.intValue() + b.intValue(), machineWordLength);
    }

    public MachineWord XOR(final MachineWord a, final MachineWord b) {
        checkWord(a, b);
        return new MachineWord(bitwise(a, b, (v, w) -> v ^ w));
    }

    public MachineWord AND(final MachineWord a, final MachineWord b) {
        checkWord(a, b);
        return new MachineWord(bitwise(a, b, (v, w) -> v & w));
    }

    public MachineWord OR(final MachineWord a, final MachineWord b) {
        checkWord(a, b);
        return new MachineWord(bitwise(a, b, (v, w) -> v | w));
    }

    public MachineWord EQL(final MachineWord a, final MachineWord b) {
        checkWord(a, b);
        MachineWord comp = new MachineWord(bitwise(a, b, (v, w) -> v == w));
        if (comp.intValue() == 0) {
            return new MachineWord(-1, machineWordLength);
        } else {
            return new MachineWord(0, machineWordLength);
        }
    }

    public MachineWord RAR(final MachineWord a) {
        checkWord(a);
        boolean[] bits = a.getBits();
        boolean tmp = bits[0];
        System.arraycopy(bits, 1, bits, 2, machineWordLength - 1);
        bits[machineWordLength - 1] = tmp;
        return new MachineWord(bits);
    }

    private boolean[] bitwise(final MachineWord a, final MachineWord b, BinaryOperator<Boolean> operator) {
        boolean[] bits = new boolean[machineWordLength];
        boolean[] aBits = a.getBits();
        boolean[] bBits = b.getBits();
        for (int i = 0; i < machineWordLength; i++) {
            bits[i] = operator.apply(aBits[i], bBits[i]);
        }
        return bits;
    }

    private void checkWord(final MachineWord... var) {
        for (MachineWord w : var) {
            if (w.getWordLength() != machineWordLength) {
                throw new IllegalArgumentException("machine words must match mima word length");
            }
        }
    }
}
