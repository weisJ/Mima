/*
 * A font chooser JavaBean component.
 * Copyright (C) 2009 Dr Christos Bohoris
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3 as published by the Free Software Foundation;
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * swing@connectina.com
 */
package edu.kit.mima.gui.components.fontchooser.model;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.awt.Font;
import java.util.Objects;


/**
 * A generic implementation of {@code FontSelectionModel}.
 *
 * @author Christos Bohoris
 * @see Font
 */
public class DefaultFontSelectionModel implements FontSelectionModel {

    private static final int DEFAULT_SIZE = 12;

    /**
     * A list of registered event listeners.
     */
    private final EventListenerList listenerList = new EventListenerList();

    /**
     * Only one {@code ChangeEvent} is needed per model instance
     * since the event's only (read-only) state is the source property.
     * The source of events generated here is always "this".
     */
    private transient ChangeEvent changeEvent;

    private Font selectedFont;

    /**
     * Creates a {@code DefaultFontSelectionModel} with the
     * current font set to {@code font}, which should be
     * non-{@code null}. Note that setting the font to
     * {@code null} is undefined and may have unpredictable
     * results.
     *
     * @param font the new {@code Font}
     */
    public DefaultFontSelectionModel(Font font) {
        selectedFont = font;
    }

    /**
     * Returns the selected {@code Font} which should be
     * non-{@code null}.
     *
     * @return the selected {@code Font}
     */
    @Override
    public Font getSelectedFont() {
        return selectedFont;
    }

    /**
     * Sets the selected font to {@code font}.
     * Note that setting the font to {@code null}
     * is undefined and may have unpredictable results.
     * This method fires a state changed event if it sets the
     * current font to a new non-{@code null} font;
     * if the new font is the same as the current font,
     * no event is fired.
     *
     * @param font the new {@code Font}
     */
    @Override
    public void setSelectedFont(Font font) {
        if (font != null && !selectedFont.equals(font)) {
            selectedFont = font;
            fireStateChanged();
        }
    }

    @Override
    public String getSelectedFontName() {
        return selectedFont.getName();
    }

    @Override
    public String getSelectedFontFamily() {
        return selectedFont.getFamily();
    }

    @Override
    public int getSelectedFontSize() {
        return selectedFont.getSize();
    }

    /**
     * Adds a {@code ChangeListener} to the model.
     *
     * @param listener the {@code ChangeListener} to be added
     */
    @Override
    public void addChangeListener(ChangeListener listener) {
        listenerList.add(ChangeListener.class, listener);
    }

    /**
     * Removes a {@code ChangeListener} from the model.
     *
     * @param listener the {@code ChangeListener} to be removed
     */
    @Override
    public void removeChangeListener(ChangeListener listener) {
        listenerList.remove(ChangeListener.class, listener);
    }

    /**
     * Returns an array of all the {@code ChangeListener}s added
     * to this {@code DefaultFontSelectionModel} with
     * {@code addChangeListener}.
     *
     * @return all of the {@code ChangeListener}s added, or an empty
     * array if no listeners have been added
     */
    public ChangeListener[] getChangeListeners() {
        return listenerList.getListeners(ChangeListener.class);
    }

    /**
     * Runs each {@code ChangeListener}'s
     * {@code stateChanged} method.
     */
    private void fireStateChanged() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (Objects.equals(listeners[i], ChangeListener.class)) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
            }
        }
    }

}
