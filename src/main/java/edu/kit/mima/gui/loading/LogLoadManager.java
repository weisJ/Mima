package edu.kit.mima.gui.loading;

import java.io.File;

import static edu.kit.mima.gui.logging.Logger.error;
import static edu.kit.mima.gui.logging.Logger.log;

/**
 * Implementation if LoadManager using the Logger
 *
 * @author Jannis Weis
 * @since 2018
 */
public class LogLoadManager implements LoadManager {
    @Override
    public void onLoad(final String filepath) {
        log("Loading: " + filepath + "...");
    }

    @Override
    public void afterRequest(final File chosenFile) {
        log(chosenFile.getAbsolutePath());
    }

    @Override
    public void afterLoad() {
        log("done");
    }

    @Override
    public void onSave(final String path) {
        log("Saving: " + path + "...");
    }

    @Override
    public void afterSave() {
        log("done");
    }

    @Override
    public void onFail(final String errorMessage) {
        error(errorMessage);
    }
}
