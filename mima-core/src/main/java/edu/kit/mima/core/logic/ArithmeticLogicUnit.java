package edu.kit.mima.core.logic;

import edu.kit.mima.core.Mima;
import edu.kit.mima.core.data.MachineWord;
import org.jetbrains.annotations.NotNull;

import java.util.function.BinaryOperator;

/**
 * The ALU in {@link Mima}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class ArithmeticLogicUnit {

    private final int machineWordLength;

    /**
     * Create new arithmetic logic unit that uses the given number of bits for calculations.
     *
     * @param machineWordLength number of bits in one machineWord
     */
    public ArithmeticLogicUnit(final int machineWordLength) {
        this.machineWordLength = machineWordLength;
    }

    /**
     * Add two values. Is commutative.
     *
     * @param x first value
     * @param y second value
     * @return x + y
     */
    @NotNull
    public MachineWord add(@NotNull final MachineWord x, @NotNull final MachineWord y) {
        checkWord(x, y);
        final boolean[] bits = new boolean[machineWordLength];
        final boolean[] bitsX = x.getBits();
        final boolean[] bitsY = y.getBits();
        boolean carry = false;
        for (int i = 0; i < machineWordLength; i++) {
            if (bitsX[i] && bitsY[i]) {
                bits[i] = carry;
                carry = true;
            } else if (bitsX[i] || bitsY[i]) {
                bits[i] = !carry;
            } else {
                bits[i] = carry;
                carry = false;
            }
        }
        return new MachineWord(bits, machineWordLength);
    }

    /**
     * Perform xor on two values. Is commutative.
     *
     * @param x first value
     * @param y second value
     * @return x xor y
     */
    @NotNull
    public MachineWord xor(@NotNull final MachineWord x, @NotNull final MachineWord y) {
        checkWord(x, y);
        return new MachineWord(bitwise(x, y, (v, w) -> v ^ w), machineWordLength);
    }

    /**
     * Perform and on two values. Is commutative.
     *
     * @param x first value
     * @param y second value
     * @return x & y
     */
    @NotNull
    public MachineWord and(@NotNull final MachineWord x, @NotNull final MachineWord y) {
        checkWord(x, y);
        return new MachineWord(bitwise(x, y, (v, w) -> v & w), machineWordLength);
    }

    /**
     * Perform or on two values. Is commutative.
     *
     * @param x first value
     * @param y second value
     * @return x | y
     */
    @NotNull
    public MachineWord or(@NotNull final MachineWord x, @NotNull final MachineWord y) {
        checkWord(x, y);
        return new MachineWord(bitwise(x, y, (v, w) -> v | w), machineWordLength);
    }

    /**
     * Checks of two values are equal. Is symmetric.
     *
     * @param x first value
     * @param y second value
     * @return -1 if x == y else 0
     */
    @NotNull
    public MachineWord negativeIfEquals(@NotNull final MachineWord x,
                                        @NotNull final MachineWord y) {
        checkWord(x, y);
        final MachineWord comp = new MachineWord(bitwise(x, y, (v, w) -> v ? w : !w),
                                                 machineWordLength);
        return comp.intValue() == -1 ? comp : new MachineWord(0, machineWordLength);
    }

    /**
     * Rotate the bits in the value one place to the right.
     *
     * @param a value
     * @return rotate value
     */
    @NotNull
    public MachineWord rar(@NotNull final MachineWord a) {
        checkWord(a);
        final boolean[] bits = a.getBits();
        final boolean tmp = bits[0];
        System.arraycopy(bits, 1, bits, 0, bits.length - 1);
        bits[bits.length - 1] = tmp;
        return new MachineWord(bits, machineWordLength);
    }

    /*
     * Perform an bitwise operator on two machine words
     */
    @NotNull
    private boolean[] bitwise(@NotNull final MachineWord x,
                              @NotNull final MachineWord y,
                              @NotNull final BinaryOperator<Boolean> operator) {
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
    private void checkWord(@NotNull final MachineWord... var) {
        for (final MachineWord w : var) {
            if (w.getWordLength() != machineWordLength) {
                throw new IllegalArgumentException("machine words must match mima word length");
            }
        }
    }
}
