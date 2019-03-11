package edu.kit.mima.gui.formatter.syntaxtree;

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

    public final SyntaxToken[] tokens;
    private final SyntaxNode root;

    /**
     * Create syntax tree
     * @param tokens tokens to build tree from
     */
    public SyntaxTree(SyntaxToken[] tokens) {
        this.tokens = tokens;
        this.root = new SimpleSyntaxNode(NodeType.ROOT, 0, tokens.length, null);
        build();
    }

    /**
     * Get the root node
     * @return root node
     */
    public SyntaxNode root() {
        return root;
    }

    /**
     * Build the tree
     */
    private void build() {
        buildGroundLayer();
        buildScopes();
    }

    /**
     * Build ground layer consisting of leaf nodes
     */
    private void buildGroundLayer() {
        for (int i = 0; i < tokens.length; i++) {
            root.addChild(tokenToNode(tokens[i], i));
        }
    }

    /**
     * Group lines
     * @param node parent node to create lines on
     */
    private void buildLines(SyntaxNode node) {
        insertLayer(n -> n.getType() == NodeType.NEW_LINE,
                (x, y) -> new SimpleSyntaxNode(NodeType.LINE, x, y, null), node);
        buildJumps(node);
    }

    /**
     * Create Jump layer
     * @param node parent node to create layer on
     */
    private void buildJumps(SyntaxNode node) {
        for (var line : new ArrayList<>(node.children())) {
            groupLayer(n -> n.getType() == NodeType.JUMP_DEL,
                    (x, y) -> new SimpleSyntaxNode(NodeType.JUMP, x, y, null), line, false);
        }
        buildInstructions(node);
    }

    /**
     * Build scopes.
     */
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

    /**
     * Group instructions into one node
     * @param node parent node to group on
     */
    private void buildInstructions(SyntaxNode node) {
        for (var line : new ArrayList<>(node.children())) {
            groupLayer(n -> n.getType() == NodeType.INSTRUCTION_END,
                    (x, y) -> new SimpleSyntaxNode(NodeType.INSTRUCTION, x, y, null), line, false);
        }
        buildBlocks(node);
    }

    /**
     * Create code blocks
     * @param node parent nodes to create blocks on
     */
    private void buildBlocks(SyntaxNode node) {
        insertLayer(n -> n.children().size() <= 1,
                (x, y) -> new SimpleSyntaxNode(NodeType.BLOCK, x, y, null), node);
        for (var n : new ArrayList<>(node.children())) {
            if (calculateSize(n) <= 1) {
                node.removeChild(n);
            }
        }
    }

    /**
     * Calculate size of node recursively.
     * @param node node to calculate size of
     * @return all Leaf node descendants
     */
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

    /**
     * Edit Layer
     * @param function function to apply on children
     * @param parentLayer parentNode
     */
    private void editLayer(Consumer<SyntaxNode> function, SyntaxNode parentLayer) {
        editLayer(n -> true, function, parentLayer);
    }

    /**
     * Edit Layer
     * @param filter filter out nodes to edit
     * @param function function to apply
     * @param parentLayer parentNode
     */
    private void editLayer(Predicate<SyntaxNode> filter, Consumer<SyntaxNode> function, SyntaxNode parentLayer) {
        for (var n : parentLayer.children()) {
            if (filter.test(n)) {
                function.accept(n);
            }
        }
    }

    /**
     * Split children on Predicate and group into new Node
     * @param filter filter to test where to split
     * @param supplier supplier for new node
     * @param parentNode parent node
     * @param includeLast whether to include the last group
     */
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

    /**
     * Insert new layer by grouping
     * @param filter filter to split on
     * @param supplier supplier for new nodes
     * @param insertAfter parent node of new nodes
     */
    private void insertLayer(Predicate<SyntaxNode> filter,
                             BiFunction<Integer, Integer, SyntaxNode> supplier, SyntaxNode insertAfter) {
        SyntaxNode[] nodes = insertAfter.children().toArray(new SyntaxNode[0]);
        List<SyntaxNode> newNodes = group(nodes, filter, supplier, true);
        insertAfter.removeAll();
        for (var n : newNodes) {
            IntStream.rangeClosed(n.getBegin(), n.getEnd()).forEach(i -> n.addChild(nodes[i]));
        }
        insertAfter.addAll(newNodes);
    }

    /**
     * Duplicate a layer
     * @param parent parent of layer to duplicate
     */
    private void duplicateLayer(SyntaxNode parent) {
        for (var n : new ArrayList<>(parent.children())) {
            var duplicate = new SimpleSyntaxNode(n.getType(), n.getBegin(), n.getEnd(), null);
            parent.removeChild(n);
            duplicate.addChild(n);
            parent.addChild(duplicate);
        }
    }

    /**
     * Group items based on predicate
     * @param items items to group
     * @param filter filter to split on
     * @param supplier supplier for new nodes
     * @param includeLast whether to include last group
     * @param <T> Type of input data
     * @param <K> Type of output data
     * @return List of grouped items
     */
    private <T, K> List<K> group(T[] items, Predicate<T> filter,
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

    /**
     * Get indices of split
     * @param array array to split
     * @param predicate predicate to split on
     * @param <T> Type of input array
     * @return integer array containing split indices
     */
    private <T> int[] splitOn(T[] array, Predicate<T> predicate) {
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            if (predicate.test(array[i])) {
                values.add(i);
            }
        }
        return values.stream().sorted(Integer::compareTo).mapToInt(Integer::intValue).toArray();
    }

    /**
     * Parse {@link SyntaxNode} to {@link SyntaxNode}
     * @param token token to parse
     * @param index index of token
     * @return Syntax Node
     */
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
