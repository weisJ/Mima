package edu.kit.mima.core;

import java.util.*;
import java.util.regex.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Interpreter {

    private static final String POINTER = "\\([^()]*\\)";
    private static final Pattern COMMAND_PATTERN = Pattern.compile("^(?: )*([a-zA-Z]*(?:"
                                                                           + POINTER + ")?)(?: )*([^ :§]*)?(?: )*$");
    private static final Pattern COMMAND_LOOKUP = Pattern
            .compile("^(?: )*([^ :§]+)(?: )*:(?: )*([^ :§*]*(?: )*[^ :§*]*)(?: )*$");
    private static final Pattern COMMAND_INTERN = Pattern.compile("^([a-zA-Z]*(?:" + POINTER + ")?)§(&?-?[0-9]+)?$");

    private static final String DEFINITION = "§define";
    private static final Pattern DEF_PATTERN = Pattern.compile("^(?:§define)([^ :$]+)(?:|:([0-9]+))$");

    private static final String BINARY_PREFIX = "0b";
    private static final String REFERENCE_PREFIX = "&";
    private final int constWordLength;
    private final int wordLength;
    private final String[] program;
    private Map<String, Integer> memoryLookupTable;
    private Map<String, Integer> commandLookupTable;
    private int instructionPointer;
    private int firstInstruction;


    public Interpreter(final String[] lines, final int constWordLength, final int wordLength) {
        this.program = lines;
        this.instructionPointer = 0;
        this.constWordLength = constWordLength;
        this.wordLength = wordLength;
    }

    public static String[] getKeywords() {
        return new String[]{"§define", ":", "\\(", "\\)", " [0-9]+ ?", "0b[01]*", "#[^\n]*\n?"};
    }

    public void compile() {
        this.memoryLookupTable = new HashMap<>();
        this.commandLookupTable = new HashMap<>();
        setInstructionPointer(0);
        removeComments();
        setupMemoryLookup();
        firstInstruction = instructionPointer;
        setupInstructionLookup();
        replaceMemoryAssociations();
    }

    private void removeComments() {
        for (int i = 0; i < program.length; i++) {
            if (program[i] != null && program[i].length() > 0) {
                if (program[i].charAt(0) == '#') {
                    program[i] = "";
                } else if (program[i].contains("#")) {
                    String line = program[i];
                    int index = line.indexOf('#');
                    int lastIndex = line.lastIndexOf('#');
                    if (index == lastIndex) {
                        program[i] = line.substring(0, index - 1);
                    }
                }
            }
        }
    }

    private void setupMemoryLookup() {
        int reservedIndex = -1; //Won't clash with allowed memory address
        while (program[instructionPointer].startsWith(DEFINITION)) {
            try {
                String line = program[instructionPointer].replace(" ", "");
                Matcher matcher = DEF_PATTERN.matcher(line);
                if (!matcher.matches()) {
                    throw new IllegalArgumentException("illegal definition at line :" + (instructionPointer + 1));
                }
                String reference = matcher.group(1);
                if (reference.isEmpty()) {
                    throw new IllegalArgumentException("missing identifier at line " + (instructionPointer + 1));
                }
                int value;
                if (matcher.group(2) == null || matcher.group(2).isEmpty()) {
                    value = reservedIndex;
                    reservedIndex--;
                } else {
                    try {
                        value = Integer.parseInt(matcher.group(2));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("reference must be an integer");
                    }
                }
                if (memoryLookupTable.containsKey(reference)) {
                    throw new IllegalArgumentException("reference <" + reference + "> already defined");
                }
                memoryLookupTable.put(reference, value);
                instructionPointer++;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("errror parsing reference at line " + (instructionPointer + 1));
            }
        }
    }

    private void setupInstructionLookup() {
        for (int i = instructionPointer; i < program.length; i++) {
            String line = program[i];
            if (line.contains(":")) {
                line = addCommandEntry(line, i);
                program[i] = line;
            }
        }
    }

    private void replaceMemoryAssociations() {
        for (int i = instructionPointer; i < program.length; i++) {
            String line = program[i];
            Pattern brackets = Pattern.compile(POINTER);
            Matcher bracketMatcher = brackets.matcher(line);
            String pointerBracket = "";
            if (bracketMatcher.find()) {
                pointerBracket = bracketMatcher.group();
                line = line.replaceAll(POINTER, "");
            }

            Matcher matcher = COMMAND_PATTERN.matcher(line);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("invalid instruction at line : " + (i + 1));
            }
            String command = matcher.group(1) != null ? matcher.group(1) : "";
            String value = matcher.group(2) != null ? matcher.group(2) : "";
            if (value.length() > 0) {
                if (memoryLookupTable.containsKey(value)) {
                    value = REFERENCE_PREFIX + String.valueOf(memoryLookupTable.get(value));
                } else if (commandLookupTable.containsKey(value)) {
                    value = REFERENCE_PREFIX + String.valueOf(commandLookupTable.get(value));
                } else if (value.startsWith(BINARY_PREFIX)) {
                    value = String.valueOf(parseBinary(value.substring(BINARY_PREFIX.length())));
                } else if (!value.matches("[0-9]*")) {
                    throw new IllegalArgumentException("unresolved Symbol <" + value + "> at line " + (i + 1));
                } else {
                    try {
                        int val = Integer.parseInt(value);
                        value = String.valueOf(val);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("illegal memory address <" + value + "> at line " + (i + 1));
                    }
                }
            }
            program[i] = command + pointerBracket + "§" + value;
        }
    }

    public Command nextInstruction() throws IllegalArgumentException {
        if (!hasInstructions()) {
            throw new IllegalStateException("No more instructions");
        }
        String line = program[instructionPointer];
        instructionPointer++;
        Command command = parseCommand(line);

        if (command == null) { //Ignore empty lines
            return nextInstruction();
        } else {
            return command;
        }
    }

    private Command parseCommand(final String l) {
        if (l.equals("§")) {
            return null;
        }
        Matcher matcher = COMMAND_INTERN.matcher(l);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("invalid instruction at line " + instructionPointer);
        }
        //No argument command
        if (matcher.group(2) == null || matcher.group(2).isEmpty()) {
            return new Command(matcher.group(1), null, false);
        }

        //Argument command
        String sVal = matcher.group(2); //Argument value
        boolean isReference = sVal.startsWith(REFERENCE_PREFIX);
        if (isReference) {
            sVal = sVal.substring(REFERENCE_PREFIX.length());
        }
        int value;
        try {
            value = Integer.parseInt(sVal);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("parameter in line " + (instructionPointer + 1)
                                                       + " is neither an integer value nor a memory reference");
        }
        return new Command(matcher.group(1), new MachineWord(value, wordLength), isReference);
    }

    private String addCommandEntry(final String line, final int lineNumber) {
        Matcher matcher = COMMAND_LOOKUP.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("not a correct jump association at line : " + (lineNumber + 1));
        }
        String reference = matcher.group(1);
        if (memoryLookupTable.containsKey(reference)) {
            throw new IllegalArgumentException(reference + " is already associated with an instruction");
        }
        commandLookupTable.put(reference, lineNumber + 1);
        return matcher.group(2);
    }

    private int parseBinary(final String sVal) {
        Pattern crop = Pattern.compile("0*([01]*)");
        Matcher matcher = crop.matcher(sVal);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("not a binary number <" + sVal + ">");
        }
        String digits = new StringBuilder(matcher.group(1)).reverse().toString(); //reverse and crop leading 0
        if (digits.length() > wordLength) {
            throw new IllegalArgumentException("binary number <" + sVal + "> is too large for " + wordLength + "bits");
        }
        boolean[] bits;
        if (digits.length() > constWordLength) {
            bits = new boolean[wordLength];
        } else {
            bits = new boolean[constWordLength];
        }
        for (int i = 0; i < digits.length(); i++) {
            bits[i] = digits.charAt(i) == '1';
        }
        return new MachineWord(bits).intValue();
    }

    public int getInstructionPointer() {
        return instructionPointer;
    }

    public void setInstructionPointer(final int pointer) {
        if (pointer < 0 || pointer >= program.length) {
            throw new IllegalArgumentException("invalid pointer position");
        }
        instructionPointer = pointer;
    }

    public Map<String, Integer> getMemoryLookupTable() {
        return memoryLookupTable;
    }

    public boolean hasInstructions() {
        return instructionPointer < program.length;
    }

    public void reset() {
        setInstructionPointer(firstInstruction);
    }
}