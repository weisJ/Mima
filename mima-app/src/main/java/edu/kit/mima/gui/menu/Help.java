package edu.kit.mima.gui.menu;

import edu.kit.mima.MimaUserInterface;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.JFrame;

/**
 * Help menu frame for {@link MimaUserInterface}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class Help extends JFrame {

    private static final Charset ENCODING = StandardCharsets.UTF_8;
    private static final String HELP_LOCAL = "Help.md";
    private static final String HELP_WEB = "https://raw.githubusercontent.com/weisJ/Mima/master/README.md";
    private static final int MAXIMUM_ATTEMPTS = 20;

    private static final int CONNECTION_TIMEOUT = 3000;
    private static final int READ_TIMEOUT = 5000;
    private static final int RETRY_TIMEOUT = 20000;

    private static final Dimension SIZE = Toolkit.getDefaultToolkit().getScreenSize();

    private static Help instance;

    @Nullable private static Thread loadSource;
    private static boolean loadedFromWeb;

    private static JFXPanel jfxPanel;
    @Nullable private static String source;

    /*
     * Construct the Help Screen
     */
    private Help() {
        setIconImage(Toolkit.getDefaultToolkit()
                             .getImage(getClass().getClassLoader().getResource("images/mima.png")));
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
     * Get an help screen instance.
     *
     * @return instance of Help
     */
    public static Help getInstance() {
        if (instance == null) {
            instance = new Help();
        }
        return instance;
    }

    /**
     * Show the window.
     *
     * @param parent parent component
     */
    public static void showWindow(final Component parent) {
        final Help s = getInstance();
        s.setLocationRelativeTo(parent);
        s.setVisible(true);
    }

    /**
     * Hide window instance.
     */
    public static void hideWindow() {
        getInstance().setVisible(false);
    }

    private static void showHtml(final String htmlSource) {
        Platform.runLater(() -> {
            final WebView webView = new WebView();
            webView.getEngine().loadContent(
                    "<html><body text=\"#D8D8D8\" "
                            + "bgcolor=\"#3c3f41\""
                            + "style='font-family: "
                            + "Menlo,Monaco,Consolas,\"Liberation Mono\",\"Courier New\","
                            + "monospace'>"
                            + htmlSource
                            + "</body></html>");
            jfxPanel.setScene(new Scene(webView));
        });
    }

    /**
     * Close the Help Window.
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
     * Render the markdown to HTML
     */
    private static String renderMarkdown(final String text) {
        final Parser parser = Parser.builder().build();
        final Node document = parser.parse(text);
        final HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    /*
     * Load ReadMe from github
     */
    private @Nullable String loadMarkdown() {
        try {
            final URLConnection urlConnection = new URL(HELP_WEB).openConnection();

            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(READ_TIMEOUT);

            try (final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream(), ENCODING)
            )) {
                final String markdown = reader.lines().collect(Collectors.joining("\n"));
                loadedFromWeb = true;
                return markdown;
            }
        } catch (@NotNull final IOException e) {
            return (source == null) ? loadFallback() : null;
        }
    }

    /*
     * Load local fallback option
     */
    @Nullable
    private String loadFallback() {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(
                        getClass().getClassLoader().getResourceAsStream(HELP_LOCAL)), ENCODING))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (@NotNull final IOException e) {
            return null;
        }
    }

    private void fetchFromWebSource() {
        showHtml(renderMarkdown(loadMarkdown()));
        boolean alive = true;
        int attempts = 0;
        while (!loadedFromWeb && alive && (attempts < MAXIMUM_ATTEMPTS)) {
            final String htmlSource = loadMarkdown();
            if (htmlSource != null) {
                source = htmlSource;
                showHtml(renderMarkdown(source));
            }
            try {
                Thread.sleep(RETRY_TIMEOUT);
                attempts++;
            } catch (@NotNull final InterruptedException e) {
                alive = false;
            }
        }
    }
}