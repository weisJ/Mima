/*
  Copyright (c) 2008-2009, Piet Blok
  All rights reserved.
  <p>
  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions
  are met:
  <p>
  * Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above
  copyright notice, this list of conditions and the following
  disclaimer in the documentation and/or other materials provided
  with the distribution.
  * Neither the name of the copyright holder nor the names of the
  contributors may be used to endorse or promote products derived
  from this software without specific prior written permission.
  <p>
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.pbjar.jxlayer.plaf.misc;

import org.jdesktop.jxlayer.JXLayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * Manages mouse events for {@link JXLayer}. Mouse events targeted at the glass pane are dispatched
 * to the glass pane itself and to the children in the component hierarchy. Other mouse events are
 * dispatched only to the children.
 * <p>
 * Sub classes may apply transformations in order to select a different child component than the
 * component associated with the event..
 * </p>
 *
 * <p>
 * The {@code AbstractTransparentMouseUI} class is <b>not</b> designed for shared use, but shared
 * use <em>may</em> work. I just didn't test it.
 * </p>
 *
 * @param <V> JXLayer's view
 * @param <S> A state object
 * @author Piet Blok
 */
public abstract class AbstractTransparentMouseUI<V extends JComponent, S> extends
                                                                          GeneralLayerUI<V, S> {

    private static final long serialVersionUID = 1L;

    @Nullable
    private Component lastEnteredTarget, lastPressedTarget;

    private boolean dispatchingMode = false;

    /**
     * Construct a new {@code UI} with {@link AWTEventListener} disabled.
     */
    public AbstractTransparentMouseUI() {
        super();
    }

    /**
     * Overridden to allow for re dispatching of mouse events to their intended (visual) recipients,
     * rather than to the components according to their bounds.
     */
    @Override
    public void eventDispatched(AWTEvent event, @NotNull final JXLayer<? extends V> layer) {

        if (event instanceof MouseEvent) {
            if (!dispatchingMode) {
                dispatchingMode = true;
                try {
                    redispatch((MouseEvent) event, layer);
                } finally {
                    dispatchingMode = false;
                }
            } else {
                Component component = ((MouseEvent) event).getComponent();
                layer.getGlassPane().setCursor(component.getCursor());
            }
        }
    }

    /**
     * Overridden to only get the following event types: {@link AWTEvent#MOUSE_EVENT_MASK}, {@link
     * AWTEvent#MOUSE_MOTION_EVENT_MASK} and {@link AWTEvent#MOUSE_WHEEL_EVENT_MASK}.
     */
    @Override
    public long getLayerEventMask() {
        return AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK
               | AWTEvent.MOUSE_WHEEL_EVENT_MASK;
    }

    @NotNull
    private Point calculateTargetPoint(@NotNull JXLayer<? extends V> layer,
                                       @NotNull MouseEvent mouseEvent) {
        Point point = mouseEvent.getPoint();
        V view = layer.getView();
        Rectangle layerBounds = layer.getBounds();
        Container parent = layer.getParent();
        Rectangle parentRectangle = new Rectangle(-layerBounds.x, -layerBounds.y,
                                                  parent.getWidth(), parent.getHeight());
        SwingUtilities.convertPointToScreen(point, mouseEvent.getComponent());
        SwingUtilities.convertPointFromScreen(point, view);
        if (parentRectangle.contains(point)) {
            return transformPoint(layer, point);
        } else {
            // Return a point outside the layer.
            return new Point(-1, -1);
        }
    }

    private void dispatchMouseEvent(@Nullable MouseEvent mouseEvent) {
        if (mouseEvent != null) {
            Component target = mouseEvent.getComponent();
            target.dispatchEvent(mouseEvent);
        }
    }

    private void generateEnterExitEvents(@NotNull JXLayer<? extends V> layer,
                                         @NotNull MouseEvent originalEvent,
                                         Component newTarget,
                                         @NotNull Point realPoint) {
        if (lastEnteredTarget != newTarget) {
            dispatchMouseEvent(transformMouseEvent(
                    layer, originalEvent,
                    lastEnteredTarget, realPoint, MouseEvent.MOUSE_EXITED));
            lastEnteredTarget = newTarget;
            dispatchMouseEvent(transformMouseEvent(
                    layer, originalEvent,
                    lastEnteredTarget, realPoint, MouseEvent.MOUSE_ENTERED));
        }
    }

    /**
     * Find the first component up in the hierarchy that may be listening for a specific event.
     *
     * @param event     the event
     * @param component the first component to investigate
     * @return A component that is listening for this type of event, or {@code null} if such a
     * component can not be found.
     */
    @SuppressWarnings("Duplicates")
    @Nullable
    private Component getListeningComponent(@NotNull MouseEvent event,
                                            @NotNull Component component) {
        return switch (event.getID()) {
            case (MouseEvent.MOUSE_CLICKED),
                    (MouseEvent.MOUSE_ENTERED),
                    (MouseEvent.MOUSE_EXITED),
                    (MouseEvent.MOUSE_PRESSED),
                    (MouseEvent.MOUSE_RELEASED) -> getMouseListeningComponent(component);
            case (MouseEvent.MOUSE_DRAGGED),
                    (MouseEvent.MOUSE_MOVED) -> getMouseMotionListeningComponent(component);
            case (MouseEvent.MOUSE_WHEEL) -> getMouseWheelListeningComponent(component);
            default -> null;
        };
    }

    @Nullable
    private Component getMouseListeningComponent(@NotNull Component component) {
        if (component.getMouseListeners().length > 0) {
            return component;
        } else {
            Container parent = component.getParent();
            if (parent != null) {
                return getMouseListeningComponent(parent);
            } else {
                return null;
            }
        }
    }

    @SuppressWarnings("Duplicates")
    @Nullable
    private Component getMouseMotionListeningComponent(@NotNull Component component) {
        /*
         * Mouse motion events may result in MOUSE_ENTERED and MOUSE_EXITED.
         *
         * Therefore, components with MouseListeners registered should be
         * returned as well.
         */
        if (component.getMouseMotionListeners().length > 0
            || component.getMouseListeners().length > 0) {
            return component;
        } else {
            Container parent = component.getParent();
            if (parent != null) {
                return getMouseMotionListeningComponent(parent);
            } else {
                return null;
            }
        }
    }

    @Nullable
    private Component getMouseWheelListeningComponent(@NotNull Component component) {
        if (component.getMouseWheelListeners().length > 0) {
            return component;
        } else {
            Container parent = component.getParent();
            if (parent != null) {
                return getMouseWheelListeningComponent(parent);
            } else {
                return null;
            }
        }
    }

    @Nullable
    private Component getTarget(@NotNull JXLayer<? extends V> layer, @NotNull Point targetPoint) {
        Component userTopLevel = getTopLevelUserComponent(layer);
        if (userTopLevel == null) {
            return null;
        } else {
            Point viewPoint = SwingUtilities.convertPoint(layer.getView(),
                                                          targetPoint, userTopLevel);
            return SwingUtilities.getDeepestComponentAt(userTopLevel,
                                                        viewPoint.x, viewPoint.y);
        }
    }

    @SuppressWarnings("Duplicates")
    private void redispatch(@NotNull MouseEvent originalEvent,
                            @NotNull final JXLayer<? extends V> layer) {

        V view = layer.getView();
        if (view != null) {
            if (originalEvent.getComponent() != layer.getGlassPane()) {
                originalEvent.consume();
            }
            MouseEvent newEvent = null;

            Point realPoint = calculateTargetPoint(layer, originalEvent);
            Component realTarget = getTarget(layer, realPoint);
            if (realTarget != null) {
                realTarget = getListeningComponent(originalEvent, realTarget);
            }

            switch (originalEvent.getID()) {
                case MouseEvent.MOUSE_RELEASED:
                    newEvent =
                            transformMouseEvent(layer, originalEvent, lastPressedTarget, realPoint);
                    lastPressedTarget = null;
                    break;
                case MouseEvent.MOUSE_ENTERED:
                case MouseEvent.MOUSE_EXITED:
                    // is ignored, see MOUSE_MOVED / MOUSE_DRAGGED
                    generateEnterExitEvents(layer, originalEvent, realTarget, realPoint);
                    break;
                case MouseEvent.MOUSE_CLICKED:
                    newEvent = transformMouseEvent(layer, originalEvent, realTarget, realPoint);
                    break;
                case MouseEvent.MOUSE_PRESSED:
                    newEvent = transformMouseEvent(layer, originalEvent, realTarget, realPoint);
                    if (newEvent != null) {
                        lastPressedTarget = newEvent.getComponent();
                    }
                    break;
                case MouseEvent.MOUSE_MOVED:
                    newEvent = transformMouseEvent(layer, originalEvent, realTarget, realPoint);
                    generateEnterExitEvents(layer, originalEvent, realTarget, realPoint);
                    break;
                case MouseEvent.MOUSE_DRAGGED:
                    newEvent =
                            transformMouseEvent(layer, originalEvent, lastPressedTarget, realPoint);
                    generateEnterExitEvents(layer, originalEvent, realTarget, realPoint);
                    break;
                case (MouseEvent.MOUSE_WHEEL):
                    redispatchMouseWheelEvent((MouseWheelEvent) originalEvent, realTarget, layer);
                    break;
            }
            dispatchMouseEvent(newEvent);
        }
    }

    /**
     * Re-dispatch a MouseWheelEvent. Two steps are performed:
     * <ol>
     * <li>Invoke
     * {@link #transformMouseWheelEvent(MouseWheelEvent, Component, JXLayer)} creates a new
     * event.</li>
     * <li>Invoke {@link #processMouseWheelEvent(MouseWheelEvent, JXLayer)} with
     * the new event.</li>
     * </ol>
     *
     * @param mouseWheelEvent the event
     * @param target          the target component
     * @param layer           the layer
     */
    private void redispatchMouseWheelEvent(@NotNull MouseWheelEvent mouseWheelEvent,
                                           @NotNull Component target,
                                           @NotNull JXLayer<? extends V> layer) {
        MouseWheelEvent newEvent = this.transformMouseWheelEvent(
                mouseWheelEvent, target, layer);
        processMouseWheelEvent(newEvent, layer);
    }

    @Nullable
    private MouseEvent transformMouseEvent(@NotNull JXLayer<? extends V> layer,
                                           @NotNull MouseEvent mouseEvent,
                                           @NotNull Component target,
                                           @NotNull Point realPoint) {
        return transformMouseEvent(layer, mouseEvent, target, realPoint,
                                   mouseEvent.getID());
    }

    @Nullable
    private MouseEvent transformMouseEvent(@NotNull JXLayer<? extends V> layer,
                                           @NotNull MouseEvent mouseEvent,
                                           @Nullable Component target,
                                           @NotNull Point targetPoint, int id) {
        if (target == null) {
            return null;
        } else {
            Point newPoint = new Point(targetPoint);
            SwingUtilities.convertPointToScreen(newPoint, layer.getView());
            SwingUtilities.convertPointFromScreen(newPoint, target);
            return new MouseEvent(target, //
                                  id, //
                                  mouseEvent.getWhen(), //
                                  mouseEvent.getModifiersEx(), //
                                  newPoint.x, //
                                  newPoint.y, //
                                  mouseEvent.getClickCount(), //
                                  mouseEvent.isPopupTrigger(), //
                                  mouseEvent.getButton());
        }
    }

    /**
     * Transforms a {@code MouseWheelEvent} to a new event with the right coordinates and the target
     * source component.
     *
     * @param mouseWheelEvent the event
     * @param t               the target component
     * @param layer           the layer
     * @return a new event
     */
    @NotNull
    private MouseWheelEvent transformMouseWheelEvent(
            @NotNull MouseWheelEvent mouseWheelEvent, Component t,
            JXLayer<? extends V> layer) {
        var target = t;
        if (target == null) {
            target = layer;
        }
        Point point = SwingUtilities.convertPoint(mouseWheelEvent.getComponent(),
                                                  mouseWheelEvent.getPoint(), target);
        return new MouseWheelEvent(target, //
                                   mouseWheelEvent.getID(), //
                                   mouseWheelEvent.getWhen(), //
                                   mouseWheelEvent.getModifiersEx(), //
                                   point.x, //
                                   point.y, //
                                   mouseWheelEvent.getClickCount(), //
                                   mouseWheelEvent.isPopupTrigger(), //
                                   mouseWheelEvent.getScrollType(), //
                                   mouseWheelEvent.getScrollAmount(), //
                                   mouseWheelEvent.getWheelRotation() //
        );
    }

    /**
     * Return some component in this hierarchy that serves as the start for a search for deeper
     * components.
     *
     * @param layer The layer
     * @return a component
     */
    @NotNull
    protected abstract Component getTopLevelUserComponent(
            JXLayer<? extends V> layer);

    /**
     * When transformations are used, transform a point in device space to a point in component
     * space.
     *
     * @param layer The layer
     * @param point A point
     * @return a transformed point
     */
    @NotNull
    protected abstract Point transformPoint(JXLayer<? extends V> layer,
                                            Point point);

}
