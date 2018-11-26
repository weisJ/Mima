package edu.kit.mima.core.instruction;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jannis Weis
 * @since 2018
 */
public enum InstructionSet {
    /**
     * Mima instruction Set
     */
    MIMA(24, 20) {
        @Override
        public String[] getInstructions() {
            List<String> instructions = Arrays.stream(MimaInstruction.values())
                    .map(MimaInstruction::toString)
                    .collect(Collectors.toList());
            instructions.add("HALT");
            instructions.add("JMP");
            instructions.add("JMN");
            return instructions.toArray(new String[0]);
        }
    },
    /**
     * MimaX instruction set
     */
    MIMA_X(24, 24) {
        @Override
        public String[] getInstructions() {
            List<String> instructions = Arrays.stream(MimaXInstruction.values())
                    .map(MimaXInstruction::toString)
                    .collect(Collectors.toList());
            instructions.addAll(Arrays.stream(MimaInstruction.values())
                    .map(MimaInstruction::toString)
                    .collect(Collectors.toList()));
            instructions.add("ADC");
            instructions.add("JMP");
            instructions.add("JMN");
            instructions.add("HALT");
            instructions.add("CALL");
            instructions.add("RET");
            return instructions.toArray(new String[0]);
        }
    };

    private final int wordLength;
    private final int constCordLength;

    /**
     * Instruction set with given number of bits for machine words
     * and argument machine words
     *
     * @param wordLength      number of bits in machine word
     * @param constCordLength number of bits in argument machine word
     */
    InstructionSet(int wordLength, int constCordLength) {
        this.wordLength = wordLength;
        this.constCordLength = constCordLength;
    }

    /**
     * Get the wordLength
     *
     * @return number of bits in machine word
     */
    public int getWordLength() {
        return wordLength;
    }

    /**
     * Get the constant wordLength
     *
     * @return number of bits in argument machine word
     */
    public int getConstCordLength() {
        return constCordLength;
    }

    /**
     * Get the instruction identifiers as String array
     *
     * @return instruction identifiers
     */
    public abstract String[] getInstructions();
}
