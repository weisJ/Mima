package edu.kit.mima.core.file;

/**
 * Object that represents a part of a file.
 *
 * @author Jannis Weis
 * @since 2018
 */
public interface FileObject {

    /**
     * Get the line of the object in file.
     *
     * @return line index.
     */
    int getLineIndex();

    /**
     * The offset in file to the start of object.
     *
     * @return offset in file
     */
    int getOffset();

    /**
     * The length of the object.
     *
     * @return length of object in file
     */
    int getLength();
}
