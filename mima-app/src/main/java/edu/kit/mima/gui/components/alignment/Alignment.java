package edu.kit.mima.gui.components.alignment;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.function.BiFunction;

import static edu.kit.mima.gui.components.alignment.AlignmentHelper.*;

/**
 * Alignment for GUI elements.
 *
 * @author Jannis Weis
 * @since 2018
 */
public enum Alignment {
    NORTH(AlignmentHelper.align(HOR_CENTER, VERT_TOP),
          AlignmentHelper.alignRelative(HOR_CENTER_INSIDE, VERT_TOP_INSIDE),
          AlignmentHelper.alignRelative(HOR_CENTER_OUTSIDE, VERT_TOP_OUTSIDE)
    ),
    SOUTH(AlignmentHelper.align(HOR_CENTER, VERT_BOTTOM),
          AlignmentHelper.alignRelative(HOR_CENTER_INSIDE, VERT_BOTTOM_INSIDE),
          AlignmentHelper.alignRelative(HOR_CENTER_OUTSIDE, VERT_BOTTOM_OUTSIDE)
    ),
    EAST(AlignmentHelper.align(HOR_RIGHT, VERT_CENTER),
         AlignmentHelper.alignRelative(HOR_RIGHT_INSIDE, VERT_CENTER_INSIDE),
         AlignmentHelper.alignRelative(HOR_RIGHT_OUTSIDE, VERT_CENTER_OUTSIDE)
    ),
    WEST(AlignmentHelper.align(HOR_LEFT, VERT_CENTER),
         AlignmentHelper.alignRelative(HOR_LEFT_INSIDE, VERT_CENTER_INSIDE),
         AlignmentHelper.alignRelative(HOR_LEFT_OUTSIDE, VERT_CENTER_OUTSIDE)
    ),
    NORTH_EAST(AlignmentHelper.align(HOR_LEFT, VERT_TOP),
               AlignmentHelper.alignRelative(HOR_LEFT_INSIDE, VERT_TOP_INSIDE),
               AlignmentHelper.alignRelative(HOR_LEFT_OUTSIDE, VERT_TOP_OUTSIDE)
    ),
    NORTH_WEST(AlignmentHelper.align(HOR_RIGHT, VERT_TOP),
               AlignmentHelper.alignRelative(HOR_RIGHT_INSIDE, VERT_TOP_INSIDE),
               AlignmentHelper.alignRelative(HOR_RIGHT_OUTSIDE, VERT_TOP_OUTSIDE)
    ),
    SOUTH_EAST(AlignmentHelper.align(HOR_RIGHT, VERT_BOTTOM),
               AlignmentHelper.alignRelative(HOR_RIGHT_INSIDE, VERT_BOTTOM_INSIDE),
               AlignmentHelper.alignRelative(HOR_RIGHT_OUTSIDE, VERT_BOTTOM_OUTSIDE)
    ),
    SOUTH_WEST(AlignmentHelper.align(HOR_LEFT, VERT_BOTTOM),
               AlignmentHelper.alignRelative(HOR_LEFT_INSIDE, VERT_BOTTOM_INSIDE),
               AlignmentHelper.alignRelative(HOR_LEFT_OUTSIDE, VERT_BOTTOM_OUTSIDE)
    ),
    CENTER((toAlign, alignAt) -> alignAt,
           AlignmentHelper.alignRelative(HOR_CENTER_INSIDE, VERT_CENTER_INSIDE),
           AlignmentHelper.alignRelative(HOR_CENTER_OUTSIDE, VERT_CENTER_OUTSIDE)
    );


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

    @Contract(pure = true)
    public Insets maskInsets(@NotNull final Insets insets) {
        return switch (this) {
            case NORTH -> new Insets(insets.top, 0, 0, 0);
            case NORTH_EAST -> new Insets(insets.top, 0, 0, insets.right);
            case EAST -> new Insets(0, 0, 0, insets.right);
            case SOUTH_EAST -> new Insets(0, 0, insets.bottom, insets.right);
            case SOUTH -> new Insets(0, 0, insets.bottom, 0);
            case SOUTH_WEST -> new Insets(0, insets.left, insets.bottom, 0);
            case WEST -> new Insets(0, insets.left, 0, 0);
            case NORTH_WEST -> new Insets(insets.top, insets.left, 0, 0);
            case CENTER -> new Insets(0, 0, 0, 0);
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
