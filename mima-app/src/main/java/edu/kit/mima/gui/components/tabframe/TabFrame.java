package edu.kit.mima.gui.components.tabframe;

import com.bulenkov.iconloader.util.EmptyIcon;
import edu.kit.mima.gui.components.alignment.Alignment;
import edu.kit.mima.gui.persist.PersistableComponent;
import edu.kit.mima.gui.persist.PersistenceInfo;
import edu.kit.mima.gui.persist.PersistenceManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Frame that supports plugin components.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class TabFrame extends PersistableComponent {

    public TabFrame() {
        setUI(new TabFrameUI());
    }

    /**
     * Get the content pane.
     *
     * @return the content pane.
     */
    public JComponent getContentPane() {
        var content = ((TabFrameLayout) getLayout()).getTabFrameContent().getContentPane();
        if (content == null) {
            content = new JPanel();
            setContentPane(content);
        }
        return content;
    }

    public void setContentPane(final JComponent c) {
        ((TabFrameLayout) getLayout()).setContent(c);
    }

    @Override
    public void updateUI() {
        super.updateUI();
        ((TabFrameUI) getUI()).updateUI();
    }

    @NotNull
    @Override
    public Insets getInsets() {
        return new Insets(0, 0, 0, 0);
    }

    @NotNull
    @Override
    public Insets getInsets(@NotNull final Insets insets) {
        insets.set(0, 0, 0, 0);
        return insets;
    }

    public void insertTab(
            @NotNull final PopupComponent c,
            final String title,
            final Icon icon,
            final Alignment a,
            final int index) {
        ((TabFrameLayout) getLayout()).insertTab(c, title, icon, a, index);
    }

    public void insertTab(
            @NotNull final PopupComponent c, final String title, final Alignment a, final int index) {
        insertTab(c, title, new EmptyIcon(0, 0), a, index);
    }

    public void insertTab(@NotNull final PopupComponent c, final Alignment a, final int index) {
        insertTab(c, "", a, index);
    }

    public void addTab(
            final PopupComponent c, final String title, final Icon icon, final Alignment a) {
        ((TabFrameLayout) getLayout()).addTab(c, title, icon, a);
    }

    public void addTab(final PopupComponent c, final String title, final Alignment a) {
        addTab(c, title, new EmptyIcon(0, 0), a);
    }

    public void toggleTab(final Alignment a, final int index, final boolean enabled) {
        var layout = ((TabFrameLayout) getLayout());
        var tabs = ((TabFrameLayout) getLayout()).tabsForAlignment(a);
        if (tabs.size() <= index) {
            return;
        }
        var compAtIndex = tabs.get(index);
        if (compAtIndex.isSelected() != enabled) {
            compAtIndex.setSelected(enabled);
            layout.notifySelectChange(compAtIndex);
            layout.compsForAlignment(a).get(index).setFocus(false);
        }
    }

    public void addTab(final PopupComponent c, final Alignment a) {
        addTab(c, "", a);
    }

    @Override
    public PersistenceInfo saveState() {
        var cont = ((TabFrameLayout) getLayout()).getTabFrameContent();
        for (var a : Alignment.values()) {
            var enabled = cont.isEnabled(a);
            var index = ((TabFrameLayout) getLayout()).lastSelectedIndex(a);
            persistenceInfo.putValue(a.toString(), enabled);
            persistenceInfo.putValue(a.toString() + ".index", index);
        }
        var info = ((TabFrameLayout) getLayout()).getTabFrameContent().saveState();
        info.merge(persistenceInfo);
        return info;
    }


    @Override
    public void loadState(final PersistenceInfo info) {
        for (var a : Alignment.values()) {
            var enabled = info.getBoolean(a.toString(), false);
            var index = info.getInt(a.toString() + ".index", 0);
            toggleTab(a, index, enabled);
        }
        var cont = ((TabFrameLayout) getLayout()).getTabFrameContent();
        cont.loadState(PersistenceManager.getInstance().getStates(cont));
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean isPersistable() {
        return persistable;
    }

    @Override
    public void setPersistable(final boolean persistable, final String identifier) {
        super.setPersistable(persistable, identifier);
        ((TabFrameLayout) getLayout()).getTabFrameContent().setPersistable(persistable, identifier);
    }
}
