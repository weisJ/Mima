package edu.kit.mima.gui.icons;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.svg.SVGDocument;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

/**
 * Icon from SVG image.
 *
 * @author Jannis Weis
 * @since 2019
 */
public class SVGIcon implements Icon {

    private final double width;
    private final double height;

    private final int displayWidth;
    private final int displayHeight;
    private final GraphicsNode svgIcon;

    /**
     * Method to fetch the SVG icon from a url.
     *
     * @param url           the url from which to fetch the SVG icon.
     * @param displayWidth  display width of icon.
     * @param displayHeight display height of icon.
     * @throws IOException if url can't be fetched.
     */
    public SVGIcon(@NotNull final URL url, final int displayWidth, final int displayHeight)
            throws IOException {
        this(url, displayWidth, displayHeight, false);
    }

    /**
     * Method to fetch the SVG icon from a url.
     *
     * @param url           the url from which to fetch the SVG icon.
     * @param displayWidth  display width of icon.
     * @param displayHeight display height of icon.
     * @param usePrimitive  uses the primitive bounds of the svg image for width and height. Assumes
     *                      the icon is centered and uses (w + 2 * x, h + 2 * y) as the size.
     * @throws IOException if url can't be fetched.
     */
    public SVGIcon(
            @NotNull final URL url, final int displayWidth, final int displayHeight, final boolean usePrimitive)
            throws IOException {
        this.displayHeight = displayHeight;
        this.displayWidth = displayWidth;

        String xmlParser = XMLResourceDescriptor.getXMLParserClassName();
        SVGDocumentFactory documentFactory = new SAXSVGDocumentFactory(xmlParser);
        SVGDocument doc = documentFactory.createSVGDocument(url.toString());

        var element = doc.getDocumentElement();

        UserAgent userAgent = new UserAgentAdapter();
        DocumentLoader loader = new DocumentLoader(userAgent);

        BridgeContext bridgeContext = new BridgeContext(userAgent, loader);
        bridgeContext.setDynamicState(BridgeContext.STATIC);

        GVTBuilder builder = new GVTBuilder();
        this.svgIcon = builder.build(bridgeContext, doc);
        if (!element.hasAttribute("viewBox") || usePrimitive) {
            width = svgIcon.getPrimitiveBounds().getWidth() + 2 * svgIcon.getPrimitiveBounds().getX();
            height = svgIcon.getPrimitiveBounds().getHeight() + 2 * svgIcon.getPrimitiveBounds().getY();
        } else {
            var viewBox = element.getAttribute("viewBox");
            double[] bounds =
                    Arrays.stream(viewBox.split(" ", 4)).mapToDouble(Double::parseDouble).toArray();
            width = bounds[2] - bounds[0];
            height = bounds[3] - bounds[1];
        }
    }

    private void renderIcon(@NotNull final Graphics2D gc,
                            final double width, final double height, final double angleRadians) {
        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gc.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        double scaleX = width / this.width;
        double scaleY = height / this.height;
        if (width < 0 || height < 0) {
            scaleX = 1.0;
            scaleY = 1.0;
        }

        AffineTransform affineTransform = new AffineTransform();
        affineTransform.translate(-this.width / 2.0, -this.height / 2.0);
        affineTransform.scale(scaleX, scaleY);
        affineTransform.translate((this.width / (2 * scaleX)), (this.height / (2 * scaleY)));
        if (angleRadians != 0) {
            affineTransform.rotate(angleRadians, (width / (2 * scaleX)), (height / (2 * scaleY)));
        }
        if (!affineTransform.isIdentity()) {
            svgIcon.setTransform(affineTransform);
        }
        svgIcon.paint(gc);
    }

    /**
     * Paint the icon with rotation.
     *
     * @param c        the parent component.
     * @param g        the graphics object.
     * @param x        the x coordinate
     * @param y        the y coordinate
     * @param rotation the rotation in radians.
     */
    public void paintIcon(
            final Component c, @NotNull final Graphics g, final int x, final int y, final double rotation) {
        var g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        renderIcon(g2, displayWidth, displayHeight, rotation);
        g2.dispose();
    }

    @Override
    public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
        paintIcon(c, g, x, y, 0);
    }

    @Override
    public int getIconWidth() {
        return displayWidth;
    }

    @Override
    public int getIconHeight() {
        return displayHeight;
    }
}
