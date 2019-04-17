/*
  Copyright (c) 2009, Piet Blok
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

package org.pbjar.jxlayer.repaint;

import org.jdesktop.swingx.ForwardingRepaintManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.RepaintManager;
import java.applet.Applet;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Window;

/**
 * A fall back class for when the SwingX class {@link ForwardingRepaintManager} is not available on
 * the class path.
 * <p>
 * A {@link RepaintManager} that preserves functionality of a wrapped {@code RepaintManager}. All
 * methods will delegate to the wrapped {@code RepaintManager}.
 * </p>
 * <p>
 * When sub classing this class, one must in all overridden methods call the {@code super} method.
 * </p>
 *
 * @author Piet Blok
 * @see RepaintManagerUtils
 * @see RepaintManagerProvider
 * @see ForwardingRepaintManager
 */
public class WrappedRepaintManager extends RepaintManager {

    /**
     * The wrapped manager.
     */
    @NotNull
    private final RepaintManager delegate;

    /**
     * Construct a {@code RepaintManager} wrapping an existing {@code RepaintManager}.
     *
     * @param delegate an existing RepaintManager
     */
    @Contract("null -> fail")
    public WrappedRepaintManager(@Nullable RepaintManager delegate) {
        if (delegate == null) {
            throw new NullPointerException();
        }
        this.delegate = delegate;
    }

    /**
     * Just delegates. {@inheritDoc}
     */
    @Override
    @Deprecated
    public void addDirtyRegion(Applet applet, int x, int y, int w, int h) {
        delegate.addDirtyRegion(applet, x, y, w, h);
    }

    /**
     * Just delegates. {@inheritDoc}
     */
    @Override
    public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
        delegate.addDirtyRegion(c, x, y, w, h);
    }

    /**
     * Just delegates. {@inheritDoc}
     */
    @Override
    public void addDirtyRegion(Window window, int x, int y, int w, int h) {
        delegate.addDirtyRegion(window, x, y, w, h);
    }

    /**
     * Just delegates. {@inheritDoc}
     */
    @Override
    public void addInvalidComponent(JComponent invalidComponent) {
        delegate.addInvalidComponent(invalidComponent);
    }

    /**
     * Just delegates. {@inheritDoc}
     */
    @Override
    public Rectangle getDirtyRegion(JComponent c) {
        return delegate.getDirtyRegion(c);
    }

    /**
     * Just delegates. {@inheritDoc}
     */
    @Override
    public Dimension getDoubleBufferMaximumSize() {
        return delegate.getDoubleBufferMaximumSize();
    }

    /**
     * Just delegates. {@inheritDoc}
     */
    @Override
    public void setDoubleBufferMaximumSize(Dimension d) {
        delegate.setDoubleBufferMaximumSize(d);
    }

    /**
     * Just delegates. {@inheritDoc}
     */
    @Override
    public Image getOffscreenBuffer(Component c, int proposedWidth,
                                    int proposedHeight) {
        return delegate.getOffscreenBuffer(c, proposedWidth, proposedHeight);
    }

    /**
     * Just delegates. {@inheritDoc}
     */
    @Override
    public Image getVolatileOffscreenBuffer(Component c, int proposedWidth,
                                            int proposedHeight) {
        return delegate.getVolatileOffscreenBuffer(c, proposedWidth,
                                                   proposedHeight);
    }

    /**
     * Just delegates. {@inheritDoc}
     */
    @Override
    public boolean isCompletelyDirty(JComponent c) {
        return delegate.isCompletelyDirty(c);
    }

    /**
     * Just delegates. {@inheritDoc}
     */
    @Override
    public boolean isDoubleBufferingEnabled() {
        return delegate.isDoubleBufferingEnabled();
    }

    /**
     * Just delegates. {@inheritDoc}
     */
    @Override
    public void setDoubleBufferingEnabled(boolean flag) {
        delegate.setDoubleBufferingEnabled(flag);
    }

    /**
     * Just delegates. {@inheritDoc}
     */
    @Override
    public void markCompletelyClean(JComponent c) {
        delegate.markCompletelyClean(c);
    }

    /**
     * Just delegates. {@inheritDoc}
     */
    @Override
    public void markCompletelyDirty(JComponent c) {
        delegate.markCompletelyDirty(c);
    }

    /**
     * Just delegates. {@inheritDoc}
     */
    @Override
    public void paintDirtyRegions() {
        delegate.paintDirtyRegions();
    }

    /**
     * Just delegates. {@inheritDoc}
     */
    @Override
    public void removeInvalidComponent(JComponent component) {
        delegate.removeInvalidComponent(component);
    }

    /**
     * Just delegates. {@inheritDoc}
     */
    @Override
    public void validateInvalidComponents() {
        delegate.validateInvalidComponents();
    }

    /**
     * Get the delegate.
     *
     * @return the delegate
     */
    @Nullable
    public RepaintManager getDelegateManager() {
        return delegate;
    }

}
