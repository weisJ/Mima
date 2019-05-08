import edu.kit.mima.gui.components.AlignPolicy;
import edu.kit.mima.gui.components.tooltip.DefaultTooltipWindow;
import edu.kit.mima.gui.components.tooltip.TooltipComponent;
import edu.kit.mima.gui.components.tooltip.TooltipConstants;
import edu.kit.mima.gui.laf.LafManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class TooltipDemo {

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(
                () -> {
                    LafManager.setDefaultTheme(true);
                    final var frame = new JFrame();
                    frame.setSize(500, 500);
                    frame.setLocationRelativeTo(null);
                    frame.setLayout(new GridLayout(8, 8));
                    for (int i = 0; i < 64; i++) {
                        final JPanel button =
                                new JPanel() {
                                    @Override
                                    protected void paintComponent(@NotNull final Graphics g) {
                                        super.paintComponent(g);
                                        // Draw center dot
                                        g.setColor(Color.RED);
                                        g.fillRect(getWidth() / 2, getHeight() / 2, 1, 1);
                                    }
                                };
                        button.setBackground(Color.WHITE);
                        button.setBorder(new LineBorder(Color.RED));
                        new TooltipComponent<>(
                                button,
                                new DefaultTooltipWindow(
                                        "Hallo Laaaaaaaaaaaaaaaaaaaaaanger "
                                                + " \n "
                                                + "Test1 \n Tes 2 \n\n\n\nHoher Text"),
                                600,
                                TooltipConstants.PERSISTENT,
                                AlignPolicy.COMPONENT_BOTH)
                                .setActive(true);
                        frame.add(button);
                    }
                    frame.setBackground(Color.WHITE);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setVisible(true);
                });
    }
}
