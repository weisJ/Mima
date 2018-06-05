package edu.kit.mima.core.parsing;

import edu.kit.mima.core.data.*;

import java.util.*;
import java.util.regex.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Interpreter {

    private static final String POINTER = "\\([^()]*\\)";
    private static final Pattern COMMAND_PATTERN = Pattern
            .compile("^(?: )*([a-zA-Z]*(?:" + POINTER + ")?)(?: )*([^ :§]*)?(?: )*$");
    private static final Pattern COMMAND_LOOKUP = Pattern
            .compile("^(?: )*([^ :§]+)(?: )*:(?: )*([^ :§*]*(?: )*[^ :§*]*)(?: )*$");
    private static final Pattern COMMAND_INTERN = Pattern
            .compile("^([a-zA-Z]*(?:" + POINTER + ")?)§(&?-?[0-9]+)?$");

    private static final String DEFINITION = "§define";
    private static final String CONST = "const";
    private static final Pattern DEF_PATTERN = Pattern
            .compile("^(?:" + DEFINITION + ")([^ :$]+)(?:|:([0-9]+))$");
    private static final Pattern DEF_CONST_PATTERN = Pattern
            .compile("^(?:" + DEFINITION + CONST + ")([^ :$]+)(?:|:(-?[0-9]+))$");

    private static final String BINARY_PREFIX = "0b";
    private static final String REFERENCE_PREFIX = "&";

    private final int constWordLength;
    private final int wordLength;
    private final String[] instructions;
    private int instructionPointer;
    private int firstInstruction;
    private int reservedIndex;
    private Map<String, Integer> memoryLookupTable;
    private Map<String, Integer> instructionLookupTable;
    private Map<String, Integer> constLookupTable;


    public Interpreter(final String[] lines, final int constWordLength, final int wordLength) {
        this.instructions = lines;
        this.instructionPointer = 0;
        this.constWordLength = constWordLength;
        this.wordLength = wordLength;
        this.reservedIndex = -1;
    }

    public static String[] getKeywords() {
        return new String[]{"(?<![^ \n])" + DEFINITION + "(?![^ \n])"
                , "(?<![^ \n])" + CONST + "(?![^ \n])"
                , "(?<![^ \n]):(?![^ \n])"
                , "\\(", "\\)"
                , "(?<![^ \n])[0-9]+(?![^ (\n])"
                , "0b[01]*"
                , "#[^\n]*\n?"};
    }

    public void compile() {
        this.memoryLookupTable = new HashMap<>();
        this.instructionLookupTable = new HashMap<>();
        this.constLookupTable = new HashMap<>();
        reservedIndex = -1;
        setInstructionPointer(0);
        int index = removeComments(instructions);
        firstInstruction = setupDefinitions(instructions, index, memoryLookupTable, constLookupTable);
        setupInstructionLookup(instructions, firstInstruction, instructionLookupTable);
        setInstructionPointer(firstInstruction);
        replaceAssociations();
        setInstructionPointer(firstInstruction);

    }

    public List<Set<String>> getReferences(String[] lines) throws IllegalArgumentException {
        Map<String, Integer> constTable = new HashMap<>();
        Map<String, Integer> memoryTable = new HashMap<>();
        Map<String, Integer> instructionTable = new HashMap<>();
        int index = removeComments(lines);
        index = setupDefinitions(lines, index, memoryTable, constTable);
        setupInstructionLookup(lines, index, instructionTable);
        return List.of(constTable.keySet(), memoryTable.keySet(), instructionTable.keySet());
    }

    private int removeComments(String[] lines) {
        if (lines.length == 0) return 0;
        boolean foundFirstNonComment = false;
        int firstNonComment = 0;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i] != null && lines[i].length() > 0) {
                if (lines[i].charAt(0) == '#') {
                    lines[i] = "";
                } else if (lines[i].contains("#")) {
                    String line = lines[i];
                    int index = line.indexOf('#');
                    int lastIndex = line.lastIndexOf('#');
                    if (index == lastIndex) {
                        lines[i] = line.substring(0, index - 1);
                    }
                } else if (!foundFirstNonComment) {
                    firstNonComment = i;
                    foundFirstNonComment = true;
                }
            }
        }
        return firstNonComment;
    }

    private int setupDefinitions(String[] lines, int startIndex, Map<String, Integer> memoryMap,
                                 Map<String, Integer> constMap) {
        if (lines.length == 0) return startIndex;
        int index = startIndex;
        while (lines[index].startsWith(DEFINITION)) {
            String line = lines[index].replace(" ", "");
            boolean parsed = setConstDefinition(line, constMap);
            if (!parsed) {
                parsed = setMemoryDefinition(line, memoryMap);
            }
            if (!parsed) {
                throw new InterpretationException("invalid definition", lines[index], index + 1);
            }
            index++;
        }
        return index;
    }

    private boolean setMemoryDefinition(String line, Map<String, Integer> lookupTable) {
        Matcher matcher = DEF_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return false;
        }
        if (parseDefinition(matcher, lookupTable) < 0) {
            throw new InterpretationException("negative memory address", line);
        }
        return true;
    }

    private boolean setConstDefinition(String line, Map<String, Integer> lookupTable) {
        Matcher matcher = DEF_CONST_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return false;
        }
        parseDefinition(matcher, lookupTable);
        return true;
    }

    private int parseDefinition(Matcher matcher, Map<String, Integer> table) {
        try {
            String reference = matcher.group(1);
            if (reference.isEmpty()) {
                throw new InterpretationException("missing identifier", instructionPointer + 1);
            }
            int value;
            if (matcher.group(2) == null || matcher.group(2).isEmpty()) {
                value = reservedIndex;
                reservedIndex--;
            } else {
                try {
                    value = Integer.parseInt(matcher.group(2));
                } catch (NumberFormatException e) {
                    throw new InterpretationException("reference must be an integer", instructionPointer + 1);
                }
            }
            if (table.containsKey(reference)) {
                throw new InterpretationException("reference <" + reference + "> already defined",
                                                  instructionPointer + 1);
            }
            table.put(reference, value);
            return value;
        } catch (NumberFormatException e) {
            throw new InterpretationException("error parsing reference", instructionPointer + 1);
        }
    }

    private void setupInstructionLookup(String[] lines, int startIndex, Map<String, Integer> instructionMap) {
        if (lines.length == 0) return;
        for (int i = startIndex; i < lines.length; i++) {
            String line = lines[i];
            if (line.contains(":")) {
                line = addCommandEntry(line, i, instructionMap);
                lines[i] = line;
            }
        }
    }

    private String addCommandEntry(final String line, final int lineNumber, Map<String, Integer> lookupTable) {
        Matcher matcher = COMMAND_LOOKUP.matcher(line);
        if (!matcher.matches()) {
            throw new InterpretationException("incorrect jump association", lineNumber + 1);
        }
        String reference = matcher.group(1);
        if (lookupTable.containsKey(reference)) {
            throw new InterpretationException(reference + " is already associated with an instruction", lineNumber + 1);
        }
        lookupTable.put(reference, lineNumber + 1);
        return matcher.group(2);
    }

    private void replaceAssociations() {
        for (int i = instructionPointer; i < instructions.length; i++) {
            String line = instructions[i];

            //Bracket (...) operator
            Pattern brackets = Pattern.compile(POINTER);
            Matcher bracketMatcher = brackets.matcher(line);
            String pointerBracket = "";
            if (bracketMatcher.find()) {
                pointerBracket = bracketMatcher.group();
                line = line.replaceAll(POINTER, "");
            }

            Matcher matcher = COMMAND_PATTERN.matcher(line);
            if (!matcher.matches()) {
                throw new InterpretationException("invalid instruction", i + 1);
            }
            String command = matcher.group(1) != null ? matcher.group(1) : "";
            String value = matcher.group(2) != null ? matcher.group(2) : "";
            if (value.length() > 0) {
                if (memoryLookupTable.containsKey(value)) {
                    value = REFERENCE_PREFIX + String.valueOf(memoryLookupTable.get(value));
                } else if (instructionLookupTable.containsKey(value)) {
                    value = REFERENCE_PREFIX + String.valueOf(instructionLookupTable.get(value));
                } else if (constLookupTable.containsKey(value)) {
                    value = String.valueOf(constLookupTable.get(value));
                } else if (value.startsWith(BINARY_PREFIX)) {
                    value = String.valueOf(parseBinary(value.substring(BINARY_PREFIX.length())));
                } else if (!value.matches("[0-9]*")) {
                    throw new InterpretationException("unresolved Symbol", value, i + 1);
                } else {
                    try {
                        int val = Integer.parseInt(value);
                        value = String.valueOf(val);
                    } catch (NumberFormatException e) {
                        throw new InterpretationException("illegal memory address", value, i + 1);
                    }
                }
            }
            instructions[i] = command + pointerBracket + "§" + value;
        }
    }

    public Command nextInstruction() throws IllegalArgumentException {
        if (!hasInstructions()) {
            throw new IllegalStateException("No more instructions");
        }
        String line = instructions[instructionPointer];
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
            throw new InterpretationException("invalid instruction", instructionPointer);
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
            throw new InterpretationException("parameter is neither an integer value nor a memory reference",
                                              instructionPointer + 1);
        }
        return new Command(matcher.group(1), new MachineWord(value, wordLength), isReference);
    }

    private int parseBinary(final String sVal) {
        Pattern crop = Pattern.compile("0*([01]*)");
        Matcher matcher = crop.matcher(sVal);
        if (!matcher.matches()) {
            throw new InterpretationException("not a binary number", sVal);
        }
        String digits = new StringBuilder(matcher.group(1)).reverse().toString(); //reverse and crop leading 0
        if (digits.length() > wordLength) {
            throw new InterpretationException("binary number is too large for " + wordLength + "bits", sVal);
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
        if (pointer < 0 || pointer >= instructions.length) {
            throw new IllegalArgumentException("invalid pointer position");
        }
        instructionPointer = pointer;
    }

    public Map<String, Integer> getMemoryLookupTable() {
        return memoryLookupTable;
    }

    public boolean hasInstructions() {
        return instructionPointer < instructions.length;
    }

    public void reset() {
        setInstructionPointer(firstInstruction);
    }

}