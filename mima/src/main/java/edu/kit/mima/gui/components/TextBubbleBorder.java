package edu.kit.mima.gui.components;

import javax.swing.border.AbstractBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class TextBubbleBorder extends AbstractBorder {

    private static final long serialVersionUID = 1L;
    private Alignment pointerSide = Alignment.NORTH;
    private RenderingHints hints;
    private Color color;
    private int thickness;
    private int radius;
    private int pointerSize;
    private Insets insets;
    private BasicStroke stroke;
    private int strokePad;
    private double pointerPadPercent = 0.5;


    /**
     * Create new TextBubbleBorder with given colour.
     *
     * @param color color of border
     */
    public TextBubbleBorder(Color color) {
        this(color, 2, 4, 0);
    }

    /**
     * Create new TextBubbleBorder.
     *
     * @param color       Colour of bubble.
     * @param thickness   Line thickness of border.
     * @param radius      corner radius of border.
     * @param pointerSize size of pointer.
     *                    You can set this size to 0 to achieve no pointer, but it is not
     *                    desirable. The appropriate method for this is to set using
     *                    {@link TextBubbleBorder#setPointerSide(Alignment)} to {@link Alignment#CENTER}
     */
    public TextBubbleBorder(Color color, int thickness, int radius, int pointerSize) {
        this.color = color;
        this.thickness = thickness;
        this.radius = radius;
        this.pointerSize = pointerSize;

        hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        insets = new Insets(0, 0, 0, 0);
        setThickness(thickness);
    }

    /**
     * Get border Colour.
     *
     * @return border colour
     */
    public Color getColor() {
        return color;
    }

    /**
     * Set the border colour.
     *
     * @param color border colour
     * @return this
     */
    public TextBubbleBorder setColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * Get the percentage for pointer padding where
     * 0 - means left/top
     * 1 - means right/bottom
     *
     * @return percentage of padding
     */
    public double getPointerPadPercent() {
        return pointerPadPercent;
    }

    /**
     * Set the percentage for pointer padding where
     * 0 - means left/top
     * 1 - means right/bottom
     * Clips at 0 and 1
     *
     * @param percent percentage between 0 and 1
     * @return this
     */
    public TextBubbleBorder setPointerPadPercent(double percent) {
        this.pointerPadPercent = percent > 1 ? 1 : percent;
        pointerPadPercent = pointerPadPercent < 0 ? 0 : pointerPadPercent;
        return this;
    }


    /**
     * Get the border thickness;
     *
     * @return thickness of border.
     */
    public int getThickness() {
        return thickness;
    }

    /**
     * Set the border thickness
     *
     * @param n new thickness
     * @return this
     */
    public TextBubbleBorder setThickness(int n) {
        thickness = n < 0 ? 0 : n;
        stroke = new BasicStroke(thickness);
        strokePad = thickness / 2;
        return setPointerSize(pointerSize);
    }

    /**
     * Get the corner radius.
     *
     * @return radius of corners
     */
    public int getRadius() {
        return radius;
    }

    /**
     * Set the corner radius.
     *
     * @param radius radius of corner.
     * @return this
     */
    public TextBubbleBorder setRadius(int radius) {
        this.radius = radius;
        return setPointerSize(pointerSize);
    }

    /**
     * Get the pointer size
     *
     * @return size of pointer.
     */
    public int getPointerSize() {
        return pointerSize;
    }

    /**
     * Set the pointer size
     * Clips at 1.
     *
     * @param size size of pointer.
     * @return this
     */
    public TextBubbleBorder setPointerSize(int size) {
        pointerSize = size < 0 ? 0 : size;
        int pad = radius / 2 + strokePad;
        int pointerSidePad = pad + pointerSize + strokePad;
        int left, right, bottom, top;
        left = right = bottom = top = pad;
        switch (pointerSide) {
            case NORTH:
            case NORTH_WEST:
            case NORTH_EAST:
                top = pointerSidePad;
                break;
            case WEST:
                left = pointerSidePad;
                break;
            case EAST:
                right = pointerSidePad;
                break;
            case SOUTH:
            case SOUTH_WEST:
            case SOUTH_EAST:
                bottom = pointerSidePad;
                break;
            case CENTER:
                break;
        }
        insets.set(top, left, bottom, right);
        return this;
    }

    /**
     * Get the Alignment the pointer follows.
     * Default is {@link Alignment#NORTH}
     *
     * @return alignment
     */
    public Alignment getPointerSide() {
        return pointerSide;
    }

    /**
     * Set the alignment for the pointer.
     * Not there is no difference between
     * {@link Alignment#NORTH}, {@link Alignment#NORTH_EAST} and {@link Alignment#NORTH_WEST}
     * as well as
     * {@link Alignment#SOUTH}, {@link Alignment#SOUTH_EAST} and {@link Alignment#SOUTH_WEST}
     * {@link Alignment#CENTER} results in n pointer.
     *
     * @param side direction in which the pointer should point.
     * @return this.
     */
    public TextBubbleBorder setPointerSide(Alignment side) {
        this.pointerSide = side;
        return setPointerSize(pointerSize);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return insets;
    }


    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        return getBorderInsets(c);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g;
        RoundRectangle2D.Double bubble;
        int rx, ry, rw, rh;
        rx = ry = strokePad;
        rw = width - thickness;
        rh = height - thickness;
        switch (pointerSide) {
            case WEST:
                rx += pointerSize;
            case EAST:
                rw -= pointerSize;
                break;
            case NORTH:
            case NORTH_WEST:
            case NORTH_EAST:
                ry += pointerSize;
                /*fallthrough*/
            case SOUTH:
            case SOUTH_WEST:
            case SOUTH_EAST:
                rh -= pointerSize;
                break;
            case CENTER:
                break;
        }
        bubble = new RoundRectangle2D.Double(rx, ry, rw, rh, radius, radius);
        int pointerPad;

        if (pointerSide == Alignment.WEST || pointerSide == Alignment.EAST) {
            pointerPad = pointerSize + (int) (pointerPadPercent * (height - radius * 2 - 3 * pointerSize));
        } else if (pointerSide == Alignment.CENTER) {
            pointerPad = 0;
        } else {
            pointerPad = pointerSize + (int) (pointerPadPercent * (width - radius * 2 - 3 * pointerSize));
        }

        int basePad = strokePad + radius + pointerPad;
        int widthPad = pointerSize / 2;

        Polygon pointer = new Polygon();
        switch (pointerSide) {
            case WEST:
                pointer.addPoint(rx, basePad - widthPad);// top
                pointer.addPoint(rx, basePad + pointerSize + widthPad);// bottom
                pointer.addPoint(strokePad, basePad + pointerSize / 2);
                break;
            case EAST:
                pointer.addPoint(rw, basePad - widthPad);// top
                pointer.addPoint(rw, basePad + pointerSize + widthPad);// bottom
                pointer.addPoint(width - strokePad, basePad + pointerSize / 2);
                break;
            case NORTH_WEST:
            case NORTH_EAST:
            case NORTH:
                pointer.addPoint(basePad - widthPad, ry);// left
                pointer.addPoint(basePad + pointerSize + widthPad, ry);// right
                pointer.addPoint(basePad + (pointerSize / 2), strokePad);
                break;
            case SOUTH_WEST:
            case SOUTH_EAST:
            case SOUTH:
                pointer.addPoint(basePad - widthPad, rh);// left
                pointer.addPoint(basePad + pointerSize + widthPad, rh);// right
                pointer.addPoint(basePad + (pointerSize / 2), height - strokePad);
                break;
            case CENTER:
                break;
        }

        Area area = new Area(bubble);
        area.add(new Area(pointer));
        g2.setRenderingHints(hints);

        g2.setColor(c.getBackground());
        g2.fill(area);
        g2.setColor(color);
        g2.setStroke(stroke);
        g2.draw(area);
    }
}