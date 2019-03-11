package edu.kit.mima.gui.menu;

import edu.kit.mima.core.parsing.token.Tuple;
import edu.kit.mima.core.parsing.token.ValueTuple;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class CardPanelItem {

    private final CardPanelBuilder builder;
    private final JPanel panel;
    private final String title;
    private final List<Tuple<String, JComponent>> settings;

    /*default*/ CardPanelItem(String title, CardPanelBuilder builder, boolean alignLeft) {
        this.builder = builder;
        this.title = title;
        settings = new ArrayList<>();
        this.panel = new JPanel();
        if (alignLeft) {
            this.panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        }
    }

    public CardPanelItem addItem(String title, boolean alignLeft) {
        complete();
        return builder.nextItem(title, this, alignLeft);
    }

    public CardPanelItem addItem(String title) {
        return addItem(title, true);
    }

    public CardPanelItem addSetting(String description, JComponent component) {
        settings.add(new ValueTuple<>(description, component));
        return this;
    }

    public CardPanelItem addSetting(JComponent component) {
        return addSetting(null, component);
    }

    private void complete() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        for (var t : settings) {
            JPanel p;
            if (t.getFirst() == null) {
                p = new JPanel(new GridLayout(1, 1));
                p.add(t.getSecond());
            } else {
                p = new JPanel(new GridLayout(1, 2));
                p.add(new JLabel(t.getFirst()));
                p.add(t.getSecond());
            }
            content.add(p, BorderLayout.LINE_START);
        }
        panel.add(content);
    }

    public void addToComponent(Container parent) {
        complete();
        builder.nextItem("Last", this, true);
        builder.addToComponent(parent);
    }

    public JPanel getPanel() {
        return panel;
    }

    public String getTitle() {
        return title;
    }
}
