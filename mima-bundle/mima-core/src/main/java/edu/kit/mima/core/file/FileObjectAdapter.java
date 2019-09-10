package edu.kit.mima.core.file;

/**
 * Adapter class for {@link FileObject}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class FileObjectAdapter implements FileObject {
    @Override
    public int getLineIndex() {
        return 0;
    }

    @Override
    public int getOffset() {
        return 0;
    }

    @Override
    public int getLength() {
        return 0;
    }
}
