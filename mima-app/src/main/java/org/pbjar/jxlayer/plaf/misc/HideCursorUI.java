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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A LayerUI that hides the cursor. After a MouseEvent or MouseMoveEvent the cursor will reappear
 * for some specified time.
 *
 * @author Piet Blok
 */
public final class HideCursorUI extends GeneralLayerUI<JComponent, HideCursorUI.HideCursorState> {

    private static final long serialVersionUID = 1L;
    private static final Cursor nullCursor =
            Toolkit.getDefaultToolkit()
                    .createCustomCursor(
                            new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "nullCursor");
    private final int timeout;

    /**
     * Equivalent to {@code HideCursorUI(0)}.
     *
     * @see #HideCursorUI(int)
     */
    public HideCursorUI() {
        this(0);
    }

    /**
     * Create a HideCursorUI with a specified timeout. If timeout is 0, the cursor will not be hidden.
     *
     * @param timeout the timeout
     */
    public HideCursorUI(final int timeout) {
        super();
        this.timeout = timeout;
    }

    /**
     * Get {@link Action}s that.
     *
     * <ol>
     * <li>Set the cursor timeout value.
     * <li>
     * </ol>
     */
    @NotNull
    @Override
    public List<Action> getActions(@NotNull final JXLayer<? extends JComponent> layer) {
        ArrayList<Action> actionList = new ArrayList<>(super.getActions(layer));
        /*
         * Change the cursor timeout
         */
        actionList.add(
                new AbstractAction("Set cursor timeout") {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(final ActionEvent event) {
                        HideCursorState state = HideCursorUI.this.getStateObject(layer);
                        Integer timeout = state.getTimeout();
                        JSpinner spinner = new JSpinner();
                        SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
                        model.setStepSize(100);
                        model.setMinimum(0);
                        spinner.setValue(timeout);
                        if (JOptionPane.OK_OPTION
                            == JOptionPane.showConfirmDialog(
                                layer, spinner, "Change cursor timeout", JOptionPane.OK_CANCEL_OPTION)) {
                            timeout = (Integer) spinner.getValue();
                            state.setTimeout(timeout);
                        }
                    }
                });

        return actionList;
    }

    private void resetCursor(@NotNull final JXLayer<? extends JComponent> layer) {
        getStateObject(layer).resetCursor(layer);
    }

    @Override
    protected void cleanupStateObject(@NotNull final HideCursorState stateObject) {
        stateObject.timer.stop();
    }

    @NotNull
    @Override
    protected HideCursorState createStateObject(@NotNull final JXLayer<? extends JComponent> layer) {
        return new HideCursorState(layer, timeout);
    }

    @Override
    protected void processMouseEvent(final MouseEvent e, @NotNull final JXLayer<? extends JComponent> layer) {
        super.processMouseEvent(e, layer);
        resetCursor(layer);
    }

    @Override
    protected void processMouseMotionEvent(final MouseEvent e, @NotNull final JXLayer<? extends JComponent> l) {
        super.processMouseMotionEvent(e, l);
        resetCursor(l);
    }

    /**
     * Holds state information.
     */
    protected static class HideCursorState {

        @NotNull
        private final Timer timer;
        @NotNull
        private final Cursor oldCursor;
        private int timeout;

        public HideCursorState(@NotNull final JXLayer<? extends JComponent> layer, final int timeout) {
            this.timeout = timeout;
            oldCursor = layer.getGlassPane().getCursor();
            this.timer = new Timer(timeout, event -> layer.getGlassPane().setCursor(nullCursor));
            timer.setRepeats(false);
        }

        public int getTimeout() {
            return timeout;
        }

        public void setTimeout(final int timeout) {
            this.timeout = timeout;
            timer.setInitialDelay(timeout);
        }

        public void resetCursor(@NotNull final JXLayer<? extends JComponent> layer) {
            if (timeout > 0) {
                layer.getGlassPane().setCursor(oldCursor);
                timer.restart();
            }
        }
    }
}
