package edu.kit.mima.core.legacy.intepretation;

import edu.kit.mima.core.data.MachineWord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Interpreter {

    // @formatter:off
    private static final String POINTER = "\\([^()]*\\)";
    private static final String DEFINITION = "§define";
    private static final String REFERENCE = "([^ :§]+)(?: )*(?:|:(?: )*([^ \n])*)";
    private static final String CONST = "const";
    private static final String BINARY_PREFIX = "0b";
    private static final String REFERENCE_PREFIX = "&";

    private static final Pattern COMMAND_PATTERN = Pattern.compile("^(?: )*([a-zA-Z]*(?:" + POINTER + ")?)(?: )*([^ :§]*)?(?: )*$");
    private static final Pattern COMMAND_LOOKUP = Pattern.compile("^(?: )*([^ :§]+)(?: )*:(?: )*([^\n]*)$");
    private static final Pattern COMMAND_INTERN = Pattern.compile("^([a-zA-Z]*(?:" + POINTER + ")?)§(&?-?[0-9]+)?$");

    private static final Pattern DEF_PATTERN = Pattern.compile("^(?: )*" + DEFINITION + "(?: )*" + REFERENCE + "(?: )*[^\\n]*$");
    private static final Pattern DEF_CONST_PATTERN = Pattern.compile("^(?: )*" + DEFINITION + "(?: )*" + CONST + "(?: )*" + REFERENCE + "(?: )*[^\\n]*$");
    // @formatter:on

    private final int constWordLength;
    private final int wordLength;
    private final String[] instructions;
    private int instructionPointer;
    private int firstInstruction;
    private int reservedIndex;

    private boolean isSilent;

    private Map<String, Integer> memoryLookupTable;
    private Map<String, Integer> instructionLookupTable;
    private Map<String, Integer> constLookupTable;


    public Interpreter(final String[] lines, final int constWordLength, final int wordLength) {
        super();
        instructions = lines;
        instructionPointer = 0;
        this.constWordLength = constWordLength;
        this.wordLength = wordLength;
        reservedIndex = -1;
    }

    public static String[] getKeywords() {
        return new String[]{"(?<![^ \n])" + DEFINITION + "(?![^ \n])"
                , "(?<![^ \n])" + CONST + "(?![^ \n])"
                , "(?<![^ \n]):(?![^ \n])"
                , "\\(", "\\)"
                , "(?<![^ \n])-?[0-9]+(?![^ (\n])"
                , "0b[01]*"
                , "#[^\n]*\n?"};
    }

    public void compile() {
        memoryLookupTable = new HashMap<>();
        instructionLookupTable = new HashMap<>();
        constLookupTable = new HashMap<>();
        reservedIndex = -1;
        setInstructionPointer(0);
        final int index = removeComments(instructions);
        firstInstruction = setupDefinitions(instructions, index, memoryLookupTable, constLookupTable);
        setupInstructionLookup(instructions, firstInstruction, instructionLookupTable);
        setInstructionPointer(firstInstruction);
        replaceAssociations();
        setInstructionPointer(firstInstruction);

    }

    public List<Set<String>> getReferences(final String[] lines) throws IllegalArgumentException {
        final Map<String, Integer> constTable = new HashMap<>();
        final Map<String, Integer> memoryTable = new HashMap<>();
        final Map<String, Integer> instructionTable = new HashMap<>();
        int index = removeComments(lines);
        isSilent = true;
        try {
            index = setupDefinitions(lines, index, memoryTable, constTable);
        } catch (final InterpretationException ignored) {
        }
        try {
            setupInstructionLookup(lines, index, instructionTable);
        } catch (final InterpretationException ignored) {
        }
        isSilent = false;
        return List.of(constTable.keySet(), memoryTable.keySet(), instructionTable.keySet());
    }

    private int removeComments(final String[] lines) {
        if (lines.length == 0) {
            return 0;
        }
        boolean foundFirstNonComment = false;
        int firstNonComment = 0;
        for (int i = 0; i < lines.length; i++) {
            if ((lines[i] != null) && (!lines[i].isEmpty())) {
                if (lines[i].charAt(0) == '#') {
                    lines[i] = "";
                } else if (lines[i].contains("#")) {
                    final String line = lines[i];
                    final int index = line.indexOf('#');
                    final int lastIndex = line.lastIndexOf('#');
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

    private int setupDefinitions(final String[] lines, final int startIndex, final Map<String, Integer> memoryMap,
                                 final Map<String, Integer> constMap) {
        if (lines.length == 0) {
            return startIndex;
        }
        int index = startIndex;
        while (lines[index].startsWith(DEFINITION)) {
            try {
                final String line = lines[index];
                boolean parsed = setConstDefinition(line, constMap);
                if (!parsed) {
                    parsed = setMemoryDefinition(line, memoryMap);
                }
                if (!parsed) {
                    throw new InterpretationException("invalid definition", lines[index], index + 1);
                }
                index++;
            } catch (final InterpretationException e) {
                if (!isSilent) {
                    throw e;
                }
            }
        }
        return index;
    }

    private boolean setMemoryDefinition(final String line, final Map<String, Integer> lookupTable) {
        final Matcher matcher = DEF_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return false;
        }
        parseDefinition(matcher, lookupTable, false);
        return true;
    }

    private boolean setConstDefinition(final String line, final Map<String, Integer> lookupTable) {
        final Matcher matcher = DEF_CONST_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return false;
        }
        parseDefinition(matcher, lookupTable, true);
        return true;
    }

    private void parseDefinition(final MatchResult matcher, final Map<String, Integer> table,
                                 final boolean allowNegative) {
        final String reference = matcher.group(1);
        if (reference.isEmpty()) {
            throw new InterpretationException("missing identifier", instructionPointer + 1);
        }
        int value = 0;
        if ((matcher.group(2) == null) || matcher.group(2).isEmpty()) {
            value = reservedIndex;
            reservedIndex--;
        } else {
            try {
                value = Integer.parseInt(matcher.group(2));
                if (!allowNegative && (value < 0)) {
                    fail("negative memory address", reference + " : " + value);
                }
            } catch (final NumberFormatException e) {
                fail("reference must be an integer", instructionPointer + 1);
            }
        }
        if (table.containsKey(reference)) {
            throw new InterpretationException("reference <" + reference + "> already defined",
                    instructionPointer + 1);
        }
        table.put(reference, value);
    }

    private void setupInstructionLookup(final String[] lines, final int startIndex,
                                        final Map<String, Integer> instructionMap) {
        if (lines.length == 0) {
            return;
        }
        for (int i = startIndex; i < lines.length; i++) {
            String line = lines[i];
            if (line.contains(":")) {
                line = addCommandEntry(line, i, instructionMap);
                lines[i] = line;
            }
        }
    }

    private String addCommandEntry(final String line, final int lineNumber, final Map<String, Integer> lookupTable) {
        final Matcher matcher = COMMAND_LOOKUP.matcher(line);
        if (!matcher.matches()) {
            throw new InterpretationException("incorrect jump association", lineNumber + 1);
        }
        final String reference = matcher.group(1);
        if (lookupTable.containsKey(reference)) {
            fail(reference + " is already associated with an instruction", lineNumber + 1);
        }
        lookupTable.put(reference, lineNumber + 1);
        return matcher.group(2);
    }

    private void replaceAssociations() {
        for (int i = instructionPointer; i < instructions.length; i++) {
            String line = instructions[i];

            //Bracket (...) operator
            final Pattern brackets = Pattern.compile(POINTER);
            final Matcher bracketMatcher = brackets.matcher(line);
            String pointerBracket = "";
            if (bracketMatcher.find()) {
                pointerBracket = bracketMatcher.group();
                line = line.replaceAll(POINTER, "");
            }

            final Matcher matcher = COMMAND_PATTERN.matcher(line);
            if (!matcher.matches()) {
                throw new InterpretationException("invalid instruction", i + 1);
            }
            final String command = (matcher.group(1) != null) ? matcher.group(1) : "";
            String value = (matcher.group(2) != null) ? matcher.group(2) : "";
            if (!value.isEmpty()) {
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
                        final int val = Integer.parseInt(value);
                        value = String.valueOf(val);
                    } catch (final NumberFormatException e) {
                        throw new InterpretationException("illegal memory address", value, i + 1);
                    }
                }
            }
            instructions[i] = command + pointerBracket + "§" + value;
        }
    }

    public CompiledInstruction nextInstruction() throws IllegalArgumentException {
        if (!hasInstructions()) {
            throw new IllegalStateException("No more instructions");
        }
        final String line = instructions[instructionPointer];
        instructionPointer++;
        final CompiledInstruction command = parseCommand(line);

        if (command == null) { //Ignore empty lines
            return nextInstruction();
        } else {
            return command;
        }
    }

    private CompiledInstruction parseCommand(final String l) {
        if ("§".equals(l)) {
            return null;
        }
        final Matcher matcher = COMMAND_INTERN.matcher(l);
        if (!matcher.matches()) {
            throw new InterpretationException("invalid instruction", instructionPointer);
        }
        //No argument command
        if ((matcher.group(2) == null) || matcher.group(2).isEmpty()) {
            return new CompiledInstruction(matcher.group(1), null, false);
        }

        //Argument command
        String sVal = matcher.group(2); //Argument value
        final boolean isReference = sVal.startsWith(REFERENCE_PREFIX);
        if (isReference) {
            sVal = sVal.substring(REFERENCE_PREFIX.length());
        }
        final int value;
        try {
            value = Integer.parseInt(sVal);
        } catch (final NumberFormatException e) {
            throw new InterpretationException("parameter is neither an integer value nor a memory reference",
                    instructionPointer + 1);
        }
        return new CompiledInstruction(matcher.group(1), new MachineWord(value, wordLength), isReference);
    }

    private int parseBinary(final String sVal) {
        final Pattern crop = Pattern.compile("0*([01]*)");
        final Matcher matcher = crop.matcher(sVal);
        if (!matcher.matches()) {
            throw new InterpretationException("not a binary number", sVal);
        }
        final String digits = new StringBuilder(matcher.group(1)).reverse().toString(); //reverse and crop leading 0
        if (digits.length() > wordLength) {
            throw new InterpretationException("binary number is too large for " + wordLength + "bits", sVal);
        }
        final boolean[] bits;
        if (digits.length() > constWordLength) {
            bits = new boolean[wordLength];
        } else {
            bits = new boolean[constWordLength];
        }
        for (int i = 0; i < digits.length(); i++) {
            bits[i] = digits.charAt(i) == '1';
        }
        return new MachineWord(bits, wordLength).intValue();
    }

    public int getInstructionPointer() {
        return instructionPointer;
    }

    public void setInstructionPointer(final int pointer) {
        if ((pointer < 0) || (pointer >= instructions.length)) {
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

    /*
     * Mute Errors while fetching references
     */
    private void fail(final String message, final String line, final int lineNumber) {
        if (!isSilent) {
            throw new InterpretationException(message, line, lineNumber);
        }
    }

    private void fail(final String message, final int lineNumber) {
        if (!isSilent) {
            throw new InterpretationException(message, lineNumber);
        }
    }

    private void fail(final String message, final String line) {
        if (!isSilent) {
            throw new InterpretationException(message, line);
        }
    }

    private void fail(final String message) {
        if (!isSilent) {
            throw new InterpretationException(message);
        }
    }

}