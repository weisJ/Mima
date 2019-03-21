package edu.kit.mima.gui.components.editor.view;

import org.jetbrains.annotations.NotNull;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.PlainView;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;


/**
 * ViewFactory for supporting jagged-underlines and strike through.
 *
 * @author Jannis Weis
 * @since 2018
 */
public class HighlightViewFactory implements ViewFactory {

    @NotNull
    @Override
    public View create(@NotNull final Element elem) {
        final String kind = elem.getName();
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
                default:
                    return new PlainView(elem);
            }
        }
        return new LabelView(elem);
    }

}
