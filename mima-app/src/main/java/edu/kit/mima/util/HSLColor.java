package edu.kit.mima.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.function.Function;

/**
 * The HSLColor class provides methods to manipulate HSL (Hue, Saturation Luminance) values to
 * create a corresponding Color object using the RGB ColorSpace.
 *
 * <p>The HUE is the color, the Saturation is the purity of the color (with respect to grey) and
 * Luminance is the brightness of the color (with respect to black and white)
 *
 * <p>The Hue is specified as an angel between 0 - 360 degrees where red is 0, green is 120 and
 * blue is 240. In between you have the colors of the rainbow. Saturation is specified as a
 * percentage between 0 - 100 where 100 is fully saturated and 0 approaches gray. Luminance is
 * specified as a percentage between 0 - 100 where 0 is black and 100 is white.
 *
 * <p>In particular the HSL color space makes it easier change the Tone or Shade of a color by
 * adjusting the luminance value.
 */
@SuppressWarnings("CheckStyle")
public class HSLColor {
    private final Color rgb;
    private final float[] hsl;
    private final float alpha;

    /**
     * Create a HSLColor object using an RGB Color object.
     *
     * @param rgb the RGB Color object
     */
    public HSLColor(@NotNull final Color rgb) {
        this.rgb = rgb;
        hsl = fromRGB(rgb);
        alpha = rgb.getAlpha() / 255.0f;
    }

    /**
     * Create a HSLColor object using individual HSL values and a default alpha value of 1.0.
     *
     * @param h is the Hue value in degrees between 0 - 360
     * @param s is the Saturation percentage between 0 - 100
     * @param l is the Luminance percentage between 0 - 100
     */
    public HSLColor(final float h, final float s, final float l) {
        this(h, s, l, 1.0f);
    }

    /**
     * Create a HSLColor object using individual HSL values.
     *
     * @param h     the Hue value in degrees between 0 - 360
     * @param s     the Saturation percentage between 0 - 100
     * @param l     the Luminance percentage between 0 - 100
     * @param alpha the alpha value between 0 - 1
     */
    public HSLColor(final float h, final float s, final float l, final float alpha) {
        hsl = new float[]{h, s, l};
        this.alpha = alpha;
        rgb = toRGB(hsl, alpha);
    }

    /**
     * Create a HSLColor object using an an array containing the individual HSL values and with a
     * default alpha value of 1.
     *
     * @param hsl array containing HSL values
     */
    public HSLColor(@NotNull final float[] hsl) {
        this(hsl, 1.0f);
    }

    /**
     * Create a HSLColor object using an an array containing the individual HSL values.
     *
     * @param hsl   array containing HSL values
     * @param alpha the alpha value between 0 - 1
     */
    public HSLColor(@NotNull final float[] hsl, final float alpha) {
        this.hsl = hsl;
        this.alpha = alpha;
        rgb = toRGB(hsl, alpha);
    }

    /**
     * Convert a RGB Color to it corresponding HSL values.
     *
     * @param color color to create from.
     * @return an array containing the 3 HSL values.
     */
    @NotNull
    @Contract("_ -> new")
    public static float[] fromRGB(@NotNull final Color color) {
        // Get RGB values in the range 0 - 1
        final float[] rgb = color.getRGBColorComponents(null);
        final float r = rgb[0];
        final float g = rgb[1];
        final float b = rgb[2];
        // Minimum and Maximum RGB values are used in the HSL calculations
        final float min = Math.min(r, Math.min(g, b));
        final float max = Math.max(r, Math.max(g, b));
        float hue = 0;
        if (max == r) {
            hue = ((60 * (g - b) / (max - min)) + 360) % 360;
        } else if (max == g) {
            hue = (60 * (b - r) / (max - min)) + 120;
        } else if (max == b) {
            hue = (60 * (r - g) / (max - min)) + 240;
        }
        final float luminance = (max + min) / 2;

        final float saturation;
        if (max == min) {
            saturation = 0;
        } else if (luminance <= .5f) {
            saturation = (max - min) / (max + min);
        } else {
            saturation = (max - min) / (2 - max - min);
        }
        return new float[]{hue, saturation * 100, luminance * 100};
    }

    /**
     * Convert HSL values to a RGB Color with a default alpha value of 1. H (Hue) is specified as
     * degrees in the range 0 - 360. S (Saturation) is specified as a percentage in the range 1 -
     * 100. L (Luminance) is specified as a percentage in the range 1 - 100.
     *
     * @param hsl an array containing the 3 HSL values
     * @return the RGB Color object
     */
    @NotNull
    public static Color toRGB(@NotNull final float[] hsl) {
        return toRGB(hsl, 1.0f);
    }

    /**
     * Convert HSL values to a RGB Color. H (Hue) is specified as degrees in the range 0 - 360. S
     * (Saturation) is specified as a percentage in the range 1 - 100. L (Luminance) is specified as
     * a percentage in the range 1 - 100.
     *
     * @param hsl   an array containing the 3 HSL values
     * @param alpha the alpha value between 0 - 1
     * @return the RGB Color object
     */
    @NotNull
    public static Color toRGB(@NotNull final float[] hsl, final float alpha) {
        return toRGB(hsl[0], hsl[1], hsl[2], alpha);
    }

    /**
     * Convert HSL values to a RGB Color with a default alpha value of 1.
     *
     * @param h Hue is specified as degrees in the range 0 - 360.
     * @param s Saturation is specified as a percentage in the range 1 - 100.
     * @param l Luminance is specified as a percentage in the range 1 - 100.
     * @return the RGB Color object
     */
    @NotNull
    public static Color toRGB(final float h, final float s, final float l) {
        return toRGB(h, s, l, 1.0f);
    }

    /**
     * Convert HSL values to a RGB Color.
     *
     * @param h     Hue is specified as degrees in the range 0 - 360.
     * @param s     Saturation is specified as a percentage in the range 1 - 100.
     * @param l     Luminance is specified as a percentage in the range 1 - 100.
     * @param alpha the alpha value between 0 - 1
     * @return the RGB Color object
     */
    @NotNull
    @Contract("_, _, _, _ -> new")
    public static Color toRGB(final float h, final float s, final float l, final float alpha) {
        checkParameters(s, l, alpha);
        //  Formula needs all values between 0 - 1.
        final float hue = (h % 360.0f) / 360.0f;
        final float saturation = s / 100f;
        final float luminance = l / 100f;

        final float q = luminance < 0.5
                ? luminance * (1 + saturation)
                : (luminance + saturation) - (saturation * luminance);
        final float p = 2 * luminance - q;

        final float r = Math.min(Math.max(0, hueToRGB(p, q, hue + (1.0f / 3.0f))), 1.0f);
        final float g = Math.min(Math.max(0, hueToRGB(p, q, hue)), 1.0f);
        final float b = Math.min(Math.max(0, hueToRGB(p, q, hue - (1.0f / 3.0f))), 1.0f);

        return new Color(r, g, b, alpha);
    }

    private static void checkParameters(final float s, final float l, final float alpha) {
        if (s < 0.0f || s > 100.0f) {
            final String message = "Color parameter outside of expected range - Saturation";
            throw new IllegalArgumentException(message);
        }

        if (l < 0.0f || l > 100.0f) {
            final String message = "Color parameter outside of expected range - Luminance";
            throw new IllegalArgumentException(message);
        }

        if (alpha < 0.0f || alpha > 1.0f) {
            final String message = "Color parameter outside of expected range - Alpha";
            throw new IllegalArgumentException(message);
        }
    }

    @Contract(pure = true)
    private static float hueToRGB(final float p, final float q, float h) {
        if (h < 0) {
            h += 1;
        }
        if (h > 1) {
            h -= 1;
        }
        if (6 * h < 1) {
            return p + ((q - p) * 6 * h);
        }
        if (2 * h < 1) {
            return q;
        }
        if (3 * h < 2) {
            return p + ((q - p) * 6 * ((2.0f / 3.0f) - h));
        }
        return p;
    }

    /**
     * Create a RGB Color object based on this HSLColor with a different Hue value. The degrees
     * specified is an absolute value.
     *
     * @param degrees - the Hue value between 0 - 360
     * @return the RGB Color object
     */
    @NotNull
    public HSLColor adjustHue(final float degrees) {
        return new HSLColor(toRGB(degrees, hsl[1], hsl[2], alpha));
    }

    /**
     * Create a RGB Color object based on this HSLColor with a different Luminance value. The
     * percent specified is an absolute value.
     *
     * @param percent - the Luminance value between 0 - 100
     * @return the RGB Color object
     */
    @NotNull
    public HSLColor adjustLuminance(final float percent) {
        return new HSLColor(toRGB(hsl[0], hsl[1], percent, alpha));
    }

    /**
     * Create a RGB Color object based on this HSLColor with a different Saturation value. The
     * percent specified is an absolute value.
     *
     * @param percent - the Saturation value between 0 - 100
     * @return the RGB Color object
     */
    @NotNull
    public HSLColor adjustSaturation(final float percent) {
        return new HSLColor(toRGB(hsl[0], percent, hsl[2], alpha));
    }

    /**
     * Create a RGB Color object based on this HSLColor with a different Shade. Changing the shade
     * will return a darker color. The percent specified is a relative value.
     *
     * @param percent - the value between 0 - 100
     * @return the RGB Color object
     */
    @NotNull
    public HSLColor adjustShade(final float percent) {
        return adjustLuminancePercent(-1 * percent, x -> Math.max(0.0f, x));
    }

    /**
     * Create a RGB Color object based on this HSLColor with a different Tone. Changing the tone
     * will return a lighter color. The percent specified is a relative value.
     *
     * @param percent - the value between 0 - 100
     * @return the RGB Color object
     */
    @NotNull
    public HSLColor adjustTone(final float percent) {
        return adjustLuminancePercent(percent, x -> Math.min(100.0f, x));
    }

    @NotNull
    private HSLColor adjustLuminancePercent(final float percent,
                                            @NotNull final Function<Float, Float> capFunction) {
        final float multiplier = (100.0f + percent) / 100.0f;
        final float l = capFunction.apply(hsl[2] * multiplier);
        return new HSLColor(toRGB(hsl[0], hsl[1], l, alpha));
    }

    /**
     * Get the Alpha value.
     *
     * @return the Alpha value.
     */
    public float getAlpha() {
        return alpha;
    }

    /**
     * Create a RGB Color object that is the complementary color of this HSLColor. This is a
     * convenience method. The complementary color is determined by adding 180 degrees to the Hue
     * value.
     *
     * @return the RGB Color object
     */
    @NotNull
    public HSLColor getComplementary() {
        final float hue = (hsl[0] + 180.0f) % 360.0f;
        return new HSLColor(toRGB(hue, hsl[1], hsl[2]));
    }

    /**
     * Get the Hue value.
     *
     * @return the Hue value.
     */
    public float getHue() {
        return hsl[0];
    }

    /**
     * Get the HSL values.
     *
     * @return the HSL values.
     */
    public float[] getHSL() {
        return hsl;
    }

    /**
     * Get the Luminance value.
     *
     * @return the Luminance value.
     */
    public float getLuminance() {
        return hsl[2];
    }

    /**
     * Get the RGB Color object represented by this HDLColor.
     *
     * @return the RGB Color object.
     */
    public Color getRGB() {
        return rgb;
    }

    /**
     * Get the Saturation value.
     *
     * @return the Saturation value.
     */
    public float getSaturation() {
        return hsl[1];
    }

    @NotNull
    public String toString() {
        return "HSLColor[h=" + hsl[0] + ",s=" + hsl[1] + ",l=" + hsl[2] + ",alpha=" + alpha + "]";
    }
}
