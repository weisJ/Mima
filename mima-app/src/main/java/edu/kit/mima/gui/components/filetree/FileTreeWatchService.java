package edu.kit.mima.gui.components.filetree;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

/**
 * @author Jannis Weis
 * @since 2019
 */
public class FileTreeWatchService {

    private final WatchService watchService;
    @NotNull
    private final Path directory;
    private final FileTree fileTree;
    private Thread thread;
    private boolean running;

    /**
     * Create new Watch service that watches for file changes and updates the FileTree accordingly.
     *
     * @param directory directory to watch.
     * @param fileTree  FileTree to modify.
     * @throws IOException if an I/O error occurs.
     */
    public FileTreeWatchService(@NotNull final Path directory, final FileTree fileTree) throws IOException {
        this.directory = directory;
        this.fileTree = fileTree;
        this.running = false;
        watchService = FileSystems.getDefault().newWatchService();
        directory.register(watchService,
                           StandardWatchEventKinds.ENTRY_CREATE,
                           StandardWatchEventKinds.ENTRY_DELETE,
                           StandardWatchEventKinds.ENTRY_MODIFY);
    }

    /**
     * Start the watch service.
     */
    public void start() {
        if (running) {
            return;
        }
        thread = new Thread(() -> {
            running = true;
            WatchKey key;
            boolean interrupted = false;
            while (!interrupted) {
                try {
                    if ((key = watchService.take()) == null) break;
                    for (WatchEvent<?> event : key.pollEvents()) {
                        var kind = event.kind();
                        Path path = directory.toAbsolutePath().resolve(((Path) event.context()));
                        if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                            create(path);
                        } else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                            delete(path);
                        } else if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                            modify(path);
                        }
                    }
                    key.reset();
                } catch (InterruptedException ex) {
                    interrupted = true;
                }
            }
            running = false;
        });
        thread.start();
    }

    /**
     * Stop the watch service.
     */
    public void stop() {
        if (!running) {
            return;
        }
        thread.interrupt();
    }

    private void create(@NotNull final Path path) {
        SwingUtilities.invokeLater(() -> {
            var node = fileTree.getTreeNodeMap().get(path.getParent().toFile());
            var file = path.toFile();
            var newNode = new FileTreeNode(file);
            fileTree.getTreeNodeMap().put(file, newNode);
            node.add(newNode);
            node.sort();
            ((DefaultTreeModel) fileTree.getTree().getModel()).nodeStructureChanged(node);
        });
    }

    private void delete(@NotNull final Path path) {
        SwingUtilities.invokeLater(() -> {
            var node = fileTree.getTreeNodeMap().get(path.toFile());
            ((DefaultMutableTreeNode) node.getParent()).remove(node);
            fileTree.getTreeNodeMap().remove(path.toFile());
            ((DefaultTreeModel) fileTree.getTree().getModel()).nodeStructureChanged(node);
        });
    }

    private void modify(@NotNull final Path path) {
        SwingUtilities.invokeLater(() -> {
            var node = fileTree.getTreeNodeMap().get(path.toFile());
            node.setUserObject(path.toFile());
            node = (FileTreeNode) node.getParent();
            node.sort();
            ((DefaultTreeModel) fileTree.getTree().getModel()).nodeStructureChanged(node);
        });
    }

}
