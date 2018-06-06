package edu.kit.mima.gui.menu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author Jannis Weis
 * @since 2018
 */
public final class Help extends JFrame {

    private static Help instance;
    private static boolean closed = true;

    /*
     * Construct the Help Screen
     */
    private Help() {
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(500, 600);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closed = true;
                e.getWindow().dispose();
            }
        });
        setTitle("Help");
        setLocationRelativeTo(null);
        setResizable(false);
        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        int fontPoints = 12;
        pane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontPoints));
        pane.setText("\n"
                             + "Instructions:\n"
                             + "\n"
                             + "<a> denotes the value at memory address a\n"
                             + "c is a constant value\n"
                             + "_________________________________________________________\n"
                             + "LDC   c |   c → akku\n"
                             + "LDV   a |   <a> → akku\n"
                             + "STV   a |   akku → <a>\n"
                             + "LDIV  a |   <<a>> → akku\n"
                             + "STIV  a |   akku → <<a>>\n"
                             + "________|_______________________________________________\n"
                             + "RAR     |   rotate akku one place to the right\n"
                             + "NOT     |   bitwise invert akku\n"
                             + "________|_______________________________________________\n"
                             + "ADD   a |   akku + <a> → akku\n"
                             + "AND   a |   akku AND <a> (bitwise) → akku\n"
                             + "OR    a |   akku OR <a> (bitwise) → akku\n"
                             + "XOR   a |   akku XOR <a> (bitwise) → akku\n"
                             + "EQL   a |   if akku = <a>  -1 → akku, else 0 → akku\n"
                             + "        |\n"
                             + "HALT    |   stop the program\n"
                             + "JMP   a |   move instruction pointer to a\n"
                             + "JMN   a |   JMP a if most significant bit in akku is 1\n"
                             + "________|_______________________________________________\n"
                             + "\n"
                             + "Language details:\n"
                             + "\n"
                             + "comments:                   #\"comment\" (only full line comments)\n"
                             + "binary values:              0b...\n"
                             + "memory address references:  $define \"reference\" : \"address value\"\n"
                             + "instruction reference:      \"reference\" : \"insctruction\"\n"
                             + "                            JMP \"reference\"");

        add(pane);
    }

    /**
     * Get an help screen instance
     *
     * @return instance of Help
     */
    public static Help getInstance() {
        if (instance == null || closed)
            instance = new Help();
        return instance;
    }
}
