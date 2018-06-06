package edu.kit.mima.gui.menu;

import javafx.application.*;
import javafx.embed.swing.*;
import javafx.scene.*;
import javafx.scene.web.*;
import org.commonmark.node.Node;
import org.commonmark.parser.*;
import org.commonmark.renderer.html.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.stream.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public final class Help extends JFrame {

    private static Help instance;
    private static boolean closed = true;

    /*
     * Construct the Help Screen
     */
    private Help() {
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(500, 600);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closed = true;
                setVisible(false);
            }
        });
        setTitle("Help");
        setLocationRelativeTo(null);
        setResizable(false);
        JFXPanel jfxPanel = new JFXPanel();
        Platform.runLater(() -> {
            WebView webView = new WebView();
            webView.getEngine().loadContent("<html> Help!");
            webView.getEngine().loadContent(renderMarkdown(loadMarkdown("Help.md")));
            jfxPanel.setScene(new Scene(webView));
        });


        add(jfxPanel);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("mima.png")));
    }

    /**
     * Get an help screen instance
     *
     * @return instance of Help
     */
    public static Help getInstance() {
        if (instance == null || closed)
            instance = new Help();
        return instance;
    }

    /*
     * Load ReadME from github
     */
    private String loadMarkdown(String fileName) {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("Help.md")));
        return reader.lines().collect(Collectors.joining("\n"));
    }

    /*
     * Render the markdown to HTML
     */
    private String renderMarkdown(String text) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(text);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }
}
