package edu.kit.mima.api.loading;

import java.io.File;

/**
 * Manager for performing actions at certain steps of loading/saving.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface LoadManager {

    /**
     * Actions to perform before loading a file.
     */
    void beforeLoad();

    /**
     * Action to perform at the load process.
     *
     * @param filepath path to file being loaded
     */
    void onLoad(String filepath);

    /**
     * Action to perform after the file request has been made.
     *
     * @param chosenFile File chosen
     */
    void afterRequest(File chosenFile);

    /**
     * Action to perform after load process.
     */
    void afterLoad();

    /**
     * Action to perform at the saving process.
     *
     * @param path path to file being saved
     */
    void onSave(String path);

    /**
     * Action to perform after save process.
     */
    void afterSave();

    /**
     * Action to perform after saving/loading has failed.
     *
     * @param errorMessage fail message
     */
    void onFail(String errorMessage);
}
