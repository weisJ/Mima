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


    public TextBubbleBorder(Color color) {
        this(color, 2, 4, 0);
    }

    public TextBubbleBorder(Color color, int thickness, int radius, int pointerSize) {
        this.color = color;
        this.thickness = thickness;
        this.radius = radius;
        this.pointerSize = pointerSize;

        hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        insets = new Insets(0, 0, 0, 0);
        setThickness(thickness);
    }

    public Color getColor() {
        return color;
    }

    public TextBubbleBorder setColor(Color color) {
        this.color = color;
        return this;
    }

    public double getPointerPadPercent() {
        return pointerPadPercent;
    }

    public TextBubbleBorder setPointerPadPercent(double percent) {
        this.pointerPadPercent = percent > 1 ? 1 : percent;
        pointerPadPercent = pointerPadPercent < 0 ? 0 : pointerPadPercent;
        return this;
    }


    public int getThickness() {
        return thickness;
    }

    public TextBubbleBorder setThickness(int n) {
        thickness = n < 0 ? 0 : n;
        stroke = new BasicStroke(thickness);
        strokePad = thickness / 2;
        setPointerSize(pointerSize);
        return this;
    }

    public int getRadius() {
        return radius;
    }

    public TextBubbleBorder setRadius(int radius) {
        this.radius = radius;
        setPointerSize(pointerSize);
        return this;
    }

    public int getPointerSize() {
        return pointerSize;
    }

    public TextBubbleBorder setPointerSize(int size) {
        pointerSize = size < 0 ? 0 : size;
        int pad = radius / 2 + strokePad;
        int pointerSidePad = pad + pointerSize + strokePad;
        int left, right, bottom, top;
        left = right = bottom = top = pad;
        switch (pointerSide) {
            case NORTH:
                top = pointerSidePad;
                break;
            case WEST:
            case NORTH_WEST:
            case SOUTH_WEST:
                left = pointerSidePad;
                break;
            case EAST:
            case NORTH_EAST:
            case SOUTH_EAST:
                right = pointerSidePad;
                break;
            case SOUTH:
                bottom = pointerSidePad;
                break;
            case CENTER:
                break;
        }
        insets.set(top, left, bottom, right);
        return this;
    }

    public Alignment getPointerSide() {
        return pointerSide;
    }

    public TextBubbleBorder setPointerSide(Alignment side) {
        this.pointerSide = side;
        setPointerSize(pointerSize);
        return this;
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
        Polygon pointer = new Polygon();
        int rx, ry, rw, rh;
        rx = ry = strokePad;
        rw = width - thickness;
        rh = height - thickness;
        switch (pointerSide) {
            case WEST:
            case NORTH_WEST:
            case SOUTH_WEST:
                rx += pointerSize;
            case EAST:
            case NORTH_EAST:
            case SOUTH_EAST:
                rw -= pointerSize;
                break;
            case NORTH:
                ry += pointerSize;
            case SOUTH:
                rh -= pointerSize;
                break;
            case CENTER:
                break;
        }
        bubble = new RoundRectangle2D.Double(rx, ry, rw, rh, radius, radius);
        int pointerPad;

        if (pointerSide == Alignment.NORTH || pointerSide == Alignment.SOUTH) {

        } else if (pointerSide == Alignment.CENTER) {

        } else {

        }

        if (pointerSide == Alignment.WEST || pointerSide == Alignment.EAST) {
            pointerPad = (int) (pointerPadPercent * (height - radius * 2 - pointerSize));
        } else {
            pointerPad = (int) (pointerPadPercent * (width - radius * 2 - pointerSize));
        }
        int basePad = strokePad + radius + pointerPad;
        int widthPad = pointerSize / 2;
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
            case NORTH:
                pointer.addPoint(basePad - widthPad, ry);// left
                pointer.addPoint(basePad + pointerSize + widthPad, ry);// right
                pointer.addPoint(basePad + (pointerSize / 2), strokePad);
                break;
            default: /*fallthrough*/
            case SOUTH:
                pointer.addPoint(basePad - widthPad, rh);// left
                pointer.addPoint(basePad + pointerSize + widthPad, rh);// right
                pointer.addPoint(basePad + (pointerSize / 2), height - strokePad);
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