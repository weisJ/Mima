package edu.kit.mima.gui.menu;

import edu.kit.mima.api.util.Tuple;
import edu.kit.mima.api.util.ValueTuple;
import org.jetbrains.annotations.NotNull;

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
 * CardPanelItem for {@link CardPanelBuilder}.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class CardPanelItem {

    private final CardPanelBuilder builder;
    @NotNull private final JPanel panel;
    private final String title;
    @NotNull private final List<Tuple<String, JComponent>> settings;

    /*default*/ CardPanelItem(final String title,
                              final CardPanelBuilder builder,
                              final boolean alignLeft) {
        this.builder = builder;
        this.title = title;
        settings = new ArrayList<>();
        this.panel = new JPanel();
        if (alignLeft) {
            this.panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        }
    }

    /**
     * Create new item.
     *
     * @param title     title of item.
     * @param alignLeft whether it is left aligned.
     * @return new {@link CardPanelItem}
     */
    @NotNull
    public CardPanelItem addItem(final String title, final boolean alignLeft) {
        complete();
        return builder.nextItem(title, this, alignLeft);
    }

    /**
     * Create new item.
     *
     * @param title title of item.
     * @return new {@link CardPanelItem}
     */
    @NotNull
    public CardPanelItem addItem(final String title) {
        return addItem(title, true);
    }

    /**
     * Add a setting.
     *
     * @param description description of setting.
     * @param component   component of setting.
     * @return this
     */
    @NotNull
    public CardPanelItem addSetting(final String description, final JComponent component) {
        settings.add(new ValueTuple<>(description, component));
        return this;
    }

    public CardPanelBuilder addItem(final String title, final JComponent c) {
        return builder.addItem(title, c);
    }

    /**
     * Add a setting.
     *
     * @param component component of setting.
     * @return this
     */
    @NotNull
    public CardPanelItem addSetting(final JComponent component) {
        return addSetting(null, component);
    }

    private void complete() {
        final JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        for (final var t : settings) {
            final JPanel p;
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

    /**
     * Add the CardPanel to the given component.
     *
     * @param parent component to add to.
     */
    public void addToComponent(@NotNull final Container parent) {
        complete();
        builder.nextItem("Last", this, true);
        builder.addToComponent(parent);
    }

    /**
     * Get the panel.
     *
     * @return panel of item.
     */
    @NotNull
    public JPanel getPanel() {
        return panel;
    }

    /**
     * Get the title.
     *
     * @return title of item.
     */
    public String getTitle() {
        return title;
    }
}
