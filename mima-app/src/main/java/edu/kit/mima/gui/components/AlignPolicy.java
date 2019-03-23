package edu.kit.mima.gui.components;

import java.awt.Point;

/**
 * Align Policies for aligning something relative to the mouse or a container.
 *
 * @author Jannis Weis
 * @since 2018
 */
public enum AlignPolicy {
    MOUSE_BOTH {
        @Override
        public Point calculatePosition(final Point mouse, final Point component) {
            return new Point(mouse.x, mouse.y);
        }
    },
    COMPONENT_BOTH {
        @Override
        public Point calculatePosition(final Point mouse, final Point component) {
            return new Point(component.x, component.y);
        }
    },
    COMPONENT_X_MOUSE_Y {
        @Override
        public Point calculatePosition(final Point mouse, final Point component) {
            return new Point(component.x, mouse.y);
        }
    },
    COMPONENT_Y_MOUSE_X {
        @Override
        public Point calculatePosition(final Point mouse, final Point component) {
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
    public abstract Point calculatePosition(final Point mouse, final Point component);
}
