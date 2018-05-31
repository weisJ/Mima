package edu.kit.mima.gui.loading;

import java.io.File;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface LoadManager {

    void onLoad(String filepath);

    void afterRequest(File chosenFile);

    void afterLoad();

    void onSave(String path);

    void afterSave();

    void onFail(String errorMessage);

}
