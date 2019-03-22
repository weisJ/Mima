package edu.kit.mima.core.parsing.preprocessor;

import edu.kit.mima.api.util.Tuple;
import edu.kit.mima.api.util.ValueTuple;
import edu.kit.mima.core.MimaConstants;
import edu.kit.mima.core.parsing.ParserException;
import edu.kit.mima.core.parsing.Processor;
import edu.kit.mima.core.parsing.ProcessorException;
import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.token.Token;
import edu.kit.mima.core.token.TokenType;
import org.apache.tika.parser.txt.CharsetDetector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
 * Preprocessor for Mima files.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class PreProcessor extends Processor {

    @NotNull private final String workingDirectory;
    @NotNull private final String mimaDirectory;
    @NotNull private final StringBuilder processedInput;
    @NotNull private final CharsetDetector charsetDetector;
    @NotNull private final Set<String> processedFiles;
    private List<ParserException> errors;
    private boolean recursive;
    private boolean isHome = false;

    /**
     * Create new PreProcessor to process preProcessor Statements.
     *
     * @param inputString      string to process
     * @param workingDirectory working directory
     * @param mimaDirectory    mima directory
     * @param recursive        whether to recursively parse and input included files
     */
    public PreProcessor(@NotNull final String inputString,
                        @NotNull final String workingDirectory,
                        @NotNull final String mimaDirectory,
                        final boolean recursive) {
        super(inputString);
        this.workingDirectory = workingDirectory;
        this.mimaDirectory = mimaDirectory;
        this.processedInput = new StringBuilder(inputString);
        this.charsetDetector = new CharsetDetector();
        this.processedFiles = new HashSet<>();
        this.errors = new ArrayList<>();
        this.recursive = recursive;
    }

    /**
     * PreProcessor constructor for recursive including.
     *
     * @param inputString      string to process
     * @param processedFiles   set of processed files
     * @param workingDirectory working directory
     * @param mimaDirectory    mima directory
     * @param isHome           whether the file to process is on the working directory
     */
    private PreProcessor(@NotNull final String inputString,
                         @NotNull final Set<String> processedFiles,
                         @NotNull final String workingDirectory,
                         @NotNull final String mimaDirectory,
                         final boolean isHome) {
        super(inputString);
        this.workingDirectory = workingDirectory;
        this.mimaDirectory = mimaDirectory;
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
    @NotNull
    @Contract(" -> new")
    public Tuple<String, List<ParserException>> process() {
        final List<Point> deleteRanges = new ArrayList<>();
        errors = new ArrayList<>();
        errors.addAll(skipError());
        while (!input.isEmpty()) {
            final int index = input.getPosition();
            try {
                if (isPunctuation(Punctuation.PRE_PROC)) {
                    input.next();
                    deleteRanges.add(processStatement(index - 1));
                } else {
                    input.next();
                }
            } catch (@NotNull final ParserException e) {
                errors.add(e);
                deleteRanges.add(new Point(index - 1, input.getPosition() - 1));
            }
        }
        deleteRanges.sort((p, q) -> Integer.compare(q.y, p.y));
        for (final Point p : deleteRanges) {
            processedInput.delete(p.x, p.y);
        }
        return new ValueTuple<>(processedInput.toString(), errors);
    }

    /**
     * Process preprocessor statement.
     *
     * @param beginIndex begin index of statement
     * @return Point (x,y) where x denotes the start of the statement and y the end of statement
     *         inside file.
     */
    @NotNull
    @Contract("_ -> new")
    private Point processStatement(final int beginIndex) {
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
     * Process inputFile.
     */
    private void processInput() {
        final Token token = input.peek();
        if (token != null && token.getType() == TokenType.STRING) {
            if (!recursive) {
                input.next();
                return;
            }
            input.next();
            final String path = token.getValue().toString();
            final String newPath = parseInputPath(path);

            final File workingDir = new File(workingDirectory);
            final File homeDir = new File(mimaDirectory);
            boolean success = false;
            for (final String ext : MimaConstants.EXTENSIONS) {
                if (success) {
                    break;
                }
                success = !isHome && workingDir.exists()
                        && tryPath(workingDir.getAbsolutePath() + newPath + '.' + ext, false);
                if (!success) {
                    success = homeDir.exists()
                            && tryPath(homeDir.getAbsolutePath() + newPath + '.' + ext, true);
                    if (!success) {
                        success = (tryPath(path, false));
                    }
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
    private boolean tryPath(@NotNull final String path, final boolean isHome) {
        if (processedFiles.contains(path)) {
            return true;
        }
        try {
            final String file = loadFile(path);
            processedFiles.add(path);

            processedInput.append("\n#<<File = ").append(path).append(">>#\n");
            final var processed = new PreProcessor(file, processedFiles, workingDirectory,
                                                   mimaDirectory, isHome).process();
            final List<ParserException> err = processed.getSecond();
            if (!err.isEmpty()) {
                errors.add(new ProcessorException("File not found: \"" + path + "\""));
                errors.addAll(err);
            }
            processedInput.append(processed.getFirst());
            processedInput.append("\n#<<File>>#\n");
            return true;
        } catch (@NotNull final IOException e) {
            return false;
        }
    }

    /**
     * Convert path from being split by '.' to '\'. Additional '\' is inserted in front.
     *
     * @param path path to convert
     * @return converted path
     */
    @NotNull
    private String parseInputPath(@NotNull final String path) {
        final String[] hierarchy = path.replaceAll("\\s+", "").split("\\.");
        final StringBuilder newPath = new StringBuilder();
        for (final String s : hierarchy) {
            newPath.append('\\').append(s);
        }
        return newPath.toString();
    }

    /**
     * Load file.
     *
     * @param path path to file
     * @return content of file
     * @throws IOException if file is not found or could not be loaded
     */
    private String loadFile(@NotNull final String path) throws IOException {
        final String charSet = charsetDetector
                .setText(new BufferedInputStream(new FileInputStream(path)))
                .detect().getName();
        try (final BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), charSet))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }


}
