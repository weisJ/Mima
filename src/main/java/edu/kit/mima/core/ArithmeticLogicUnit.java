package edu.kit.mima.core;

import edu.kit.mima.core.data.MachineWord;

import java.util.Objects;
import java.util.function.BinaryOperator;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class ArithmeticLogicUnit {

    private final int machineWordLength;

    public ArithmeticLogicUnit(final int machineWordLength) {
        super();
        this.machineWordLength = machineWordLength;
    }

    public MachineWord add(final MachineWord x, final MachineWord y) {
        checkWord(x, y);
        return new MachineWord(x.intValue() + y.intValue(), machineWordLength);
    }

    public MachineWord xor(final MachineWord x, final MachineWord y) {
        checkWord(x, y);
        return new MachineWord(bitwise(x, y, (v, w) -> v ^ w), machineWordLength);
    }

    public MachineWord and(final MachineWord x, final MachineWord y) {
        checkWord(x, y);
        return new MachineWord(bitwise(x, y, (v, w) -> v & w),machineWordLength);
    }

    public MachineWord or(final MachineWord x, final MachineWord y) {
        checkWord(x, y);
        return new MachineWord(bitwise(x, y, (v, w) -> v | w),machineWordLength);
    }

    public MachineWord equals(final MachineWord x, final MachineWord y) {
        checkWord(x, y);
        final MachineWord comp = new MachineWord(bitwise(x, y, Objects::equals), machineWordLength);
        return comp.intValue() == 0 ? new MachineWord(-1, machineWordLength)
                                    : new MachineWord(0, machineWordLength);
    }

    public MachineWord rar(final MachineWord a) {
        checkWord(a);
        final boolean[] bits = a.getBits();
        final boolean tmp = bits[0];
        System.arraycopy(bits, 1, bits, 2, machineWordLength - 1);
        bits[machineWordLength - 1] = tmp;
        return new MachineWord(bits, machineWordLength);
    }

    private boolean[] bitwise(final MachineWord x, final MachineWord y, final BinaryOperator<Boolean> operator) {
        final boolean[] bits = new boolean[machineWordLength];
        final boolean[] aBits = x.getBits();
        final boolean[] bBits = y.getBits();
        for (int i = 0; i < machineWordLength; i++) {
            bits[i] = operator.apply(aBits[i], bBits[i]);
        }
        return bits;
    }

    private void checkWord(final MachineWord... var) {
        for (final MachineWord w : var) {
            assert w.getWordLength() == machineWordLength : "machine words must match mima word length";
        }
    }
}
