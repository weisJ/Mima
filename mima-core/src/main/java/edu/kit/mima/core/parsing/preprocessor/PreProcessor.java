package edu.kit.mima.core.parsing.preprocessor;

import edu.kit.mima.api.loading.IoTools;
import edu.kit.mima.api.util.Tuple;
import edu.kit.mima.api.util.ValueTuple;
import edu.kit.mima.core.MimaConstants;
import edu.kit.mima.core.parsing.ParserException;
import edu.kit.mima.core.parsing.Processor;
import edu.kit.mima.core.parsing.ProcessorException;
import edu.kit.mima.core.parsing.inputstream.TokenStream;
import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.token.Token;
import edu.kit.mima.core.token.TokenType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Preprocessor for Mima files.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class PreProcessor extends Processor<Token, TokenStream> {

    @NotNull private final String workingDirectory;
    @NotNull private final String mimaDirectory;
    @NotNull private final StringBuilder processedInput;
    @NotNull private final Set<File> processedFiles;
    private List<ParserException> errors;
    private boolean recursive;
    private boolean isHome = false;

    /**
     * Create new PreProcessor to process preProcessor Statements.
     *
     * @param inputString      string to process
     * @param inputPath        path to input file.
     * @param workingDirectory working directory
     * @param mimaDirectory    mima directory
     * @param recursive        whether to recursively parse and input included files
     */
    public PreProcessor(@NotNull final String inputString,
                        @NotNull final String inputPath,
                        @NotNull final String workingDirectory,
                        @NotNull final String mimaDirectory,
                        final boolean recursive) {
        super(new TokenStream(inputString));
        this.workingDirectory = workingDirectory;
        this.mimaDirectory = mimaDirectory;
        this.processedInput = new StringBuilder(inputString);
        this.processedFiles = new HashSet<>();
        processedFiles.add(new File(inputPath));
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
                         @NotNull final Set<File> processedFiles,
                         @NotNull final String workingDirectory,
                         @NotNull final String mimaDirectory,
                         final boolean isHome) {
        super(new TokenStream(inputString));
        this.workingDirectory = workingDirectory;
        this.mimaDirectory = mimaDirectory;
        this.processedInput = new StringBuilder(inputString);
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
            Optional.ofNullable(processCurrent()).ifPresent(deleteRanges::add);
        }
        deleteRanges.sort((p, q) -> Integer.compare(q.y, p.y));
        for (final Point p : deleteRanges) {
            processedInput.delete(p.x, p.y);
        }
        return new ValueTuple<>(processedInput.toString(), errors);
    }

    private @Nullable Point processCurrent() {
        final int index = input.getPosition();
        Point p = null;
        try {
            if (isPunctuation(Punctuation.PRE_PROC)) {
                p = processStatement(index - 1);
            }
            input.next();
        } catch (@NotNull final ParserException e) {
            errors.add(e);
            errors.addAll(skipError());
            p = new Point(index - 1, input.getPosition() - 1);
        }
        return p;
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
            input.next();
            if (!recursive) {
                return;
            }
            final String path = token.getValue().toString();
            final String newPath = parseInputPath(path);
            boolean success = false;
            for (final String ext : MimaConstants.EXTENSIONS) {
                if (tryPaths(newPath, path, ext)) {
                    success = true;
                    break;
                }
            }
            if (!success) {
                input.error("Can't find/load file: " + path);
            }
        } else {
            input.error("!input must be followed by input path");
        }
    }

    private boolean tryPaths(final String path, @NotNull final String fullPath,
                             final String extension) {
        final File workingDir = new File(workingDirectory);
        final File homeDir = new File(mimaDirectory);
        return !isHome && workingDir.exists()
                && tryPath(workingDir.getAbsolutePath() + path + '.' + extension, false)
                || homeDir.exists()
                && tryPath(homeDir.getAbsolutePath() + path + '.' + extension, true)
                || tryPath(fullPath, false);
    }

    /**
     * Try to input file from path.
     *
     * @param path   path to try out
     * @param isHome whether current file is located inside the home directory
     * @return true if successful
     */
    private boolean tryPath(@NotNull final String path, final boolean isHome) {
        var filePath = new File(path);
        boolean success;
        if (processedFiles.add(filePath)) {
            try {
                final String file = IoTools.loadFile(path);
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
                success = true;
            } catch (@NotNull final IOException e) {
                success = false;
            }
        } else {
            success = false;
        }
        return success;
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

    @Nullable
    @Override
    protected Token parseDelimiter() {
        return input.next();
    }
}
