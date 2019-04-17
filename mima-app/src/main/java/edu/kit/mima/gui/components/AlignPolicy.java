package edu.kit.mima.gui.components;

import org.jetbrains.annotations.NotNull;

import java.awt.Point;

/**
 * Align Policies for aligning something relative to the mouse or a container.
 *
 * @author Jannis Weis
 * @since 2018
 */
public enum AlignPolicy {
    MOUSE_BOTH {
        @NotNull
        @Override
        public Point calculatePosition(@NotNull final Point mouse, final Point component) {
            return new Point(mouse.x, mouse.y);
        }
    },
    COMPONENT_BOTH {
        @NotNull
        @Override
        public Point calculatePosition(final Point mouse, @NotNull final Point component) {
            return new Point(component.x, component.y);
        }
    },
    COMPONENT_X_MOUSE_Y {
        @NotNull
        @Override
        public Point calculatePosition(@NotNull final Point mouse, @NotNull final Point component) {
            return new Point(component.x, mouse.y);
        }
    },
    COMPONENT_Y_MOUSE_X {
        @NotNull
        @Override
        public Point calculatePosition(@NotNull final Point mouse, @NotNull final Point component) {
            return new Point(mouse.x, component.y);
        }
    };

    /**
     * Calculate relative position.
     *
     * @param mouse     mouse position.
     * @param component component position.
     * @return position with relative components.
     */
    @NotNull
    public abstract Point calculatePosition(final Point mouse, final Point component);
}
