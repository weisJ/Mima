package edu.kit.mima.core.data;

import edu.kit.mima.core.Mima;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * MachineWord used in {@link Mima}. Stores integer value with given number of bits.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class MachineWord {

    private final int wordLength;
    /**
     * Bits with msb at index length - 1 and lsb at index 0.
     */
    private boolean[] bits;

    /**
     * Create new MachineWord with the given number of bits bits.length must be equal to wordLength
     *
     * @param bits       bits
     * @param wordLength number of bits
     */
    public MachineWord(@NotNull final boolean[] bits, final int wordLength) {
        assert wordLength > 0;
        if (wordLength > bits.length) {
            throw new IllegalArgumentException("too many bits");
        }
        this.wordLength = wordLength;
        this.bits = new boolean[wordLength];
        System.arraycopy(bits, 0, this.bits, 0, bits.length);
    }

    /**
     * Create new MachineWord with the given number of bits bits.length must be equal to wordLength
     *
     * @param bits       bits
     * @param wordLength number of bits
     */
    public MachineWord(@NotNull final Boolean[] bits, final int wordLength) {
        if (wordLength < bits.length) {
            throw new IllegalArgumentException("too many bits");
        }
        this.wordLength = wordLength;
        this.bits = new boolean[wordLength];
        for (int i = 0; i < bits.length; i++) {
            this.bits[i] = bits[i];
        }
    }

    /**
     * Create new MachineWord with the given number of bits.
     *
     * @param value      value as integer
     * @param wordLength number of bits
     */
    public MachineWord(final int value, final int wordLength) {
        this.wordLength = wordLength;
        setValue(value);
    }

    /**
     * Cast the value to the given number of bits.
     *
     * @param word       machineWord to cast
     * @param wordLength length to cast to
     * @return machineWord with wordLength bits
     */
    @NotNull
    @Contract("_, _ -> new")
    public static MachineWord cast(@NotNull final MachineWord word, final int wordLength) {
        return new MachineWord(word.intValue(), wordLength);
    }

    /**
     * Get the binary representation of this machine value.
     *
     * @return binary representation beginning with the msb on the left
     */
    @NotNull
    public String binaryRepresentation() {
        final StringBuilder sb = new StringBuilder();
        for (final boolean bit : bits) {
            sb.append(bit ? 1 : 0);
        }
        return sb.reverse().toString();
    }

    @NotNull
    @Override
    public String toString() {
        return String.valueOf(intValue());
    }

    @NotNull
    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public MachineWord clone() {
        return new MachineWord(bits, wordLength);
    }

    /**
     * Get the number of bits.
     *
     * @return number of bits
     */
    public int getWordLength() {
        return wordLength;
    }

    /**
     * Get the bits.
     *
     * @return bits array
     */
    public boolean[] getBits() {
        return bits;
    }

    /**
     * Set the bits.
     *
     * @param bits bits array
     */
    public void setBits(final boolean[] bits) {
        this.bits = bits;
    }

    /**
     * Invert all bits.
     *
     * @return this
     */
    @Contract(" -> this")
    @NotNull
    public final MachineWord invert() {
        for (int i = 0; i < wordLength; i++) {
            bits[i] = !bits[i];
        }
        return this;
    }

    /**
     * Get the most significant bit value.
     *
     * @return 1 if msb is set else 0
     */
    public int msb() {
        return bits[wordLength - 1] ? 1 : 0;
    }

    /**
     * Get the least significant bit value.
     *
     * @return 1 if lsb is set else 0.
     */
    public int lsb() {
        return bits[0] ? 1 : 0;
    }

    /**
     * Get the value when interpreted as an integer.
     *
     * @return Integer value
     */
    public int intValue() {
        int value = 0;
        final boolean isNegative = msb() == 1;
        if (isNegative) {
            value++;
        }
        for (int i = 0; i < bits.length - 1; i++) {
            if (bits[i] ^ isNegative) {
                value += Math.pow(2, i);
            }
        }
        if (isNegative) {
            value *= -1;
        }
        return value;
    }

    /**
     * Set the value when interpreted as integer.
     *
     * @param value value
     */
    public void setValue(final int value) {
        assert (!(value > (Math.pow(2, wordLength - 1) - 1)))
               && (!(value < (-1 * Math.pow(2, wordLength - 1))))
                : "value must be > 0 for unsigned data";
        bits = new boolean[wordLength];
        final boolean isNegative = value < 0;
        int val = Math.abs(value);
        if (isNegative) {
            val--;
        }
        final String bitValue = new StringBuilder(Integer.toBinaryString(val)).reverse().toString();
        for (int i = 0; i < Math.min(wordLength, bitValue.length()); i++) {
            bits[i] = bitValue.charAt(i) == '1';
        }
        if (isNegative) {
            invert();
        }
    }
}
