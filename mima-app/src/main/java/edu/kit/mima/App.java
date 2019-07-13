package edu.kit.mima;

import com.j256.simplejmx.client.JmxClient;
import com.j256.simplejmx.server.JmxServer;
import edu.kit.mima.api.logging.LogLevel;
import edu.kit.mima.app.MimaUserInterface;
import edu.kit.mima.core.MimaCoreDefaults;
import edu.kit.mima.gui.laf.LafManager;
import edu.kit.mima.gui.persist.PersistenceManager;
import edu.kit.mima.logger.ConsoleLogger;
import edu.kit.mima.preferences.Preferences;
import edu.kit.mima.preferences.PropertyKey;
import edu.kit.mima.session.MimaRequestHandler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import javax.management.JMException;
import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * Entry point for the Application.
 *
 * @author Jannis Weis
 * @since 2018
 */
public final class App {

    public static final ConsoleLogger logger = new ConsoleLogger();
    private static final int JMX_PORT = 8000;
    private static final int LOCK_PORT = 65535;
    //Todo: Have tasks that do heavy static initialization register to run at startup. This prevents the program from
    // taking a long time before it shows the splash screen.
    private static final String[] fakeLoadMessages =
            new String[]{"Loading Icons"};
    private static MimaUserInterface frame;
    private static MimaSplash splash;
    private static int index = 0;
    private static Timer timer;

    /*
     * Keep reference to the socket to prevent it from being removed by the garbage collector.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private static ServerSocket lockSocket;


    /**
     * Entry point for starting the Mima UI.
     *
     * @param args command line arguments (ignored)
     */
    public static void main(@Nullable final String[] args) {
        if (delegateToInstance(args)) {
            System.setProperty("org.apache.batik.warn_destination", "false");
            try {
                splash = new MimaSplash();
                splash.showSplash();
                System.out.println("makevisible");
            } catch (IOException ignored) {
            }
            SwingUtilities.invokeLater(() -> {
                init(getFilePath(args));
                timer = new Timer(200, e -> {
                    var m = nextMessage();
                    if (m != null) {
                        splash.showMessage(m);
                    } else {
                        timer.stop();
                        System.out.println("visible no more");
                        splash.closeSplash();
                        start();
                    }
                });
                timer.setRepeats(true);
                timer.start();
            });
        }
    }

    private static boolean delegateToInstance(final String[] args) {
        try {
            lockSocket = new ServerSocket(LOCK_PORT, 1, InetAddress.getLocalHost());
        } catch (IOException e) {
            try {
                JmxClient client = new JmxClient(JMX_PORT);
                client.invokeOperation("edu.kit.mima.session", "MimaRequestHandler",
                                       "openFile", getFilePath(args));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }
        return true;
    }

    private static void registerWithJmxAgent(final MimaUserInterface frame) {
        MimaRequestHandler handler = new MimaRequestHandler(frame);
        try {
            JmxServer jmxServer = new JmxServer(JMX_PORT);
            jmxServer.start();
            jmxServer.register(handler);
        } catch (JMException e) {
            e.printStackTrace();
        }
    }

    private static String nextMessage() {
        var m = index >= fakeLoadMessages.length ? null : fakeLoadMessages[index];
        index++;
        return m;
    }

    @Contract(value = "null -> !null", pure = true)
    private static String getFilePath(final String[] args) {
        return args != null && args.length >= 1 ? args[0] : "";
    }

    private static void init(final String filePath) {
        LafManager.setDefaultTheme(
                Preferences.getInstance().readString(PropertyKey.THEME).equals("Dark"));
        frame = new MimaUserInterface(filePath);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(final ComponentEvent e) {
                SwingUtilities.invokeLater(() -> PersistenceManager.getInstance().loadStates(frame.getName()));
            }
        });
        logger.setLevel(LogLevel.INFO);
        MimaCoreDefaults.setLogger(logger);
        frame.setLocationRelativeTo(null);
        registerWithJmxAgent(frame);
        initResources();
    }

    private static void initResources() {
        Preferences.getInstance();
    }

    private static void start() {
        frame.setVisible(true);
        frame.toFront();
    }

    @Contract(pure = true)
    public static boolean isInitialized() {
        return frame != null;
    }
}
