package edu.kit.mima.loading;

/**
 * Event Handler for File Events.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface FileEventHandler {

    void fileLoadedEvent(String filePath);

    void saveEvent(String filePath);

    void fileCreated(String fileName);
}
