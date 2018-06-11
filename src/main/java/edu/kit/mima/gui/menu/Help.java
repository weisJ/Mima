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
    private static final int MAXIMUM_ATTEMPTS = 20;

    private static final Dimension SIZE = Toolkit.getDefaultToolkit().getScreenSize();

    private static Help instance;

    private static Thread loadSource;
    private static boolean loadedFromWeb;

    private static JFXPanel jfxPanel;

    private static String source;

    /*
     * Construct the Help Screen
     */
    private Help() {
        super();
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("mima.png")));
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize((int) SIZE.getHeight() / 3, (int) SIZE.getWidth() / 3);
        setTitle("Help");
        setLocationRelativeTo(null);
        setResizable(false);

        jfxPanel = new JFXPanel();
        add(jfxPanel);

        loadSource = new Thread(this::fetchFromWebSource);
        loadSource.start();
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

    private static void showHtml(final String htmlSource) {
        Platform.runLater(() -> {
            final WebView webView = new WebView();
            webView.getEngine().loadContent("<html> Html loaded");
            webView.getEngine().loadContent(htmlSource);
            jfxPanel.setScene(new Scene(webView));
        });
    }

    /**
     * Close the Help Window
     */
    public static void close() {
        if (loadSource != null) {
            final Thread stop = loadSource;
            loadSource = null;
            stop.interrupt();
        }
        if (instance != null) {
            instance.dispose();
        }
    }

    /*
     * Load ReadME from github
     */
    private String loadMarkdown() {
        try {
            final URLConnection c = new URL(HELP_WEB).openConnection();

            // set the connection timeout to 3 seconds and the read timeout to 5 seconds
            c.setConnectTimeout(3000);
            c.setReadTimeout(5000);

            // get a stream to read data from
            final BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
            final String markdown = reader.lines().collect(Collectors.joining("\n"));
            loadedFromWeb = true;
            return markdown;
        } catch (final IOException e) {
            return (source == null) ? loadFallback() : null;
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
    private String renderMarkdown(final String text) {
        final Parser parser = Parser.builder().build();
        final Node document = parser.parse(text);
        final HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    private void fetchFromWebSource() {
        boolean alive = true;
        int attempts = 0;
        showHtml(renderMarkdown(loadMarkdown()));
        while (!loadedFromWeb && alive && (attempts < MAXIMUM_ATTEMPTS)) {
            final String htmlSource = loadMarkdown();
            if (htmlSource != null) {
                source = htmlSource;
                showHtml(renderMarkdown(source));
            }
            try {
                Thread.sleep(20000);
                attempts++;
            } catch (final InterruptedException e) {
                alive = false;
            }
        }
    }
}
