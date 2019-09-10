/*
 Copyright (c) 2008-2009, Piet Blok All rights reserved.
 <p>
 Redistribution and use in source and binary forms, with or without modification, are permitted
 provided that the following conditions are met:
 <p>
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 and the following disclaimer. * Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the documentation and/or other
 materials provided with the distribution. * Neither the name of the copyright holder nor the
 names of the contributors may be used to endorse or promote products derived from this software
 without specific prior written permission.
 <p>
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package demo;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

/**
 * A component that maintains a double value that can be changed by the user.
 *
 * <p>
 *
 * <ol>
 * <li>The user may use the mouse wheel over the whole area of the control to increase or decrease
 * the value.
 * <li>An up button is provided to single step up.
 * <li>A down button is provided to single step down.
 * <li>A button is provided to reset the control to its initial value (this button displays the
 * current value).
 * </ol>
 *
 * @author Piet Blok
 */
public class WheelButton extends JPanel {

    /**
     * The name of the double value bound property.
     *
     * @see PropertyChangeListener
     */
    public static final String KEY_CURRENT = WheelButton.class.getName() + ".currentValue";

    @NotNull
    private static final ImageIcon upIcon, dnIcon;
    private static final long serialVersionUID = 1L;

    static {
        BufferedImage image = new BufferedImage(32, 26, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            Path2D triangle = new Path2D.Double();
            triangle.moveTo(1, 25);
            triangle.lineTo(31, 25);
            triangle.lineTo(16, 1);
            triangle.closePath();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(
                    RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2.setColor(Color.GRAY);
            g2.fill(triangle);
            g2.setColor(Color.BLACK);
            g2.draw(triangle);
        } finally {
            g2.dispose();
        }
        upIcon = new ImageIcon(image);

        image = new BufferedImage(32, 26, BufferedImage.TYPE_INT_ARGB);
        g2 = image.createGraphics();
        try {
            Path2D triangle = new Path2D.Double();
            triangle.moveTo(1, 1);
            triangle.lineTo(31, 1);
            triangle.lineTo(16, 25);
            triangle.closePath();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(
                    RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            g2.setColor(Color.GRAY);
            g2.fill(triangle);
            g2.setColor(Color.BLACK);
            g2.draw(triangle);
        } finally {
            g2.dispose();
        }
        dnIcon = new ImageIcon(image);
    }

    private final double resetValue, incrementValue;
    private final IncrementType type;

    /**
     * Construct a {@code WheelButton}.
     *
     * @param name           <b>required</b> a name that will be displayed in a {@link TitledBorder}
     * @param resetValue     <b>required</b> the initial value
     * @param type           <b>required</b> the {@link IncrementType}
     * @param incrementValue <b>required</b> the increment or factor, depends on the specified {@link
     *                       IncrementType}
     * @param numberFormat   a {@code NumberFormat} that will be used to display the current value, or
     *                       {@code null} to display the value as a double
     * @param listener       a {@link PropertyChangeListener} that will handle changes of the {@link
     *                       #KEY_CURRENT} property, or {@code null}
     */
    public WheelButton(
            final String name,
            final double resetValue,
            final IncrementType type,
            final double incrementValue,
            final NumberFormat numberFormat,
            final PropertyChangeListener listener) {
        super(new BorderLayout());
        JPanel content = new JPanel(new BorderLayout());
        this.add(content);
        content.setBorder(BorderFactory.createTitledBorder(name));
        this.resetValue = resetValue;
        this.incrementValue = incrementValue;
        this.type = type;
        this.putClientProperty(KEY_CURRENT, resetValue);
        content.add(
                new JButton(
                        new AbstractAction() {

                            private static final long serialVersionUID = 1L;

                            {
                                this.putValue(Action.SMALL_ICON, upIcon);
                            }

                            @Override
                            public void actionPerformed(final ActionEvent e) {
                                processRotation(1);
                            }
                        }),
                BorderLayout.PAGE_START);
        content.add(
                new JButton(
                        new AbstractAction() {

                            private static final long serialVersionUID = 1L;

                            {
                                this.putValue(Action.SMALL_ICON, dnIcon);
                            }

                            @Override
                            public void actionPerformed(final ActionEvent e) {
                                processRotation(-1);
                            }
                        }),
                BorderLayout.PAGE_END);
        final JButton resetButton = new JButton(format(numberFormat, resetValue));
        resetButton.addActionListener(
                e -> WheelButton.this.putClientProperty(KEY_CURRENT, WheelButton.this.resetValue));
        content.add(resetButton, BorderLayout.CENTER);
        this.addPropertyChangeListener(WheelButton.KEY_CURRENT, listener);
        this.addPropertyChangeListener(
                WheelButton.KEY_CURRENT,
                evt -> resetButton.setText(format(numberFormat, evt.getNewValue())));
        this.addMouseWheelListener(
                event -> {
                    /*
                     * To sync with the arrow buttons invert the rotation.
                     */
                    processRotation(-event.getWheelRotation());
                });
    }

    /**
     * Get the current value. The current value is a bound property.
     *
     * @return the current value
     * @see #KEY_CURRENT
     */
    public double getCurrentValue() {
        return (Double) this.getClientProperty(KEY_CURRENT);
    }

    @Contract(pure = true)
    private double calculate(final double current, final int rotation) {
        double newValue;
        if (type == IncrementType.Factor) {
            newValue = current * Math.pow(incrementValue, rotation);
        } else {
            newValue = current + rotation * incrementValue;
        }
        return newValue;
    }

    @NotNull
    @Contract("!null, _ -> !null")
    private String format(@Nullable final NumberFormat numberFormat, @NotNull final Object value) {
        return numberFormat == null ? value.toString() : numberFormat.format(value);
    }

    private void processRotation(final int rotation) {
        double current = WheelButton.this.getCurrentValue();
        double newValue = calculate(current, rotation); //
        double upper = calculate(newValue, 1);
        double lower = calculate(newValue, -1);
        /*
         * If the new value is between the upper and lower boundary, set the new
         * value to the reset value.
         */
        if (lower < WheelButton.this.resetValue && WheelButton.this.resetValue < upper) {
            newValue = WheelButton.this.resetValue;
        } else
            /*
             * If, because of floating point limitations, the new value is outside
             * the bounds of lower and upper, set the value back to the current
             * value.
             */
            if (lower >= newValue || newValue >= upper) {
                newValue = current;
            }
        putClientProperty(KEY_CURRENT, newValue);
    }

    /**
     * Describes the increment / decrement type.
     */
    public enum IncrementType {
        /**
         * Increment / decrement by a factor. Computation is as follows:
         *
         * <pre>
         * newValue = current * Math.pow(incrementValue, rotation);
         * </pre>
         */
        Factor,
        /**
         * Increment / decrement with a fixed amount. Computation is as follows:
         *
         * <pre>
         * newValue = current + rotation * incrementValue;
         * </pre>
         */
        Fixed,
    }
}
