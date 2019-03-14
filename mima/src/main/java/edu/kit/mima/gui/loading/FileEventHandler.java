package edu.kit.mima.gui.loading;

/**
 * @author Jannis Weis
 * @since 2018
 */
public interface FileEventHandler {

    void fileLoadedEvent(String filePath);

    void saveEvent(String filePath);

    void fileCreated(String fileName);
}
