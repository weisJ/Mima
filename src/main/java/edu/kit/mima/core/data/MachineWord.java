package edu.kit.mima.core.data;


import edu.kit.mima.core.Mima;

import java.util.stream.IntStream;

/**
 * MachineWord used in {@link Mima}
 *
 * @author Jannis Weis
 * @since 2018
 */
public class MachineWord {

    private final int wordLength;
    private boolean[] bits;

    /**
     * Create new MachineWord with the given number of bits
     * bits.length must be equal to wordLength
     *
     * @param bits       bits
     * @param wordLength number of bits
     */
    public MachineWord(final boolean[] bits, final int wordLength) {
        if (wordLength > bits.length) {
            throw new IllegalArgumentException("too many bits");
        }
        this.wordLength = wordLength;
        this.bits = new boolean[wordLength];
        System.arraycopy(bits, 0, this.bits, 0, bits.length);
    }

    /**
     * Create new MachineWord with the given number of bits
     * bits.length must be equal to wordLength
     *
     * @param bits       bits
     * @param wordLength number of bits
     */
    public MachineWord(final Boolean[] bits, final int wordLength) {
        if (wordLength > bits.length) {
            throw new IllegalArgumentException("too many bits");
        }
        this.wordLength = wordLength;
        this.bits = new boolean[wordLength];
        for (int i = 0; i < bits.length; i++) {
            this.bits[i] = bits[i];
        }
    }

    /**
     * Create new MachineWord with the given number of bits
     *
     * @param value      value as integer
     * @param wordLength number of bits
     */
    public MachineWord(final int value, final int wordLength) {
        this.wordLength = wordLength;
        setValue(value);
    }

    /**
     * Cast the value to the given number of bits
     *
     * @param word       machineWord to cast
     * @param wordLength length to cast to
     * @return machineWord with wordLength bits
     */
    public static MachineWord cast(final MachineWord word, final int wordLength) {
        return new MachineWord(word.intValue(), wordLength);
    }

    @Override
    public String toString() {
        return String.valueOf(intValue());
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public MachineWord clone() {
        return new MachineWord(bits, wordLength);
    }

    /**
     * Get the number of bits
     *
     * @return number of bits
     */
    public int getWordLength() {
        return wordLength;
    }

    /**
     * Get the bits
     *
     * @return bits array
     */
    public boolean[] getBits() {
        return bits;
    }

    /**
     * Set the bits
     *
     * @param bits bits array
     */
    public void setBits(final boolean[] bits) {
        this.bits = bits;
    }

    /**
     * Invert all bits
     *
     * @return this
     */
    public final MachineWord invert() {
        for (int i = 0; i < wordLength; i++) {
            bits[i] = !bits[i];
        }
        return this;
    }

    /**
     * Get the most significant bit value
     *
     * @return 1 if msb is set else 0
     */
    public int msb() {
        return bits[wordLength - 1] ? 1 : 0;
    }

    /**
     * Get the value when interpreted as an integer
     *
     * @return Integer value
     */
    public int intValue() {
        int value = 0;
        final boolean isNegative = msb() == 1;
        if (isNegative) {
            value++;
        }
        value += IntStream.range(0, (wordLength - 2)).filter(i -> isNegative ^ bits[i]).map(i -> (int) Math.pow(2, i))
                .sum();
        if (isNegative) {
            value *= -1;
        }
        return value;
    }

    /**
     * Set the value when interpreted as integer
     *
     * @param value value
     */
    public void setValue(final int value) {
        assert (!(value > (Math.pow(2, wordLength - 1) - 1))) && (!(value < (-1 * Math
                .pow(2, wordLength - 1)))) : "value must be > 0 for unsigned data";
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
