package edu.kit.mima.gui.icons;

import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import javax.swing.Icon;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;

/**
 * A Swing Icon that draws an SVG image.
 *
 * @author <a href="mailto:cam%40mcc%2eid%2eau">Cameron McCormack</a>
 */
@SuppressWarnings("CheckStyle")
public class SVGIcon extends UserAgentAdapter implements Icon {

    /**
     * The BufferedImage generated from the SVG document.
     */
    protected BufferedImage bufferedImage;

    /**
     * The width of the rendered image.
     */
    protected int width;

    /**
     * The height of the rendered image.
     */
    protected int height;

    /**
     * Create a new SVGIcon object.
     *
     * @param uri The URI to read the SVG document from.
     * @throws TranscoderException if file cannot be transcoded.
     */
    public SVGIcon(final String uri) throws TranscoderException {
        this(uri, 0, 0);
    }

    /**
     * Create a new SVGIcon object.
     *
     * @param uri The URI to read the SVG document from.
     * @param w   The width of the icon.
     * @param h   The height of the icon.
     * @throws TranscoderException if file cannot be transcoded.
     */
    public SVGIcon(final String uri, final int w, final int h) throws TranscoderException {
        generateBufferedImage(new TranscoderInput(uri), w, h);
    }

    /**
     * Create a new SVGIcon object.
     *
     * @param doc The SVG document.
     * @throws TranscoderException if file cannot be transcoded.
     */
    public SVGIcon(final Document doc) throws TranscoderException {
        this(doc, 0, 0);
    }

    /**
     * Create a new SVGIcon object.
     *
     * @param doc The SVG document.
     * @param w   The width of the icon.
     * @param h   The height of the icon.
     * @throws TranscoderException if file cannot be transcoded.
     */
    public SVGIcon(final Document doc, final int w, final int h) throws TranscoderException {
        generateBufferedImage(new TranscoderInput(doc), w, h);
    }

    /**
     * Generate the BufferedImage.
     */
    protected void generateBufferedImage(@NotNull final TranscoderInput in,
                                         final int w, final int h)
            throws TranscoderException {
        final BufferedImageTranscoder t = new BufferedImageTranscoder();
        if (w != 0 && h != 0) {
            t.setDimensions(w, h);
        }
        t.transcode(in, null);
        bufferedImage = t.getBufferedImage();
        width = bufferedImage.getWidth();
        height = bufferedImage.getHeight();
    }

    /**
     * Returns the icon's width.
     *
     * @return icon width
     */
    public int getIconWidth() {
        return width;
    }

    // Icon //////////////////////////////////////////////////////////////////

    /**
     * Returns the icon's height.
     *
     * @return icon height
     */
    public int getIconHeight() {
        return height;
    }

    /**
     * Draw the icon at the specified location.
     *
     * @param c component
     * @param g graphics object
     * @param x x position
     * @param y x position
     */
    public void paintIcon(final Component c, @NotNull final Graphics g, final int x, final int y) {
        g.drawImage(bufferedImage, x, y, null);
    }

    /**
     * Returns the default size of this user agent.
     *
     * @return the size of the user agent.
     */
    @NotNull
    public Dimension2D getViewportSize() {
        return new Dimension(width, height);
    }

    // UserAgent /////////////////////////////////////////////////////////////

    /**
     * A transcoder that generates a BufferedImage.
     */
    protected class BufferedImageTranscoder extends ImageTranscoder {

        /**
         * The BufferedImage generated from the SVG document.
         */
        protected BufferedImage bufferedImage;

        /**
         * Creates a new ARGB image with the specified dimension.
         *
         * @param width  the image width in pixels
         * @param height the image height in pixels
         */
        @NotNull
        public BufferedImage createImage(final int width, final int height) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        /**
         * Writes the specified image to the specified output.
         *
         * @param img    the image to write
         * @param output the output where to store the image
         */
        public void writeImage(final BufferedImage img, final TranscoderOutput output) {
            bufferedImage = img;
        }

        /**
         * Returns the BufferedImage generated from the SVG document.
         */
        public BufferedImage getBufferedImage() {
            return bufferedImage;
        }

        /**
         * Set the dimensions to be used for the image.
         */
        public void setDimensions(final int w, final int h) {
            hints.put(KEY_WIDTH, (float) w);
            hints.put(KEY_HEIGHT, (float) h);
        }
    }
}
