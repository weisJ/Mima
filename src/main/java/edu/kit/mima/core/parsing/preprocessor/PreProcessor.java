package edu.kit.mima.core.parsing.preprocessor;

import edu.kit.mima.core.parsing.ParserException;
import edu.kit.mima.core.parsing.Processor;
import edu.kit.mima.core.parsing.ProcessorException;
import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;
import edu.kit.mima.core.parsing.token.Tuple;
import edu.kit.mima.core.parsing.token.ValueTuple;
import edu.kit.mima.preferences.MimaConstants;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import org.apache.tika.parser.txt.CharsetDetector;

import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Jannis Weis
 * @since 2018
 */
public final class PreProcessor extends Processor {

    private final StringBuilder processedInput;
    private final CharsetDetector charsetDetector;
    private final Set<String> processedFiles;
    private List<ParserException> errors;
    private boolean recursive;
    private boolean isHome = false;

    /**
     * Create new PreProcessor to process preProcessor Statements.
     *
     * @param inputString string to process
     * @param recursive   whether to recursively parse and input included files
     */
    public PreProcessor(final String inputString, boolean recursive) {
        super(inputString);
        this.processedInput = new StringBuilder(inputString);
        this.charsetDetector = new CharsetDetector();
        this.processedFiles = new HashSet<>();
        this.errors = new ArrayList<>();
        this.recursive = recursive;
    }

    /**
     * PreProcessor constructor for recursive including
     *
     * @param inputString    string to process
     * @param processedFiles set of processed files
     * @param isHome         whether the file to process is on the working directory
     */
    private PreProcessor(final String inputString, final Set<String> processedFiles, boolean isHome) {
        super(inputString);
        this.processedInput = new StringBuilder(inputString);
        this.charsetDetector = new CharsetDetector();
        this.processedFiles = processedFiles;
        this.isHome = isHome;
        this.errors = new ArrayList<>();
    }

    /**
     * Process the String.
     *
     * @return processed String.
     */
    public Tuple<String, List<ParserException>> process() {
        List<Point> deleteRanges = new ArrayList<>();
        errors = new ArrayList<>();
        errors.addAll(skipError());
        while (!input.isEmpty()) {
            int index = input.getPosition();
            try {
                if (isPunctuation(Punctuation.PRE_PROC)) {
                    input.next();
                    deleteRanges.add(processStatement(index - 1));
                } else {
                    input.next();
                }
            } catch (ParserException e) {
                errors.add(e);
                deleteRanges.add(new Point(index - 1, input.getPosition() - 1));
            }
        }
        deleteRanges.sort((p, q) -> Integer.compare(q.y, p.y));
        for (Point p : deleteRanges) {
            processedInput.delete(p.x, p.y);
        }
        return new ValueTuple<>(processedInput.toString(), errors);
    }

    /**
     * Process preprocessor statement
     *
     * @param beginIndex begin index of statement
     * @return Point (x,y) where x denotes the start of the statement and y the end of statement inside file.
     */
    private Point processStatement(int beginIndex) {
        if (isKeyword(Keyword.INPUT)) {
            input.next();
            processInput();
        } else {
            unexpected();
        }
        skipPunctuation(Punctuation.INSTRUCTION_END);
        return new Point(beginIndex, input.getPosition());
    }

    /**
     * Process inputFile
     */
    private void processInput() {
        Token token = input.peek();
        if (token != null && token.getType() == TokenType.STRING) {
            if (!recursive) {
                input.next();
                return;
            }
            input.next();
            String path = token.getValue().toString();
            String newPath = parseInputPath(path);

            var pref = Preferences.getInstance();
            File workingDir = new File(pref.readString(PropertyKey.DIRECTORY_WORKING));
            File homeDir = new File(pref.readString(PropertyKey.DIRECTORY_MIMA));
            boolean success = false;
            for (String ext : MimaConstants.EXTENSIONS) {
                if (success
                        || (!isHome
                                    && workingDir.exists()
                                    && tryPath(workingDir.getAbsolutePath() + newPath + '.' + ext, false))
                        || (homeDir.exists()
                                    && tryPath(homeDir.getAbsolutePath() + newPath + '.' + ext, true))
                        || (tryPath(path, false))) {
                    success = true;
                }
            }
            if (!success) {
                input.error("Can't find/load file: " + path);
            }
        } else {
            input.error("!input must be followed by input path");
        }
    }

    /**
     * Try to input file from path.
     *
     * @param path   path to try out
     * @param isHome whether current file is located inside the home directory
     * @return true if successful
     */
    private boolean tryPath(String path, boolean isHome) {
        if (processedFiles.contains(path)) {
            return true;
        }
        try {
            String file = loadFile(path);
            processedFiles.add(path);

            processedInput.append("\n#<<File = ").append(path).append(">>#\n");
            var processed = new PreProcessor(file, processedFiles, isHome).process();
            List<ParserException> err = processed.getSecond();
            if (!err.isEmpty()) {
                errors.add(new ProcessorException("File not found: \"" + path + "\""));
                errors.addAll(err);
            }
            processedInput.append(processed.getFirst());
            processedInput.append("\n#<<File>>#\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Convert path from being split by '.' to '\'. Additional '\' is inserted in front.
     *
     * @param path path to convert
     * @return converted path
     */
    private String parseInputPath(String path) {
        String[] hierarchy = path.replaceAll("\\s+", "").split("\\.");
        StringBuilder newPath = new StringBuilder();
        for (String s : hierarchy) {
            newPath.append('\\').append(s);
        }
        return newPath.toString();
    }

    /**
     * Load file
     *
     * @param path path to file
     * @return content of file
     * @throws IOException if file is not found or could not be loaded
     */
    private String loadFile(String path) throws IOException {
        String charSet = charsetDetector
                .setText(new BufferedInputStream(new FileInputStream(path)))
                .detect().getName();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), charSet))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }


}
