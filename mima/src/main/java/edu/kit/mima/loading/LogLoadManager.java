package edu.kit.mima.loading;

import edu.kit.mima.logging.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Implementation of LoadManager using the Logger.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class LogLoadManager implements LoadManager {
    @Override
    public void beforeLoad() {
    }

    @Override
    public void onLoad(final String filepath) {
        Logger.log("Loading: " + filepath + "...");
    }

    @Override
    public void afterRequest(@NotNull final File chosenFile) {
        Logger.log(chosenFile.getAbsolutePath());
    }

    @Override
    public void afterLoad() {
        Logger.log("done");
    }

    @Override
    public void onSave(final String path) {
        Logger.log("Saving: " + path + "...");
    }

    @Override
    public void afterSave() {
        Logger.log("done");
    }

    @Override
    public void onFail(final String errorMessage) {
        Logger.error(errorMessage);
    }
}
