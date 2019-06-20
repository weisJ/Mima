import edu.kit.mima.gui.components.filetree.FileTree;
import edu.kit.mima.gui.laf.LafManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * @author Jannis Weis
 * @since 2019
 */
public final class FileTreeDemo {

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            System.setProperty("org.apache.batik.warn_destination", "false");
            LafManager.setDefaultTheme(true);
            String path = "C:\\Users\\weisj\\OneDrive - bwedu\\Dokumente\\Code\\Java\\Mima";


            JFrame frame = new JFrame("FileTree");
            frame.setForeground(Color.black);
            frame.setBackground(Color.lightGray);
            Container cp = frame.getContentPane();

            cp.setLayout(new BoxLayout(cp, BoxLayout.X_AXIS));
            cp.add(new FileTree(new File(path)));
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
