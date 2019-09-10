package edu.kit.mima.session;

import com.j256.simplejmx.common.JmxOperation;
import com.j256.simplejmx.common.JmxResource;
import edu.kit.mima.annotations.ReflectionCall;
import edu.kit.mima.app.MimaUserInterface;
import org.jetbrains.annotations.Contract;

/**
 * Handler for requests to the application.
 *
 * @author Jannis Weis
 * @since 2019
 */
@JmxResource(domainName = "edu.kit.mima.session", beanName = "MimaRequestHandler")
public class MimaRequestHandler {

    private final MimaUserInterface frame;

    @Contract(pure = true)
    public MimaRequestHandler(final MimaUserInterface frame) {
        this.frame = frame;
    }

    @ReflectionCall
    @JmxOperation(description = "Open File")
    public void openFile(final String path) {
        frame.openFile(path);
    }
}
