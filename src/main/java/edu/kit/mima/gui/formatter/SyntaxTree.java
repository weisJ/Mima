package edu.kit.mima.gui.formatter;

import edu.kit.mima.core.parsing.lang.Punctuation;
import edu.kit.mima.core.parsing.token.SyntaxToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * @author Jannis Weis
 * @since 2018
 */
public class SyntaxTree {

    public static SyntaxToken[] tokens = null;
    private SyntaxNode root;

    public SyntaxTree(SyntaxToken[] tokens) {
        SyntaxTree.tokens = tokens;
        this.root = new SimpleSyntaxNode(NodeType.ROOT, 0, tokens.length, null);
        build();
    }

    public SyntaxNode root() {
        return root;
    }

    private void build() {
        buildGroundLayer();
        buildScopes();
    }

    private void buildGroundLayer() {
        for (int i = 0; i < tokens.length; i++) {
            root.addChild(tokenToNode(tokens[i], i));
        }
    }

    private void buildLines(SyntaxNode node) {
        insertLayer(n -> n.getType() == NodeType.NEW_LINE,
                (x, y) -> new SimpleSyntaxNode(NodeType.LINE, x, y, null), node);
        buildJumps(node);
    }

    private void buildJumps(SyntaxNode node) {
        for (var line : new ArrayList<>(node.children())) {
            groupLayer(n -> n.getType() == NodeType.JUMP_DEL,
                    (x, y) -> new SimpleSyntaxNode(NodeType.JUMP, x, y, null), line, false);
        }
        buildInstructions(node);
    }

    private void buildScopes() {
        Stack<SyntaxNode> scopes = new Stack<>();
        scopes.add(root);
        for (var n : new ArrayList<>(root.children())) {
                if (n.getType() == NodeType.SCOPE_OPEN) {
                    scopes.peek().addChild(n);
                    scopes.add(new SimpleSyntaxNode(NodeType.SCOPE, n.getBegin(), -1, null));
                } else if (n.getType() == NodeType.SCOPE_CLOSED && !scopes.isEmpty()) {
                    SyntaxNode scope = scopes.pop();
                    scopes.peek().addChild(scope);
                    scopes.peek().addChild(n);
                    buildLines(scope);
                } else if (!scopes.isEmpty()) {
                    scopes.peek().addChild(n);
                }
        }
        buildLines(root);
    }

    private void buildInstructions(SyntaxNode node) {
        for (var line : new ArrayList<>(node.children())) {
            groupLayer(n -> n.getType() == NodeType.INSTRUCTION_END,
                    (x, y) -> new SimpleSyntaxNode(NodeType.INSTRUCTION, x, y, null), line, false);
        }
        buildBlocks(node);
    }

    private void buildBlocks(SyntaxNode node) {
        insertLayer(n -> n.children().size() <= 1,
                (x, y) -> new SimpleSyntaxNode(NodeType.BLOCK, x, y, null), node);
        for (var n : new ArrayList<>(node.children())) {
            if (calculateSize(n) <= 1) {
                node.removeChild(n);
            }
        }
    }

    private int calculateSize(SyntaxNode node) {
        if (node.children().isEmpty()) {
            return 1;
        }
        int result = 0;
        for (var n : node.children()) {
            result += calculateSize(n);
        }
        return result;
    }

    private void editLayer(Consumer<SyntaxNode> function, SyntaxNode parentLayer) {
        editLayer(n -> true, function, parentLayer);
    }

    private void editLayer(Predicate<SyntaxNode> filter, Consumer<SyntaxNode> function, SyntaxNode parentLayer) {
        for (var n : parentLayer.children()) {
            if (filter.test(n)) {
                function.accept(n);
            }
        }
    }

    private void groupLayer(Predicate<SyntaxNode> filter,
                            BiFunction<Integer, Integer, SyntaxNode> supplier, SyntaxNode parentNode,
                            boolean includeLast) {
        SyntaxNode[] nodes = parentNode.children().toArray(new SyntaxNode[0]);
        List<SyntaxNode> newNodes = group(nodes, filter, supplier, includeLast);
        for (var n : newNodes) {
            IntStream.rangeClosed(n.getBegin(), n.getEnd()).forEach(i -> n.addChild(nodes[i]));
            parentNode.addChild(n);
        }

    }

    private void insertLayer(Predicate<SyntaxNode> filter,
                             BiFunction<Integer, Integer, SyntaxNode> supplier, SyntaxNode insertAfter) {
        SyntaxNode[] nodes = insertAfter.children().toArray(new SyntaxNode[0]);
        List<SyntaxNode> newNodes = group(nodes, filter, supplier, true);
        insertAfter.removeAll();
        insertAfter.addAll(newNodes);
        for (var n : newNodes) {
            IntStream.rangeClosed(n.getBegin(), n.getEnd()).forEach(i -> n.addChild(nodes[i]));
        }
    }

    private void duplicateLayer(SyntaxNode parent) {
        for (var n : new ArrayList<>(parent.children())) {
            var duplicate = new SimpleSyntaxNode(n.getType(), n.getBegin(), n.getEnd(), null);
            parent.removeChild(n);
            duplicate.addChild(n);
            parent.addChild(duplicate);
        }
    }

    public String toString() {
        return "";
    }

    private <T, K extends SyntaxNode> List<K> group(T[] items, Predicate<T> filter,
                                                    BiFunction<Integer, Integer, K> supplier,
                                                    boolean includeLast) {
        List<K> groups = new ArrayList<>();
        int[] splits = splitOn(items, filter);
        int current = 0;
        for (int i : splits) {
            groups.add(supplier.apply(current, i));
            current = i + 1;
        }
        if (current < items.length && includeLast) {
            groups.add(supplier.apply(current, items.length - 1));
        }
        return groups;
    }

    private <T> int[] splitOn(T[] array, Predicate<T> predicate) {
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            if (predicate.test(array[i])) {
                values.add(i);
            }
        }
        return values.stream().sorted(Integer::compareTo).mapToInt(Integer::intValue).toArray();
    }

    private SyntaxNode tokenToNode(SyntaxToken token, int index) {
        SyntaxNode node = new LeafNode(NodeType.LEAF, index, null);
        switch (token.getType()) {
            case PUNCTUATION:
                char c = token.getValue().toString().charAt(0);
                if (c == Punctuation.INSTRUCTION_END) {
                    node.setType(NodeType.INSTRUCTION_END);
                } else if (c == Punctuation.JUMP_DELIMITER) {
                    node.setType(NodeType.JUMP_DEL);
                } else if (c == Punctuation.SCOPE_OPEN) {
                    node.setType(NodeType.SCOPE_OPEN);
                } else if (c == Punctuation.SCOPE_CLOSED) {
                    node.setType(NodeType.SCOPE_CLOSED);
                }
                break;
            case COMMENT:
                node.setType(NodeType.COMMENT);
                break;
            case NEW_LINE:
                node.setType(NodeType.NEW_LINE);
                break;
            default:
                break;
        }
        return node;
    }
}
