package edu.kit.mima.gui.components.fontchooser.panes;

import javax.swing.JTextField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.TreeSet;

public class SearchListener extends KeyAdapter {

    private final Collection<String> fontFamilyNames = new TreeSet<>();

    private final FamilyPane familyPane;

    public SearchListener(FamilyPane familyPane) {
        this.familyPane = familyPane;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        JTextField searchField = (JTextField) e.getSource();
        String searchString = searchField.getText().toLowerCase(Locale.ENGLISH);
        Optional<String> first = fontFamilyNames.stream()
                .filter(family -> family.toLowerCase(Locale.ENGLISH).contains(searchString))
                .findFirst();
        first.ifPresent(familyPane::setSelectedFamily);
    }

    public void addFamilyName(String name) {
        fontFamilyNames.add(name);
    }
}
