package edu.kit.mima.core.data;

import java.util.stream.IntStream;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class MachineWord {

    private final int wordLength;
    private boolean[] bits;

    public MachineWord(final boolean[] bits) {
        wordLength = bits.length;
        this.bits = bits;
    }

    public MachineWord(final int value, final int wordLength) {
        this.wordLength = wordLength;
        setValue(value);
    }

    public static MachineWord cast(final MachineWord word, final int wordLength) {
        try {
            return new MachineWord(word.intValue(), wordLength);
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("not enough space to cast");
        }
    }

    @Override
    public String toString() {
        return String.valueOf(intValue());
    }

    public int getWordLength() {
        return wordLength;
    }

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

    public boolean[] getBits() {
        return bits.clone();
    }

    public void setBits(final boolean[] bits) {
        this.bits = bits;
    }

    public final MachineWord invert() {
        for (int i = 0; i < wordLength; i++) {
            bits[i] = !bits[i];
        }
        return this;
    }

    public MachineWord copy() {
        return new MachineWord(bits);
    }

    public int msb() {
        return bits[wordLength - 1] ? 1 : 0;
    }

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
