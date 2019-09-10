package edu.kit.mima.gui.components;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Insets2D implements Cloneable {

    public double top;
    public double left;
    public double bottom;
    public double right;

    /**
     * Creates and initializes a new <code>Insets</code> object with the
     * specified top, left, bottom, and right insets.
     * @param       top   the inset from the top.
     * @param       left   the inset from the left.
     * @param       bottom   the inset from the bottom.
     * @param       right   the inset from the right.
     */
    @Contract(pure = true)
    public Insets2D(final double top, final double left, final double bottom, final double right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    /**
     * Set top, left, bottom, and right to the specified values
     *
     * @param       top   the inset from the top.
     * @param       left   the inset from the left.
     * @param       bottom   the inset from the bottom.
     * @param       right   the inset from the right.
     * @since 1.5
     */
    public void set(final double top, final double left, final double bottom, final double right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }


    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Insets2D) {
            Insets2D insets = (Insets2D)obj;
            return ((top == insets.top) && (left == insets.left) &&
                    (bottom == insets.bottom) && (right == insets.right));
        }
        return false;
    }

    @Contract(pure = true)
    @Override
    public int hashCode() {
        double sum1 = left + bottom;
        double sum2 = right + top;
        double val1 = sum1 * (sum1 + 1)/2 + left;
        double val2 = sum2 * (sum2 + 1)/2 + top;
        double sum3 = val1 + val2;
        return (int)(sum3 * (sum3 + 1)/2 + val2);
    }

    @NotNull
    public String toString() {
        return getClass().getName() + "[top="  + top + ",left=" + left + ",bottom=" + bottom + ",right=" + right + "]";
    }

    @NotNull
    @Contract(value = " -> new", pure = true)
    @Override
    public Insets2D clone() {
        return new Insets2D(top, left, bottom, right);
    }


}
