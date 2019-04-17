package edu.kit.mima.gui.components.tabframe;

import edu.kit.mima.api.annotations.ReflectionCall;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import java.awt.Graphics;

/**
 * UI class for {@link TabFrame}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class TabFrameUI extends ComponentUI {

    private TabFrameLayout layout;

    @NotNull
    @Contract("_ -> new")
    @ReflectionCall
    public static ComponentUI createUI(JComponent c) {
        return new TabFrameUI();
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        TabFrame tabFrame = (TabFrame) c;
        layout = new TabFrameLayout(tabFrame);
        c.setLayout(layout);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        layout.layoutContainer(c);
        super.paint(g, c);
        g.setColor(UIManager.getColor("Border.line1"));
        //Bottom
        var bottomRect = layout.bottomRect;
        g.drawLine(bottomRect.x, bottomRect.y,
                   bottomRect.x + bottomRect.width - 1, bottomRect.y);
        //Top
        var topRect = layout.topRect;
        g.drawLine(topRect.x, topRect.y + topRect.height - 1,
                   topRect.x + topRect.width - 1, topRect.y + topRect.height - 1);
        //Left
        var leftRect = layout.leftRect;
        g.drawLine(leftRect.x + leftRect.width - 1, leftRect.y,
                   leftRect.x + leftRect.width - 1, leftRect.y + leftRect.height - 1);
        //Right
        var rightRect = layout.rightRect;
        g.drawLine(rightRect.x, rightRect.y, rightRect.x, rightRect.y + rightRect.height - 1);

        if (topRect.height == 0) {
            g.drawLine(leftRect.x, leftRect.y, leftRect.x + leftRect.width, leftRect.y);
            g.drawLine(rightRect.x, rightRect.y, rightRect.x + rightRect.width, rightRect.y);
        }
        if (bottomRect.height == 0) {
            g.drawLine(leftRect.x, leftRect.y + leftRect.height - 1,
                       leftRect.x + leftRect.width, leftRect.y + leftRect.height - 1);
            g.drawLine(rightRect.x, rightRect.y + rightRect.height - 1,
                       rightRect.x + rightRect.width, rightRect.y + rightRect.height - 1);
        }
    }


    @Override
    public int getBaseline(JComponent c, int width, int height) {
        super.getBaseline(c, width, height);
        return 0;
    }

}
