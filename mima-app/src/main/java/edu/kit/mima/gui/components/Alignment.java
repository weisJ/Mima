package edu.kit.mima.gui.components;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.function.BiFunction;

/**
 * Alignment for GUI elements.
 *
 * @author Jannis Weis
 * @since 2018
 */
public enum Alignment {
    NORTH((toAlign, alignAt) -> {
        return new Point(alignAt.x - toAlign.width / 2, alignAt.y - toAlign.height);
    }, (toAlign, bounds) -> {
        return new Point(bounds.x + bounds.width / 2 - toAlign.width / 2, bounds.y);
    }, (toAlign, bounds) -> {
        return new Point(bounds.x + bounds.width / 2 - toAlign.width / 2,
                         bounds.y - toAlign.height);
    }),
    NORTH_EAST((toAlign, alignAt) -> {
        return new Point(alignAt.x, alignAt.y - toAlign.height);
    }, (toAlign, bounds) -> {
        return new Point(bounds.x + bounds.width - toAlign.width, bounds.y);
    }, (toAlign, bounds) -> {
        return new Point(bounds.x + bounds.width - toAlign.width, bounds.y - toAlign.height);
    }),
    EAST((toAlign, alignAt) -> {
        return new Point(alignAt.x, alignAt.y - toAlign.height / 2);
    }, (toAlign, bounds) -> {
        return new Point(bounds.x + bounds.width - toAlign.width,
                         bounds.y + bounds.height / 2 - toAlign.height / 2);
    }, (toAlign, bounds) -> {
        return new Point(bounds.x, bounds.y + bounds.height / 2 - toAlign.height / 2);
    }),
    SOUTH_EAST((toAlign, alignAt) -> {
        return new Point(alignAt.x, alignAt.y);
    }, (toAlign, bounds) -> {
        return new Point(bounds.x + bounds.width - toAlign.width,
                         bounds.y + bounds.height - toAlign.height);
    }, (toAlign, bounds) -> {
        return new Point(bounds.x + bounds.width, bounds.y + bounds.height);
    }),
    SOUTH((toAlign, alignAt) -> {
        return new Point(alignAt.x - toAlign.width / 2, alignAt.y);
    }, (toAlign, bounds) -> {
        return new Point(bounds.x + bounds.width / 2 - toAlign.width / 2,
                         bounds.y + bounds.height - toAlign.height);
    }, (toAlign, bounds) -> {
        return new Point(bounds.x + bounds.width / 2 - toAlign.width / 2,
                         bounds.y + bounds.height);
    }),
    SOUTH_WEST((toAlign, alignAt) -> {
        return new Point(alignAt.x - toAlign.width, alignAt.y);
    }, (toAlign, bounds) -> {
        return new Point(bounds.x, bounds.y + bounds.height - toAlign.height);
    }, (toAlign, bounds) -> {
        return new Point(bounds.x - toAlign.width, bounds.y + bounds.height);
    }),
    WEST((toAlign, alignAt) -> {
        return new Point(alignAt.x - toAlign.width, alignAt.y - toAlign.height / 2);
    }, (toAlign, bounds) -> {
        return new Point(bounds.x, bounds.y + bounds.height / 2 - toAlign.height / 2);
    }, (toAlign, bounds) -> {
        return new Point(bounds.x - toAlign.width,
                         bounds.y + bounds.height / 2 - toAlign.height / 2);
    }),
    NORTH_WEST((toAlign, alignAt) -> {
        return new Point(alignAt.x - toAlign.width, alignAt.y - toAlign.height);
    }, (toAlign, bounds) -> {
        return new Point(bounds.x, bounds.y);
    }, (toAlign, bounds) -> {
        return new Point(bounds.x - toAlign.width, bounds.y - toAlign.height);
    }),
    CENTER((toAlign, alignAt) -> alignAt, (toAlign, bounds) -> {
        return new Point(bounds.x + bounds.width / 2 - toAlign.width / 2,
                         bounds.y + bounds.height / 2 - toAlign.height / 2);
    }, (toAlign, bounds) -> {
        return new Point(bounds.x + bounds.width / 2 - toAlign.width / 2,
                         bounds.y + bounds.height / 2 - toAlign.height / 2);
    });

    private final BiFunction<Dimension, Point, Point> relativePos;
    private final BiFunction<Dimension, Rectangle, Point> alignInside;
    private final BiFunction<Dimension, Rectangle, Point> alignOutside;

    @Contract(pure = true)
    Alignment(BiFunction<Dimension, Point, Point> relativePos,
              BiFunction<Dimension, Rectangle, Point> alignInside,
              BiFunction<Dimension, Rectangle, Point> alignOutside) {
        this.relativePos = relativePos;
        this.alignInside = alignInside;
        this.alignOutside = alignOutside;
    }

    /**
     * Get fitting alignment.
     *
     * @param point       point to align at.
     * @param size        Size of rectangle to align.
     * @param outerBounds outer boundaries to align in.
     * @param hint        preferred alignment.
     * @return fitting alignment. If none is found wit is defaulted to {@link Alignment#CENTER}.
     */
    public static Alignment getAlignment(@NotNull final Point point,
                                         @NotNull final Dimension size,
                                         @NotNull final Rectangle outerBounds,
                                         @NotNull final Alignment hint) {
        if (hint.canBeAligned(point, size, outerBounds)) {
            return hint;
        }
        for (var alignment : Alignment.values()) {
            if (alignment != Alignment.CENTER && alignment != hint
                    && alignment.canBeAligned(point, size, outerBounds)) {
                return alignment;
            }
        }
        return Alignment.CENTER;
    }

    /**
     * Get the opposite alignment.
     *
     * @return Alignment opposite on the compass.
     */
    @Contract(pure = true)
    @NotNull
    public Alignment opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case NORTH_EAST -> SOUTH_WEST;
            case EAST -> WEST;
            case SOUTH_EAST -> NORTH_WEST;
            case SOUTH -> NORTH;
            case SOUTH_WEST -> NORTH_EAST;
            case WEST -> EAST;
            case NORTH_WEST -> SOUTH_EAST;
            case CENTER -> CENTER;
        };
    }

    /**
     * Get the relative Position of Rectangle to Point with respect to the alignment.
     *
     * @param toAlign size of Rectangle to align.
     * @param alignAt point to align at.
     * @return top/left position of aligned rectangle
     */
    public Point relativePos(@NotNull final Dimension toAlign, @NotNull final Point alignAt) {
        return this.relativePos.apply(toAlign, alignAt);
    }


    /**
     * Check wheter the given Rectangle can be aligned at point inside boundaries.
     *
     * @param point       point to align at.
     * @param size        size of rectangle to align.
     * @param outerBounds boundaries.
     * @return true if can be aligned.
     */
    public boolean canBeAligned(@NotNull final Point point,
                                @NotNull final Dimension size,
                                @NotNull final Rectangle outerBounds) {
        var p = relativePos(size, point);
        return p.x >= outerBounds.x && p.y >= outerBounds.y
                && p.x + size.width < outerBounds.x + outerBounds.width
                && p.y + size.height < outerBounds.x + outerBounds.height;
    }

    /**
     * Align Rectangle inside other rectangle with respect to the alignment.
     *
     * @param toAlign     size of rectangle to align
     * @param outerBounds bounds of outer rectangle
     * @return top/left point of aligned rectangle
     */
    public Point alignInside(@NotNull final Dimension toAlign,
                             @NotNull final Rectangle outerBounds) {
        return this.alignInside.apply(toAlign, outerBounds);
    }

    /**
     * Align Rectangle outside other rectangle with respect to the alignment.
     *
     * @param toAlign     size of rectangle to align
     * @param innerBounds bounds of inside rectangle
     * @return top/left point of aligned rectangle
     */
    public Point alignOutside(@NotNull final Dimension toAlign,
                              @NotNull final Rectangle innerBounds) {
        return this.alignOutside.apply(toAlign, innerBounds);

    }
}
