package edu.kit.mima.gui.editor.history;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class FileHistoryObject {

        private final int caretPosition;
        private final String text;
        private final int amendLength;

        public FileHistoryObject(final int caretPosition, final String text, final int ammendLength) {
            this.caretPosition = caretPosition;
            this.text = text;
            this.amendLength = ammendLength;
        }

        public int getCaretPosition() {
            return caretPosition;
        }

        public String getText() {
            return text;
        }

        public int getAmendLength() {
            return amendLength;
        }
}
