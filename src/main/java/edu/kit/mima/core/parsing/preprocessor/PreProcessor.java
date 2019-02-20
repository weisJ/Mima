package edu.kit.mima.core.parsing.preprocessor;

import edu.kit.mima.core.parsing.ParseReferences;
import edu.kit.mima.core.parsing.Processor;
import edu.kit.mima.core.parsing.lang.Keyword;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.parsing.token.Token;
import edu.kit.mima.core.parsing.token.TokenType;
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
public class PreProcessor extends Processor {

    private final StringBuilder processedInput;
    private final CharsetDetector charsetDetector;
    private boolean isHome = false;

    private final Set<String> processedFiles;

    public PreProcessor(final String inputString) {
        super(inputString);
        this.processedInput = new StringBuilder(inputString);
        this.charsetDetector = new CharsetDetector();
        this.processedFiles = new HashSet<>();
    }

    private PreProcessor(final String inputString, final Set<String> processedFiles, boolean isHome) {
        super(inputString);
        this.processedInput = new StringBuilder(inputString);
        this.charsetDetector = new CharsetDetector();
        this.processedFiles = processedFiles;
        this.isHome = isHome;
    }

    public String process() {
        List<Point> deleteRanges = new ArrayList<>();
        while (!input.isEmpty()) {
            int index = input.getPosition();
            if (isPunctuation(Punctuation.PRE_PROC)) {
                input.next();
                deleteRanges.add(processStatement(index - 1));
            } else {
                input.next();
            }
        }
        deleteRanges.sort((p, q) -> Integer.compare(q.y, p.y));
        for (Point p : deleteRanges) {
            processedInput.delete(p.x, p.y);
        }
        return processedInput.toString();
    }

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

    private void processInput() {
        Token token = input.peek();
        if (token != null && token.getType() == TokenType.STRING) {
            input.next();
            String path = token.getValue().toString();
            String newPath = parseInputPath(path);

            File workingDir = new File(ParseReferences.WORKING_DIRECTORY);
            File homeDir = new File(ParseReferences.MIMA_DIR);
            boolean success = false;
            for (String ext : ParseReferences.FILE_EXTENSIONS) {
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

    private boolean tryPath(String path, boolean isHome) {
        if (processedFiles.contains(path)) {
            return true;
        }
        try {
            String file = loadFile(path);
            processedFiles.add(path);

            processedInput.append("\n#<<File = ").append(path).append(">>#\n");
            processedInput.append(new PreProcessor(file, processedFiles, isHome).process());
            processedInput.append("\n#<<File>>#\n");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private String parseInputPath(String path) {
        String[] hierarchy = path.split("\\.");
        StringBuilder newPath = new StringBuilder();
        for (String s : hierarchy) {
            newPath.append('\\').append(s);
        }
        return newPath.toString();
    }

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
