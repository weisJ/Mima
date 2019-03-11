package edu.kit.mima.gui.laf;

import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import java.awt.Color;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class LightMetalTheme extends DefaultMetalTheme {
    public static final ColorUIResource primary1 = new ColorUIResource(10, 36, 106);
    private static final ColorUIResource darkGray = new ColorUIResource(132, 130, 132);
    private static final ColorUIResource white = new ColorUIResource(255, 255, 255);
    private static final ColorUIResource darkBlue = new ColorUIResource(82, 108, 164);
    //      private static ColorUIResource lightGray = new ColorUIResource(214, 211, 206);
    private static final ColorUIResource lightGray = new ColorUIResource(214, 214, 214);
    private static final ColorUIResource primary2 = new ColorUIResource(91, 135, 206);
    private static final ColorUIResource primary3 = new ColorUIResource(166, 202, 240);

    @Override
    public String getName() {
        return "Light Metal Theme";
    }

    @Override
    public ColorUIResource getControl() {
        return lightGray;
    }

    @Override
    public ColorUIResource getSeparatorBackground() {
        return white;
    }

    @Override
    public ColorUIResource getSeparatorForeground() {
        return darkGray;
    }

    @Override
    public ColorUIResource getMenuBackground() {
        return lightGray;
    }

    @Override
    public ColorUIResource getMenuSelectedBackground() {
        return darkBlue;
    }

    @Override
    public ColorUIResource getMenuSelectedForeground() {
        return white;
    }

    @Override
    public ColorUIResource getAcceleratorSelectedForeground() {
        return white;
    }

    @Override
    public ColorUIResource getFocusColor() {
        return new ColorUIResource(Color.black);
    }

    @Override
    protected ColorUIResource getPrimary1() {
        return primary1;
    }

    @Override
    protected ColorUIResource getPrimary2() {
        return primary2;
    }

    @Override
    protected ColorUIResource getPrimary3() {
        return primary3;
    }
}
