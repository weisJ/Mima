package edu.kit.mima.core.data;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class MachineWord {

    private final int wordLength;
    private boolean[] bits;
    private boolean unsigned;

    public MachineWord(final int value, final int wordLength) {
        this(value, wordLength, false);
    }

    public MachineWord(final boolean[] bits, final boolean unsigned) {
        super();
        this.unsigned = unsigned;
        this.wordLength = bits.length;
        this.bits = bits;
    }

    public MachineWord(final int value, final int wordLength, final boolean unsigned) {
        super();
        this.wordLength = wordLength;
        setValue(value);
    }

    public MachineWord(final boolean[] bits) {
        this(bits, false);
    }

    public static MachineWord cast(final MachineWord word, final int wordLength) {
        try {
            return new MachineWord(word.intValue(), wordLength, word.unsigned);
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException("not enough space to cast");
        }
    }

    public String toString() {
        return String.valueOf(intValue());
    }

    public int getWordLength() {
        return wordLength;
    }

    public boolean isUnsigned() {
        return unsigned;
    }

    public int intValue() {
        int value = 0;
        final boolean isNegative = MSB() == 1;
        if (isNegative) value++;
        for (int i = 0; i < (wordLength - 2); i++) {
            if (isNegative ^ bits[i]) value += Math.pow(2, i);
        }
        if (isNegative) value *= -1;
        return value;
    }

    public boolean[] getBits() {
        return bits.clone();
    }

    public void setBits(final boolean[] bits) {
        this.bits = bits;
    }

    public final void invert() {
        for (int i = 0; i < wordLength; i++) {
            bits[i] = !bits[i];
        }
    }

    public MachineWord copy() {
        return new MachineWord(bits);
    }

    public int MSB() {
        return bits[wordLength - 1] ? 1 : 0;
    }

    public void setValue(final int value) {
        if (unsigned) {
            if (value > (Math.pow(2, wordLength) - 1)) {
                throw new IllegalArgumentException("invalid value");
            }
        } else {
            if ((value > (Math.pow(2, wordLength - 1) - 1)) || (value < (-1 * Math.pow(2, wordLength - 1)))) {
                throw new IllegalArgumentException("value must be > 0 for unsigned data");
            }
        }
        final int digits = unsigned ? (wordLength + 1) : wordLength;
        this.bits = new boolean[wordLength];
        final boolean isNegative = value < 0;
        int val = Math.abs(value);
        if (isNegative) val--;
        final String bitValue = new StringBuilder(Integer.toBinaryString(val)).reverse().toString();
        for (int i = 0; i < Math.min(digits, bitValue.length()); i++) {
            bits[i] = bitValue.charAt(i) == '1';
        }
        if (isNegative) invert();
    }
}
