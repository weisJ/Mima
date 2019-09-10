package edu.kit.mima.gui.components.listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public interface MouseMovementListener extends MouseMotionListener {

   @Override
   default void mouseDragged(final MouseEvent e) {}

   @Override
   void mouseMoved(MouseEvent e);
}
