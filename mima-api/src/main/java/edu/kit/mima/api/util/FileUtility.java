package edu.kit.mima.api.util;

import com.intellij.openapi.util.io.FileUtil;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * @author Jannis Weis
 * @since 2019
 */
public final class FileUtility {

    private FileUtility() {
    }

    /**
     * Will move the source File to the destination File.
     * The Method will backup the dest File, copy source to
     * dest, and then will delete the source and the backup.
     *
     * @param source File to be moved
     * @param dest   File to be overwritten (does not matter if
     *               non existent)
     * @throws IOException if an IO error occurs.
     */
    public static void moveAndOverwrite(final File source, final File dest) throws IOException {
        File backup = FileUtility.getNonExistingTempFile(dest);
        Files.copy(dest.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        if (!FileUtility.filesEqual(source, dest)) {
            if (!source.delete()) {
                throw new IOException("Failed to delete " + source.getName());
            }
        }
        if (!backup.delete()) {
            throw new IOException("Failed to delete " + backup.getName());
        }
    }

    /**
     * Recursive Method to generate a FileName in the same
     * Folder as the {@code inputFile}, that is not existing
     * and ends with {@code _temp}.
     *
     * @param inputFile The FileBase to generate a Tempfile
     * @return A non existing File
     */
    public static File getNonExistingTempFile(final File inputFile) {
        File tempFile = new File(inputFile.getParentFile(), inputFile.getName() + "_temp");
        if (tempFile.exists()) {
            return FileUtility.getNonExistingTempFile(tempFile);
        } else {
            return tempFile;
        }
    }

    /**
     * Check whether two given files are equal.
     *
     * @param f1 first file.
     * @param f2 second file.
     * @return true if the two paths of the files are equal.
     */
    public static boolean filesEqual(final File f1, final File f2) {
        return FileUtil.filesEqual(f1, f2);
    }

    /**
     * Deletes a file, never throwing an exception. If file is a directory, delete it and all sub-directories.
     *
     * <p>The difference between File.delete() and this method are:
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>No exceptions are thrown when a file or directory cannot be deleted.</li>
     * </ul>
     *
     * @param f file or directory to delete, can be {@code null}
     * @return {@code true} if the file or directory was deleted, otherwise
     * {@code false}
     */
    public static boolean deleteQuietly(final File f) {
        return FileUtils.deleteQuietly(f);
    }

    /**
     * Moves a file or directory to the destination directory.
     *
     * <p>When the destination is on another file system, do a "copy and delete".
     *
     * @param file          the file or directory to be moved
     * @param folder        the destination directory
     * @param createDestDir If {@code true} create the destination directory,
     *                      otherwise if {@code false} throw an IOException
     * @throws NullPointerException if source or destination is {@code null}
     * @throws FileExistsException  if the directory or file exists in the destination directory
     * @throws IOException          if source or destination is invalid
     * @throws IOException          if an IO error occurs moving the file
     * @since 1.4
     */
    public static void moveToDirectory(final File file, final File folder, final boolean createDestDir) throws IOException {
        FileUtils.moveToDirectory(file, folder, createDestDir);
    }

    /**
     * Copies a file or directory to within another directory preserving the file dates.
     *
     * <p>This method copies the source file or directory, along all its contents, to a
     * directory of the same name in the specified destination directory.
     *
     * <p>The destination directory is created if it does not exist.
     * If the destination directory did exist, then this method merges
     * the source with the destination, with the source taking precedence.
     *
     * <p><strong>Note:</strong> This method tries to preserve the files' last
     * modified date/times using {@link File#setLastModified(long)}, however
     * it is not guaranteed that those operations will succeed.
     * If the modification operation fails, no indication is provided.
     *
     * @param src  an existing file or directory to copy, must not be {@code null}
     * @param dest the directory to place the copy in, must not be {@code null}
     * @throws NullPointerException if source or destination is {@code null}
     * @throws IOException          if source or destination is invalid
     * @throws IOException          if an IO error occurs during copying
     * @since 2.6
     */
    public static void copyToDirectory(final File src, final File dest) throws IOException {
        FileUtils.copyToDirectory(src, dest);
    }
}
