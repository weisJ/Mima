package edu.kit.mima.gui.components.tabframe;

import com.bulenkov.iconloader.util.EmptyIcon;
import edu.kit.mima.gui.components.alignment.Alignment;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Insets;

/**
 * Frame that supports plugin components.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class TabFrame extends JComponent {

    private JComponent content;

    public TabFrame() {
        setUI(new TabFrameUI());
    }

    public JComponent getContentPane() {
        if (content == null) {
            setContentPane(new JPanel());
        }
        return content;
    }

    public void setContentPane(final JComponent c) {
        ((TabFrameLayout) getLayout()).setContent(c);
    }

    @Override
    public Insets getInsets(Insets insets) {
        insets.set(0, 0, 0, 0);
        return insets;
    }

    @Override
    public Insets getInsets() {
        return new Insets(0, 0, 0, 0);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        getUI().paint(g, this);
    }

    public void insertTab(final PopupComponent c, final String title, final Icon icon,
                          final Alignment a, final int index) {
        ((TabFrameLayout) getLayout()).insertTab(c, title, icon, a, index);
    }

    public void insertTab(final PopupComponent c, final String title,
                          final Alignment a, final int index) {
        insertTab(c, title, new EmptyIcon(0, 0), a, index);
    }

    public void insertTab(final PopupComponent c,
                          final Alignment a, final int index) {
        insertTab(c, "", a, index);
    }

    public void addTab(final PopupComponent c, final String title,
                       final Icon icon, final Alignment a) {
        ((TabFrameLayout) getLayout()).addTab(c, title, icon, a);
    }


    public void addTab(final PopupComponent c, final String title, final Alignment a) {
        addTab(c, title, new EmptyIcon(0, 0), a);
    }

    public void addTab(final PopupComponent c, final Alignment a) {
        addTab(c, "", a);
    }

}
