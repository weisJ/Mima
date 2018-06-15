package edu.kit.mima.core;

import edu.kit.mima.core.data.MachineWord;

import java.util.Objects;
import java.util.function.BinaryOperator;

/**
 * The ALU in {@link Mima}
 *
 * @author Jannis Weis
 * @since 2018
 */
public class ArithmeticLogicUnit {

    private final int machineWordLength;

    /**
     * create new arithmetic logic unit that uses the given number of bits for
     * calculations
     *
     * @param machineWordLength number of bits in one machineWord
     */
    public ArithmeticLogicUnit(final int machineWordLength) {
        this.machineWordLength = machineWordLength;
    }

    /**
     * Add two values. Is commutative
     *
     * @param x first value
     * @param y second value
     * @return x + y
     */
    public MachineWord add(final MachineWord x, final MachineWord y) {
        checkWord(x, y);
        return new MachineWord(x.intValue() + y.intValue(), machineWordLength);
    }

    /**
     * Perform xor on two values. Is commutative
     *
     * @param x first value
     * @param y second value
     * @return x xor y
     */
    public MachineWord xor(final MachineWord x, final MachineWord y) {
        checkWord(x, y);
        return new MachineWord(bitwise(x, y, (v, w) -> v ^ w), machineWordLength);
    }

    /**
     * Perform and on two values. Is commutative
     *
     * @param x first value
     * @param y second value
     * @return x & y
     */
    public MachineWord and(final MachineWord x, final MachineWord y) {
        checkWord(x, y);
        return new MachineWord(bitwise(x, y, (v, w) -> v & w), machineWordLength);
    }

    /**
     * Perform or on two values. Is commutative
     *
     * @param x first value
     * @param y second value
     * @return x | y
     */
    public MachineWord or(final MachineWord x, final MachineWord y) {
        checkWord(x, y);
        return new MachineWord(bitwise(x, y, (v, w) -> v | w), machineWordLength);
    }

    /**
     * Checks of two values are equal. Is symmetric
     *
     * @param x first value
     * @param y second value
     * @return -1 if x == y else 0
     */
    public MachineWord equals(final MachineWord x, final MachineWord y) {
        checkWord(x, y);
        final MachineWord comp = new MachineWord(bitwise(x, y, Objects::equals), machineWordLength);
        return comp.intValue() == 0 ? new MachineWord(-1, machineWordLength)
                : new MachineWord(0, machineWordLength);
    }

    /**
     * Rotate the bits in the value one place to the right
     *
     * @param a value
     * @return rotate value
     */
    public MachineWord rar(final MachineWord a) {
        checkWord(a);
        final boolean[] bits = a.getBits();
        final boolean tmp = bits[0];
        System.arraycopy(bits, 1, bits, 2, machineWordLength - 1);
        bits[machineWordLength - 1] = tmp;
        return new MachineWord(bits, machineWordLength);
    }

    /*
     * Perform an bitwise operator on two machine words
     */
    private boolean[] bitwise(final MachineWord x, final MachineWord y, final BinaryOperator<Boolean> operator) {
        final boolean[] bits = new boolean[machineWordLength];
        final boolean[] aBits = x.getBits();
        final boolean[] bBits = y.getBits();
        for (int i = 0; i < machineWordLength; i++) {
            bits[i] = operator.apply(aBits[i], bBits[i]);
        }
        return bits;
    }

    /*
     * Check if the arguments have the necessary number of bits
     */
    private void checkWord(final MachineWord... var) {
        for (final MachineWord w : var) {
            assert w.getWordLength() == machineWordLength : "machine words must match mima word length";
        }
    }
}
