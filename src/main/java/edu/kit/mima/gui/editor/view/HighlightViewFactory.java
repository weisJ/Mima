package edu.kit.mima.gui.editor.view;

import javax.swing.text.*;

/**
 * ViewFactory for supporting jagged-underlines and strike through
 *
 * @author Jannis Weis
 * @since 2018
 */
public class HighlightViewFactory implements ViewFactory {

    @Override
    public View create(Element elem) {
        String kind = elem.getName();
        if (kind != null) {
            if (kind.equals(AbstractDocument.ContentElementName)) {

                return new HighlightLabelView(elem);
            } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                return new ParagraphView(elem);
            } else if (kind.equals(AbstractDocument.SectionElementName)) {
                return new BoxView(elem, View.Y_AXIS);

            } else if (kind.equals(StyleConstants.ComponentElementName)) {
                return new ComponentView(elem);
            } else if (kind.equals(StyleConstants.IconElementName)) {
                return new IconView(elem);
            }

        }

        // default to text display
        return new LabelView(elem);
    }

}
