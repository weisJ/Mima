package edu.kit.mima.gui.menu;

import edu.kit.mima.app.MimaUserInterface;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Help menu frame for {@link MimaUserInterface}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class Help extends JFrame {

    private static final Charset ENCODING = StandardCharsets.UTF_8;
    private static final String HELP_LOCAL = "Help.md";
    private static final String HELP_WEB =
            "https://raw.githubusercontent.com/weisJ/Mima/master/README.md";
    private static final int MAXIMUM_ATTEMPTS = 20;

    private static final int CONNECTION_TIMEOUT = 3000;
    private static final int READ_TIMEOUT = 5000;
    private static final int RETRY_TIMEOUT = 20000;

    private static final Dimension SIZE = Toolkit.getDefaultToolkit().getScreenSize();

    @NotNull
    private static final Help instance = new Help();

    @Nullable
    private Thread loadSource;
    private boolean loadedFromWeb;

    private final JEditorPane panel;
    private final HTMLEditorKit kit;
    @Nullable
    private String source;

    /*
     * Construct the Help Screen
     */
    private Help() {
        setIconImage(
                Toolkit.getDefaultToolkit()
                        .getImage(getClass().getClassLoader().getResource("images/mima.png")));
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize((int) SIZE.getHeight() / 3, (int) SIZE.getWidth() / 3);
        setTitle("Help");
        setLocationRelativeTo(null);
        setResizable(false);

        panel = new JEditorPane();
        kit = new HTMLEditorKit();
        panel.setEditorKit(kit);
        panel.setEditable(false);

        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body {color:#D8D8D8, font-family:Monospaced}");

        var scrollPane = new JScrollPane();
        scrollPane.setViewportView(panel);
        add(scrollPane);

        loadSource = new Thread(this::fetchFromWebSource);
        loadSource.start();
    }

    /**
     * Get an help screen instance.
     *
     * @return instance of Help
     */
    @NotNull
    @Contract(pure = true)
    public static Help getInstance() {
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

    /*
     * Render the markdown to HTML
     */
    private static String renderMarkdown(final String text) {
        final Parser parser = Parser.builder().build();
        final Node document = parser.parse(text);
        final HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    private void showHtml(final String htmlSource) {
        SwingUtilities.invokeLater(
                () -> {
                    var doc = kit.createDefaultDocument();
                    panel.setDocument(doc);
                    panel.setText("<html><body>" + htmlSource + "</body></html>");
                });
    }

    /**
     * Close the Help Window.
     */
    public void close() {
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
     * Load ReadMe from github
     */
    private @Nullable
    String loadMarkdown() {
        try {
            final URLConnection urlConnection = new URL(HELP_WEB).openConnection();

            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(READ_TIMEOUT);

            try (final BufferedReader reader =
                         new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), ENCODING))) {
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
        try (final BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(HELP_LOCAL)),
                                     ENCODING))) {
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
