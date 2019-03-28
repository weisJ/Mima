package edu.kit.mima.api.loading;

import edu.kit.mima.api.lambda.LambdaUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Component;
import java.io.File;
import java.util.Optional;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Reader/Writer that takes user input to determine the location the file is saved to.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class FileRequester {

    private final LoadManager manager;
    private final Component parent;

    /**
     * FileRequester to control loading and saving files as well as handling the request for file
     * paths.
     *
     * @param parent  parent component for dialogs
     * @param manager underlying load manager
     */
    @Contract(pure = true)
    public FileRequester(final Component parent, final LoadManager manager) {
        this.manager = manager;
        this.parent = parent;
    }

    /**
     * Request the user to choose a file location and save the text to this location.
     *
     * @param text         text to save
     * @param searchPath   starting directory for the file chooser
     * @param extension    allowed file extension
     * @param abortHandler action to perform if request was aborted
     */
    public void requestSave(@NotNull final String text,
                            final String searchPath,
                            @NotNull final String extension,
                            @NotNull final Runnable abortHandler) {
        try {
            Optional.ofNullable(requestPath(searchPath, new String[]{extension}, abortHandler))
                    .map(p -> !p.endsWith(extension) ? p + '.' + extension : p)
                    .stream()
                    .peek(manager::onSave)
                    .peek(LambdaUtil.reduceFirst(LambdaUtil.wrap(IoTools::saveFile), text))
                    .forEach(p -> manager.afterSave());
        } catch (final RuntimeException e) {
            manager.onFail(e.getMessage());
        }
    }

    /**
     * Request the user to choose a file location and load the text from this location.
     *
     * @param searchPath   starting directory for the file chooser
     * @param extensions   allowed file extension
     * @param abortHandler action to perform if request was aborted
     * @return loaded text
     */
    public @Nullable String requestLoad(final String searchPath,
                                        final String[] extensions,
                                        @NotNull final Runnable abortHandler) {
        manager.beforeLoad();
        try {
            return Optional.ofNullable(requestPath(searchPath, extensions, abortHandler))
                    .stream()
                    .peek(manager::onLoad)
                    .map(LambdaUtil.wrap(IoTools::loadFile))
                    .findFirst().orElse(null);
        } catch (final RuntimeException e) {
            manager.onFail(e.getMessage());
            return null;
        }
    }

    /*
     * Request the path form user using JFileChooser
     */
    private @Nullable String requestPath(final String savedPath,
                                         final String[] extensions,
                                         @NotNull final Runnable abortHandler) {
        final JFileChooser chooser = new JFileChooser(savedPath);
        final FileNameExtensionFilter filter = new FileNameExtensionFilter(extensions[0],
                                                                           extensions);
        chooser.setFileFilter(filter);
        return Optional.of(chooser.showDialog(parent, "Choose File")).stream()
                .filter(i -> i == JFileChooser.APPROVE_OPTION)
                .map(i -> chooser.getSelectedFile())
                .peek(manager::afterRequest)
                .map(File::getAbsolutePath)
                .findFirst()
                .orElseGet(() -> {
                    abortHandler.run();
                    return null;
                });
    }
}
