package edu.kit.mima.gui.loading;

import java.io.*;

import static edu.kit.mima.gui.logging.Logger.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class LogLoadManager implements LoadManager {
    @Override
    public void onLoad(String path) {
        log("Loading: " + path + "...");
    }

    @Override
    public void afterRequest(File chosenFile) {
        log(chosenFile.getAbsolutePath());
    }

    @Override
    public void afterLoad() {
        log("done");
    }

    @Override
    public void onSave(String path) {
        log("Saving: " + path + "...");
    }

    @Override
    public void afterSave() {
        log("done");
    }

    @Override
    public void onFail(String errorMessage) {
        error(errorMessage);
    }
}
