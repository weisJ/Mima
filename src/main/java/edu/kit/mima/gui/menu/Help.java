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
import java.net.*;
import java.util.stream.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public final class Help extends JFrame {

    private static final String HELP_LOCALE = "Help.md";
    private static final String HELP_WEB = "https://raw.githubusercontent.com/weisJ/Mima/master/README.md";

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
            webView.getEngine().loadContent(renderMarkdown(loadMarkdown()));
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
        if (instance == null) {
            instance = new Help();
        }
        return instance;
    }

    /*
     * Load ReadME from github
     */
    private String loadMarkdown() {
        try {
            URLConnection c = new URL(HELP_WEB).openConnection();

            // set the connection timeout to 3 seconds and the read timeout to 5 seconds
            c.setConnectTimeout(3000);
            c.setReadTimeout(50000);

            // get a stream to read data from
            BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return loadFallback();
        }
    }

    /*
     * Load local fallback option
     */
    private String loadFallback() {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream(HELP_LOCALE)));
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
