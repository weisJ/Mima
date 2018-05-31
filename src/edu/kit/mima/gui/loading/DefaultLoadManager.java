package edu.kit.mima.gui.loading;

import java.io.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class DefaultLoadManager implements LoadManager {
    @Override
    public void onLoad(String path) {
    }

    @Override
    public void afterRequest(File chosenFile) {
    }

    @Override
    public void afterLoad() {
    }

    @Override
    public void onSave(String path) {
    }

    @Override
    public void afterSave() {
    }

    @Override
    public void onFail(String errorMessage) {
    }
}
