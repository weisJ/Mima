package edu.kit.mima.gui.laf;

import com.weis.darklaf.ui.scrollpane.DarkScrollBarUI;
import edu.kit.mima.annotations.ReflectionCall;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 * Light version of {@link DarkScrollBarUI}.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class LightScrollBarUI extends DarkScrollBarUI {

    @NotNull
    @Contract(" -> new")
    @ReflectionCall
    public static BasicScrollBarUI createNormal() {
        return new LightScrollBarUI();
    }

    @NotNull
    @Contract("_ -> new")
    @ReflectionCall
    public static ComponentUI createUI(final JComponent c) {
        return new LightScrollBarUI();
    }

    @Override
    public void installUI(final JComponent c) {
        super.installUI(c);
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    protected Color getTrackColor() {
        return new Color(217, 217, 217);
    }

    protected void paintMaxiThumb(@NotNull final Graphics2D g, @NotNull final Rectangle thumbBounds) {
//        final var c = g.getComposite();
//        g.setComposite(COMPOSITE.derive(THUMB_ALPHA));
//        var thumbRect = calculateThumbRect(thumbBounds);
//        if (isVertical()) {
//            thumbRect.x = 0;
//            thumbRect.width = thumbBounds.width - 1;
//        } else {
//            thumbRect.y = 0;
//            thumbRect.height = thumbBounds.height - 1;
//        }
//        g.setPaint(getThumbColor());
//        g.fillRect(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
//        g.setPaint(getThumbBorderColor());
//        g.drawRect(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
//        g.setComposite(c);
    }

    @NotNull
    @Contract(pure = true)
    private Color getThumbBorderColor() {
        return new Color(212, 212, 212);
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    private Color getThumbColor() {
        return new Color(217, 217, 217);
    }
}
