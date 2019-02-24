package edu.kit.mima.gui.formatter;

import edu.kit.mima.core.instruction.InstructionSet;
import edu.kit.mima.core.parsing.SyntaxParser;
import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.parsing.token.SyntaxToken;

import java.util.Iterator;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class Formatter {

    private final static String TAB = "    ";
    private SyntaxToken[] tokens;

    public String format(String input) {
        tokens = new SyntaxParser(input, InstructionSet.MIMA_X).parse();
        SyntaxTree tree = new SyntaxTree(tokens);
        SyntaxNode root = tree.root();
        return printNode(root).replaceAll(" *\n", "\n").replaceAll("\t", TAB).trim();
    }

    private String printNode(SyntaxNode node) {
        StringBuilder sb = new StringBuilder();
        switch (node.getType()) {
            case ROOT:
                for (var n : node.children()) {
                    sb.append(printNode(n));
                }
                return sb.toString();
            case SCOPE:
                for (var n : node.children()) {
                    sb.append(printNode(n).replaceAll("\n", "\n" + TAB));
                }
                return '\t' + sb.toString().trim() + '\n';
            case LINE:
                return printLine(node);
            case BLOCK:
                for (var n : node.children()) {
                    sb.append(printNode(n));
                }
                return sb.toString().trim().replaceAll("\n\n+", "\n\n") + "\n\n";
            case LEAF:
                return printLeaf(node);
            case COMMENT:
            case INSTRUCTION_END:
                return tokens[node.getBegin()].getValue().toString() + ' ';
            case INSTRUCTION:
                return printInstruction(node);
            case JUMP_DEL:
                return sb.append(tokens[node.getBegin()].getValue().toString()).toString();
            case JUMP:
                for (var n : node.children()) {
                    sb.append(printNode(n));
                }
                return sb.toString();
            case SCOPE_CLOSED:
            case SCOPE_OPEN:
                return tokens[node.getBegin()].getValue().toString() + '\n';
            case NEW_LINE:
                break;
            default:
                break;
        }
        return "";
    }

    private String printInstruction(SyntaxNode node) {
        StringBuilder sb = new StringBuilder();
        var iterator = node.children().iterator();
        if (!iterator.hasNext()) {
            return sb.toString();
        }
        var n = iterator.next();
        if (n.getType() == NodeType.COMMENT) {
            boolean comment = true;
            sb.append(printNode(n));
            while (iterator.hasNext() && comment) {
                n = iterator.next();
                if (n.getType() == NodeType.COMMENT) {
                    sb.append(' ').append(printNode(n));
                } else {
                    comment = false;
                    sb.append('\n');
                    maybeJump(iterator, sb, n);
                }
            }
        } else {
            maybeJump(iterator, sb, n);
        }
        iterator.forEachRemaining(c -> sb.append(printNode(c)));
        return sb.toString();
    }

    private void maybeJump(Iterator<SyntaxNode> iterator, StringBuilder sb) {
        if (!iterator.hasNext()) {
            return;
        }
        var n = iterator.next();
        maybeJump(iterator, sb, n);
    }

    private void maybeJump(Iterator<SyntaxNode> iterator, StringBuilder sb, SyntaxNode current) {
        sb.append(printNode(current));
        if (current.getType() == NodeType.JUMP) {
            if (iterator.hasNext()) {
                current = iterator.next();
                if (current.getType() == NodeType.SCOPE_OPEN) {
                    sb.append(' ').append(printNode(current));
                } else if (current.getType() == NodeType.NEW_LINE) {
                    sb.append("\n").append(TAB);
                } else {
                    sb.append("\n").append(TAB).append(printNode(current));
                }
            }

        }
    }

    private String printLine(SyntaxNode node) {
        StringBuilder sb = new StringBuilder();
        var iterator = node.children().iterator();
        maybeJump(iterator, sb);
        iterator.forEachRemaining(n -> sb.append(printNode(n)));
        String s = sb.toString();
        if (s.length() == 0 || (s.charAt(s.length() - 1) != '\n' && !s.endsWith(TAB))) {
            sb.append('\n');
        }
        return sb.toString();
    }

    private String printLeaf(SyntaxNode node) {
        SyntaxToken token = tokens[node.getBegin()];
        switch (token.getType()) {
            case JUMP_POINT:
            case KEYWORD:
                return token.getValue().toString() + ' ';
            case PUNCTUATION:
                return printPunctuation(token.getValue().toString().charAt(0));
            default:
                return token.getValue().toString();
        }
    }

    private String printPunctuation(char c) {
        if (c == Punctuation.DEFINITION_DELIMITER) {
            return ' ' + String.valueOf(c) + ' ';
        } else if (c == Punctuation.COMMA) {
            return String.valueOf(c) + ' ';
        }
        return String.valueOf(c);
    }

}
