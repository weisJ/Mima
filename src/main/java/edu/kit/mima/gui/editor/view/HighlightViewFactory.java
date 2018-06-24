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
            switch (kind) {
                case AbstractDocument.ContentElementName:
                    return new HighlightLabelView(elem);
                case AbstractDocument.ParagraphElementName:
                    return new ParagraphView(elem);
                case AbstractDocument.SectionElementName:
                    return new BoxView(elem, View.Y_AXIS);
                case StyleConstants.ComponentElementName:
                    return new ComponentView(elem);
                case StyleConstants.IconElementName:
                    return new IconView(elem);
            }
        }
        return new LabelView(elem);
    }

}
