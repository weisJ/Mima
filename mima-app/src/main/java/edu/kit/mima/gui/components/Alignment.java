package edu.kit.mima.gui.components;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Alignment for GUI elements.
 *
 * @author Jannis Weis
 * @since 2018
 */
public enum Alignment {
    NORTH,
    NORTH_EAST,
    EAST,
    SOUTH_EAST,
    SOUTH,
    SOUTH_WEST,
    WEST,
    NORTH_WEST,
    CENTER;

    /**
     * Get the opposite alignment.
     *
     * @return Alignment opposite on the compass.
     */
    @Contract(pure = true)
    @NotNull
    public Alignment opposite() {
        switch (this) {
            case NORTH:
                return SOUTH;
            case NORTH_EAST:
                return SOUTH_WEST;
            case EAST:
                return WEST;
            case SOUTH_EAST:
                return NORTH_WEST;
            case SOUTH:
                return NORTH;
            case SOUTH_WEST:
                return NORTH_EAST;
            case WEST:
                return EAST;
            case NORTH_WEST:
                return SOUTH_EAST;
            case CENTER:
                return CENTER;
            default:
                return CENTER;
        }
    }
}
