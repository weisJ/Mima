package edu.kit.mima.loading;

import edu.kit.mima.App;
import edu.kit.mima.api.loading.LoadManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Implementation of LoadManager using the ConsoleLogger.
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
        App.logger.log("Loading: " + filepath + "...");
    }

    @Override
    public void afterRequest(@NotNull final File chosenFile) {
        App.logger.log(chosenFile.getAbsolutePath());
    }

    @Override
    public void afterLoad() {
        App.logger.log("done");
    }

    @Override
    public void onSave(final String path) {
        App.logger.log("Saving: " + path + "...");
    }

    @Override
    public void afterSave() {
        App.logger.log("done");
    }

    @Override
    public void onFail(final String errorMessage) {
        App.logger.error(errorMessage);
    }
}
