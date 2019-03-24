package edu.kit.mima.gui.components.alignment;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.function.BiFunction;

/**
 * Helper methods for calculating alignments.
 *
 * @author Jannis Weis
 * @since 2018
 */
final class AlignmentHelper {

    /**
     * Provided mapping functions.
     */
    /*default*/ static final Mapper HOR_CENTER = (d, p) -> p.x - d.width / 2;
    /*default*/ static final Mapper HOR_LEFT = (d, p) -> p.x - d.width;
    /*default*/ static final Mapper HOR_RIGHT = (d, p) -> p.x;
    /*default*/ static final Mapper VERT_CENTER = (d, p) -> p.y - d.height / 2;
    /*default*/ static final Mapper VERT_TOP = (d, p) -> p.y - d.height;
    /*default*/ static final Mapper VERT_BOTTOM = (d, p) -> p.y;

    /**
     * Provided relative mapping functions.
     */
    /*default*/ static final RMapper HOR_CENTER_INSIDE = (d, r) -> r.x + (r.width - d.width) / 2;
    /*default*/ static final RMapper HOR_LEFT_INSIDE = (d, r) -> r.x;
    /*default*/ static final RMapper HOR_RIGHT_INSIDE = (d, r) -> r.x + r.width - d.width;
    /*default*/ static final RMapper VERT_CENTER_INSIDE = (d, r) -> r.y + (r.width - d.width) / 2;
    /*default*/ static final RMapper VERT_TOP_INSIDE = (d, r) -> r.y;
    /*default*/ static final RMapper VERT_BOTTOM_INSIDE = (d, r) -> r.y + r.height - d.height;

    /*default*/ static final RMapper HOR_CENTER_OUTSIDE = HOR_CENTER_INSIDE;
    /*default*/ static final RMapper HOR_LEFT_OUTSIDE = (d, r) -> r.x - d.width;
    /*default*/ static final RMapper HOR_RIGHT_OUTSIDE = (d, r) -> r.x;
    /*default*/ static final RMapper VERT_CENTER_OUTSIDE = VERT_CENTER_INSIDE;
    /*default*/ static final RMapper VERT_TOP_OUTSIDE = (d, r) -> r.y - d.height;
    /*default*/ static final RMapper VERT_BOTTOM_OUTSIDE = (d, r) -> r.y + r.height;


    /**
     * Create mapper from component mapper.
     *
     * @param mapperX x component mapper.
     * @param mapperY y component mapper.
     * @return mapper that aligns a rectangle to point.
     */
    @NotNull
    @Contract(pure = true)
    /*default*/ static BiFunction<Dimension, Point, Point> align(
            Mapper mapperX,
            Mapper mapperY) {
        return (d, p) -> new Point(mapperX.apply(d, p), mapperY.apply(d, p));
    }

    /**
     * Create mapper from component mapper.
     *
     * @param mapperX x component mapper.
     * @param mapperY y component mapper.
     * @return mapper that aligns a rectangle relative to other rectangle.
     */
    @NotNull
    @Contract(pure = true)
    /*default*/ static BiFunction<Dimension, Rectangle, Point> alignRelative(
            RMapper mapperX,
            RMapper mapperY) {
        return (d, p) -> new Point(mapperX.apply(d, p), mapperY.apply(d, p));
    }

    /**
     * Helper interface to avoid long type names.
     */
    private interface Mapper extends BiFunction<Dimension, Point, Integer> {
    }

    /**
     * Helper interface to avoid long type names.
     */
    private interface RMapper extends BiFunction<Dimension, Rectangle, Integer> {
    }
}
